# 查重实现类
import gc

import math
import cv2
import json
import numpy as np
import tensorflow as tf
from PIL import Image
from concurrent.futures import ThreadPoolExecutor, as_completed
from pymilvus import (
    DataType,
    MilvusClient,
    FieldSchema,
    CollectionSchema,
    Collection
)
from pymilvus.client.types import (
    LoadState,
)
from tensorflow.keras.applications.resnet50 import preprocess_input
from tensorflow.keras.layers import GlobalAveragePooling2D
from tensorflow.keras.models import Model
from paddlenlp.transformers import AutoModel, AutoTokenizer
import paddle

from commonUtil import rotate_image, getFileByBase64, getFileObj
from entity.FzObj import MivlusVo
from entity.ResObj import ResObj
from entity.ServerObj import ServerObj
from entity.TempVo import TempVo
from opencvSerive import getFileList
from sklearn.metrics.pairwise import cosine_similarity
import threading
# 创建一个锁对象
# lock = threading.Lock()
prediction_counter = 0
base_model = None
model_loaded = False

server_list = {}
class afmService:
    _instance = None
    _lock = threading.Lock()

    def __new__(cls):
        with cls._lock:
            if cls._instance is None:
                cls._instance = super().__new__(cls)
                cls._instance._init_model()
        return cls._instance

    def _init_model(self):
        self._model_lock = threading.Lock()
        self._model_initialized = False
        self._prediction_counter = 0
        self.base_model = None
        self._predict_fn = None
        # 初始化文本编码模型
        self.text_model_name = "ernie-3.0-medium-zh"
        # self.text_model_name = "/home/jar/afmcnn2/src/modle/ernie-3.0-base-zh/"
        self.text_tokenizer = AutoTokenizer.from_pretrained(self.text_model_name)
        self.text_model = AutoModel.from_pretrained(self.text_model_name)
        self.text_model.eval()
        self.text_dim = 768


    def load_model(self):
        """线程安全的模型加载方法"""
        if self._model_initialized:
            return

        with self._model_lock:  # 模型操作加锁
            if self._model_initialized:
                return

            print("初始模型")
            # 原始模型加载逻辑保持不变
            model = tf.keras.applications.resnet50.ResNet50(
                weights='imagenet',
                include_top=False,
                input_shape=(224, 224, 3)
            )
            model.trainable = False

            x = model.output
            x = GlobalAveragePooling2D()(x)

            # 将base_model改为容器属性
            self.base_model = Model(inputs=model.input, outputs=x)
            self._predict_fn = tf.function(
                self.base_model.__call__,
                jit_compile=True
            )
            self._model_initialized = True
            print("初始加载完成")




    def extract_features(self, file_obj):

        instance = afmService()  # 获取单例
        instance.load_model()

        # 初始图像处理
        img_rgb = file_obj.convert('RGB').resize((224, 224))
        rotation_angles = [90, 180, -90]

        # 预分配内存
        batch_images = np.zeros((len(rotation_angles), 224, 224, 3), dtype=np.float32)

        # 批量预处理（减少中间变量）
        for i, angle in enumerate(rotation_angles):
            rotated_img = rotate_image(img_rgb, angle)
            batch_images[i] = preprocess_input(np.array(rotated_img))
        # 批量预测（减少TF会话调用次数）
        try:
            features = instance._predict_fn(batch_images).numpy()  # 立即转为numpy释放TF资源
            aggregated_features = np.mean(features, axis=0, keepdims=True)

            return aggregated_features

        finally:
            # 正确的内存清理方法
            del batch_images, features ,rotated_img,angle

            global  prediction_counter
            prediction_counter += 1

            if prediction_counter >= 1000:

                tf.keras.backend.clear_session()  # 清理会话但保留模型

                gc.collect()  # 强制垃圾回收
                # 选择性清理TF缓存（不影响模型）
                if tf.config.list_physical_devices('GPU'):
                    tf.config.experimental.reset_memory_stats('GPU:0')


    # 更新向量数据库中的数据
    def updateData(self, file):
        if file is None:
            return ResObj(500, "文件不能为空", None, False)
        obj_server = json.loads(file.server)
        # 校验是否创建数据集
        collection = self.create_collection(obj_server)
        if collection is None:
            return ResObj(501, "集合创建失败", None, False)
        flag = 1
        res = []
        try:
            res = collection.get(collection_name=obj_server["collectionName"], ids=[file.file_id],
                                 output_fields=["file_feature", "file_url"])
        except Exception as exc:
            if exc.code == 1100:
                # 向量数据库中不存在
                flag = 0
        file_json = json.loads(file.file_exif)
        if len(res) >= 1 & flag == 1:
            hit = res[0]
            file_url = hit.get('file_url')
            preds = hit.get('file_feature')
        else:
            file_obj = getFileObj(file)
            # 当文件不存在则跳过这个文件
            if file_obj is None:
                return ResObj(500, "文件不能为空", None, True)
            file_obj = Image.open(file_obj)
            # 当文件不存在则跳过这个文件
            if file_obj is None:
                return ResObj(500, "文件不能为空", None, True)
            # 统一的文件处理
            preds1 = self.extract_features(file_obj)
            preds = preds1[0]
            file_url = file.file_url
        data = [
            {
                "file_id": str(file.file_id),  # 假设 'id' 是您集合中的主键字段
                "file_prop": file_json,  # 要更新的字段和值
                "file_url": file_url,
                "file_feature": preds,
            }
        ]

        # 保存特征
        status = collection.upsert(collection_name=obj_server["collectionName"], data=data)
        print("更新文件:" + obj_server["collectionName"] + file.file_id + ";更新状态:" + str(status))
        return ResObj(200, "操作成功", None, True)

    # 文本查重：保存文本特征
    def saveFeatureByText(self, file):
        if file is None:
            return None

        afmService()  # 获取单例


        obj_server = json.loads(file.server)
        # 获取文本信息
        fileText = file.file_text
        fileTextVector = self.text_to_vector(fileText)
        file_json = json.loads(file.file_exif)
        # 校验是否创建数据集
        collection = self.create_text_collection(obj_server)
        if collection is None:
            return None

        # 判断是否文本内容存在
        existing_entities = collection.query(
            collection_name=obj_server["collectionName"],
            filter=f"file_id == '{file.file_id}'",  # 使用表达式过滤
            output_fields=["file_id"]
        )

        # if existing_entities:
        #     # 已存在该 file_id，可进一步判断内容是否一致
        #     for entity in existing_entities:
        #         stored_text = entity.get("text")
        #         # 可选：比较文本是否相同（防止 ID 相同但内容被修改）
        #         if stored_text == fileText:
        #             print("file_id:"+file.file_id+"记录已存在，文本内容为："+fileText+",无需重复插入")
        #             return ResObj(200, "记录已存在，无需重复插入", None, True)

        if existing_entities:
            return ResObj(200, "记录已存在，无需重复插入", None, True)

        # 不存在则保存特征
        data = [
            {
                "file_id": str(file.file_id),
                "file_prop" : file_json,
                "vector": fileTextVector
            }
        ]
        insert_result = collection.insert(collection_name=obj_server["collectionName"], data=data)
        print("文本查重文件新增:" + obj_server["collectionName"] + file.file_id + ";新增状态:" + str(insert_result))
        return ResObj(200, "操作成功", None, True)

    def text_to_vector(self, text: str) -> np.ndarray:
        # """文本转向量"""
        inputs = self.text_tokenizer(text, padding=True, truncation=True, max_length=500, return_tensors="pd")
        with paddle.no_grad():
            outputs = self.text_model(**inputs)
        # 使用CLS token作为句子表示
        return outputs[0][:, 0, :].numpy().flatten()

    def saveFeatureAndQueryFilesByText(self, file):
        self.saveFeatureByText(file)
        return self.queryFilesSingleByTextNoSave(file)


    # 保存特征
    def saveFeature(self, file):
        if file is None:
            return None

        obj_server = json.loads(file.server)
        # 提取特征
        file_obj = getFileObj(file)
        # 当文件不存在则跳过这个文件
        if file_obj is None:
            return None
        file_obj = Image.open(file_obj)
        # 当文件不存在则跳过这个文件
        if file_obj is None:
            return None
        # 统一的文件处理
        preds = self.extract_features(file_obj)
        file_json = json.loads(file.file_exif)
        data = [
            {"file_id": str(file.file_id), "file_feature": preds[0], "file_url": file.file_url, "file_prop": file_json},
        ]
        # 校验是否创建数据集
        collection = self.create_collection(obj_server)
        if collection is None:
            return None
        # 保存特征
        insert_result = collection.upsert(collection_name=obj_server["collectionName"], data=data)
        fileobj = TempVo(preds, file, file_obj)
        return fileobj

    # 保存特征
    def saveFeatureList(self, files, server):
        server = json.loads(server)
        collection = self.create_collection(server)
        if collection is None:
            return None
        file_list = []
        for file in files:
            ret_list = self.getPredsList(file)
            file_list.append(ret_list)
        insert_result = collection.upsert(collection_name=server["collectionName"], data=file_list)
        print(insert_result)
        return ResObj(200, "操作成功", insert_result, True)

    def getPredsList(self, file):
        metadata = file.metadata  # 或者你可以使用 param 中的某个值来生成 filename
        # 将UploadFile转换为二进制流
        try:
            # 在这里你可以对image对象进行进一步的处理
            preds = self.extract_features(file.image)
            file_json = json.loads(metadata["file_exif"])
            data = MivlusVo(str(metadata["file_id"]), preds[0], metadata["file_url"], file_json)
            return data.to_dict()
            # 校验是否创建数据集
            # 保存特征
        except IOError:
            # 处理无法打开图像的情况
            print(IOError)

    # 相似度查重-存特征
    def queryFilesSingle(self, file):
        resobj = ResObj(200, "暂无数据", None, True)
        token = file.token
        file_num = file.file_limit
        file_expr = file.query_expr
        file_similarity = file.file_similarity
        ex_list = file.ex_list
        file_id = file.file_id
        is_opencv_check = file.is_opencv_check
        thread_num = file.thread_num
        # 存特征
        file = self.saveFeature(file)
        if file is None:
            resobj = ResObj(501, "找不到当前文件", None, False)
            return resobj
        file_list = self.querySimilarity(file, file_expr, file_num, file_similarity, token, file_id,
                                         ex_list, is_opencv_check, thread_num)
        if len(file_list) > 0:
            resobj.setMsg("操作成功")
            resobj.setData(file_list)

        return resobj

    # 文本查重：返回查重结果
    def queryFilesSingleByTextNoSave(self, file):
        resobj = ResObj(200, "暂无数据", None, True)
        file_text = file.file_text
        query_text_num = file.query_text_num
        file_expr = file.query_expr
        # 从向量数据库中查询文件数据
        obj_server = json.loads(file.server)
        client = self.create_milvus_client(obj_server)
        collectionName = self.getCollectionList(client, obj_server)
        if len(collectionName) == 0:
            resobj = ResObj(200, "暂无数据", None, True)
            return resobj

        # 将查询文本转换为向量
        query_vector = self.text_to_vector(file_text)

        # 执行搜索
        results = client.search(
            collection_name=collectionName[0],
            data=[query_vector],
            anns_field="vector",
            search_params= {
                "metric_type": "L2",
                "params": {"nprobe": 10}
            },
            filter=file_expr,
            limit=query_text_num,
            output_fields=["file_id" , "vector"],
        )

        # 处理搜索结果
        similar_texts = []
        for hits in results:
            for hit in hits:
                similar_texts.append({
                    "file_id": hit.entity.get("file_id"),
                    "distance": hit.distance,
                    "rate": self.getSimilarityRate(query_vector,hit.entity.get("vector"))
                })
        if len(similar_texts) > 0:
            resobj.setMsg("操作成功")
            resobj.setData(similar_texts)

        return resobj

    def getSimilarityRate(self,query_vector,vector):
        cosine_sim = cosine_similarity([query_vector], [vector])[0][0]
        print(f"余弦相似度: {cosine_sim:.4f}") # 输出： 余弦相似度: 0.7303
        return cosine_sim


    def l2_distance_to_percent(self,distance: float) -> float:
        """将 L2 distance 转为 [0,100] 的相似度百分比"""
        d0 = 15.0   # 中心距离：认为“中等相似”
        k = 0.15    # 曲线陡峭程度
        try:
            similarity = 100.0 / (1 + math.exp(k * (distance - d0)))
            return round(similarity / 100.0, 4)
        except:
            return 0.0

    # 相似度查重-不存特征
    def queryFilesSingleNoSave(self, file):
        resobj = ResObj(200, "暂无数据", None, True)
        token = file.token
        file_num = file.file_limit
        file_expr = file.query_expr
        file_similarity = file.file_similarity
        ex_list = file.ex_list
        is_opencv_check = file.is_opencv_check
        thread_num = file.thread_num
        # 从向量数据库中查询文件数据
        obj_server = json.loads(file.server)
        client = self.create_milvus_client(obj_server)
        collectionName = self.getCollectionList(client, obj_server)
        if len(collectionName) == 0:
            resobj = ResObj(200, "暂无数据", None, True)
            return resobj
        res = client.query(
            collection_name=collectionName[0],
            filter="file_id in ['" + file.file_id + "']",
            offset=0,
            limit=1,
            output_fields=["file_feature", "file_url"],
        )
        file_obj = getFileObj(file)
        if file_obj is None:
            return None
        file_obj = Image.open(file_obj)

        # 提取特征
        if len(res) > 0:
            hit = res[0]
            preds = hit.get('file_feature')
        else:
            # 当文件不存在则跳过这个文件
            if file_obj is None:
                return None
            # 统一的文件处理
            preds1 = self.extract_features(file_obj)
            preds = preds1[0]
        fileobj = TempVo([preds], file, file_obj)
        file_list = self.querySimilarity(fileobj, file_expr, file_num, file_similarity, token,
                                         file.file_id, ex_list, is_opencv_check, thread_num)
        if len(file_list) > 0:
            resobj.setMsg("操作成功")
            resobj.setData(file_list)
        return resobj

    # 相似度计算-base64
    def queryFilesSingleNoSaveByBase64(self, file):
        resobj = ResObj(200, "暂无数据", None, True)
        token = file.token
        file_num = file.file_limit
        file_expr = file.query_expr
        file_similarity = file.file_similarity
        ex_list = file.ex_list
        is_opencv_check = file.is_opencv_check
        thread_num = file.thread_num
        # 将base64转为图像对象
        # 解码Base64字符串为字节数据
        try:
            file_obj = getFileByBase64(file.file_base64)
            file_obj = Image.open(file_obj)
            preds = self.extract_features(file_obj)
            fileobj = TempVo(preds, file, file_obj)
            file_list = self.querySimilarity(fileobj, file_expr, file_num, file_similarity, token, None, ex_list,
                                             is_opencv_check, thread_num)
            if len(file_list) > 0:
                resobj.setMsg("操作成功")
                resobj.setData(file_list)
            return resobj
        except Exception as exc:
            resobj = ResObj(501, str(exc), None, False)
            return resobj

    # 获取内存大小
    def queryServerUsed(self, servers):
        resobj = ResObj(200, "暂无数据", None, True)
        ret = self.getUseTotal(json.loads(servers.servers))
        resobj.setMsg("操作成功")
        resobj.setData(ret)
        return resobj

    ###########################################向量数据库#################################################################

    def create_milvus_client(self, server):
        id = f"{server['host']}:{server['port']}"
        id = "head_" + str(id)
        aa = server_list.setdefault(id, None)
        if aa:
            return server_list[id]
        else:
            client = MilvusClient(
                uri="http://" + server['user'] + ":" + server['password'] + "@" + server['host'] + ":" + server[
                    'port'] + "/" + server['dbName'],
            )
            server_list[id] = client
            return client
    # 创建文本查重集合
    def create_text_collection(self, server):
        client = self.create_milvus_client(server)
        res = client.get_load_state(
            collection_name=server["collectionName"]
        )
        if res["state"] == LoadState.NotExist:
            try:
                # 定义字段
                fields = [
                    FieldSchema(name="file_id", dtype=DataType.VARCHAR, is_primary=True, auto_id=False, max_length=100),
                    FieldSchema(name="file_prop", dtype=DataType.JSON),
                    FieldSchema(name="vector", dtype=DataType.FLOAT_VECTOR, dim=self.text_dim)
                ]
                index_params = client.prepare_index_params()
                index_params.add_index(
                    field_name="vector",
                    index_type="IVF_FLAT",
                    metric_type="L2",
                    params={"nlist": 6500}
                )
                # 创建集合模式
                schema = CollectionSchema(fields, description="Text collection with vector embeddings")
                # 创建集合
                client.create_collection(collection_name=server["collectionName"], schema=schema,index_params=index_params)
                client.load_collection(server["collectionName"])
            except Exception as exc:
                # 10. 删除集合
                print(f'创建集合失败：{server["collectionName"]}: {exc}')
                client.drop_collection(
                    collection_name=server["collectionName"]
                )
                return None
        if res["state"] == LoadState.NotLoad:
            # 加载现有集合
            client.load_collection(server["collectionName"])
        return client

    # 创建集合
    def create_collection(self, server):
        client = self.create_milvus_client(server)
        res = client.get_load_state(
            collection_name=server["collectionName"]
        )
        if res["state"] == LoadState.NotExist:
            try:
                fields = [
                    FieldSchema(name="file_id", dtype=DataType.VARCHAR, is_primary=True, auto_id=False, max_length=100),
                    FieldSchema(name="file_url", dtype=DataType.VARCHAR, max_length=1000),
                    FieldSchema(name="file_prop", dtype=DataType.JSON),
                    FieldSchema(name="file_feature", dtype=DataType.FLOAT_VECTOR, dim=2048)
                ]
                schema = CollectionSchema(fields=fields, metric_type="L2")
                index_params = client.prepare_index_params()
                index_params.add_index(
                    field_name="file_feature",
                    index_type="IVF_FLAT",
                    metric_type="L2",
                    params={"nlist": 6500}
                )
                client.create_collection(collection_name=server["collectionName"], schema=schema,
                                         index_params=index_params)
                client.load_collection(server["collectionName"])
            except Exception as exc:
                # 10. 删除集合
                print(f'创建集合失败：{server["collectionName"]}: {exc}')
                client.drop_collection(
                    collection_name=server["collectionName"]
                )
                return None
        if res["state"] == LoadState.NotLoad:
            client.load_collection(server["collectionName"])
        return client

    def getUseTotalSingle(self, server):
        client = self.create_milvus_client(server)
        collections = self.getCollectionListNo(client)
        if len(collections) > 0:
            num = 0
            for item in collections:
                res = client.query(
                    collection_name=item,
                    filter="",
                    output_fields=[" count(*)"]
                )
                num += res[0]["count(*)"]
            print(num)
            server_obj = ServerObj(num, server)
            return server_obj
        server_obj = ServerObj(0, server)
        return server_obj

    def getCollectionList(self, client, server):
        # 过滤掉未加载的集合
        ret = []
        res = client.get_load_state(
            collection_name=server["collectionName"]
        )
        if res["state"] == LoadState.Loaded:
            ret.append(server["collectionName"])
        return ret

    def getCollectionListNo(self, client):
        collections = client.list_collections()
        ret = []
        for item in collections:
            res = client.get_load_state(
                collection_name=item
            )
            if res["state"] == LoadState.Loaded:
                ret.append(item)
        return ret

    # 统计指定向量的总数量
    def getUseTotal(self, servers):
        ret_list = []
        for server in servers:
            server_obj = self.getUseTotalSingle(server)
            ret_list.append(server_obj)
        return ret_list

    # 相似度计算
    def querySimilarity(self, file, file_expr, file_num, file_similarity, token, item_file_id, ex_list,
                        is_opencv_check, thread_num):
        print("进行相似度算法：" + str(is_opencv_check) + "; 查询数量：" + str(file_num) + "; 查询相似度：" + str(
            file_similarity))
        # 查重(支持批量)
        search_params = {
            "metric_type": "L2",
            # nprobe的值决定了在搜索过程中需要检查的桶的数量。nprobe越大，搜索的召回率（recall）通常越高，但性能（查询速度）会越差。
            "params": {"nprobe": 16},
        }
        des1 = None
        if is_opencv_check == 1:
            # 获取对比文件
            img1 = file.img
            image_array = np.array(img1)
            # 文件统一处理
            img1_cv = cv2.cvtColor(image_array, cv2.IMREAD_GRAYSCALE)

            # 提取opencv特征点
            img1_cv = cv2.resize(img1_cv, (224, 224))
            sift = cv2.SIFT_create()
            kp1, des1 = sift.detectAndCompute(img1_cv, None)

        # 根据服务器多线程请求
        servers = json.loads(file.item.query_server)

        file_list = []
        if thread_num is None:
            thread_num = 1
        if thread_num < 1:
            thread_num = 1
        if thread_num > 50:
            thread_num = 50
        with ThreadPoolExecutor(max_workers=thread_num) as executor:  # 可以根据需要调整max_workers
            # 使用executor.map或executor.submit来提交任务
            future_to_obj = {
                executor.submit(self.querySimilarityThread, des1, ex_list, file, file_expr, file_num,
                                file_similarity,
                                is_opencv_check, item_file_id, search_params, token, server): server
                for server
                in servers}

            # 等待所有线程完成，并收集结果
            for future in as_completed(future_to_obj):
                try:
                    file_list_part = future.result()  # 获取结果，这里假设getFileList返回的是列表
                    file_list.extend(file_list_part)  # 将结果添加到总列表中
                except Exception as exc:
                    print(f'Generated an exception for : {exc}')

        sorted_desc = sorted(file_list, key=lambda obj: obj.similarity, reverse=True)
        top_5_oldest = sorted_desc[:file_num]
        return top_5_oldest

    def querySimilarityThread(self, des1, ex_list, file, file_expr, file_num, file_similarity, is_opencv_check,
                              item_file_id, search_params, token, server):
        client = self.create_milvus_client(server)
        # base64方式
        collections = self.getCollectionList(client, server)
        file_list = self.multithread_process_objects(collections, file, file_expr, file_num, file_similarity,
                                                     search_params, token,
                                                     item_file_id, ex_list, is_opencv_check, des1, client)
        return file_list

    # 按照集合开线程查询临近的N条数据
    def multithread_process_objects(self, collections, file, file_expr, file_num, file_similarity, search_params,
                                    token, item_file_id, ex_list, is_opencv_check, des1, client):
        if len(collections) == 0:
            # 集合为空返回
            return []
        """
        使用多线程处理collections中的对象，并汇总结果。
        """
        file_list = []  # 初始化结果列表

        for collection in collections:
            arry_list = getFileList(file, file_expr, file_num, file_similarity,
                                    search_params, token, collection, item_file_id, ex_list, is_opencv_check, des1,
                                    client)
            if len(arry_list) >0 :
                file_list.extend(arry_list)
        return file_list

    def deleteVector(self, file):
        # if not fileVos:
        #     return ResObj(400, "请求体不能为空", None, False)


        # for file in fileVos:
        obj_server = json.loads(file.server)
        fileId = file.file_id
        print("fileId:", fileId)
        if file.file_type == 1:  # 文本向量
            try:
                 # 校验并获取 collection
                collection = self.create_text_collection(obj_server)
                print("collection type:", type(collection))
                if collection is None:
                    print(f"无法创建或获取 collection，跳过 file_id: {fileId}")


                # 删除file_id的向量
                collection_name=obj_server["collectionName"]
                expr = f'file_id in ["{fileId}"]'
                print(f"expr: {expr}")
                result = collection.delete(collection_name=collection_name,filter=expr)

                print(f"result: {result}")
                print(f"成功删除 Milvus 中 file_id: {fileId}")

            except Exception as e:
                print(f"删除 Milvus 数据失败 (file_id={fileId}): {e}")


        elif file.file_type == 0:  # 图像查重
            try:
                # 校验是否创建数据集
                collection = self.create_collection(obj_server)
                print("collection type:", type(collection))
                if collection is None:
                    print(f"无法创建或获取 collection，跳过 file_id: {fileId}")


                # 删除file_id的向量
                collection_name=obj_server["collectionName"]
                expr = f'file_id in ["{fileId}"]'
                print(f"expr: {expr}")
                result = collection.delete(collection_name=collection_name,filter=expr)

                print(f"result: {result}")
                print(f"成功删除 Milvus 中 file_id: {fileId}")

            except Exception as e:
                print(f"删除 Milvus 数据失败 (file_id={fileId}): {e}")

        return ResObj(200, "操作成功", None, True)
