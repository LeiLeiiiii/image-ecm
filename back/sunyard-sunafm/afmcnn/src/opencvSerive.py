# 查重实现类

import cv2
import numpy as np
from concurrent.futures import ThreadPoolExecutor, as_completed

from commonUtil import cosine_similarity_method, down_image_from_url
from entity.FzObj import FzObj


# 计算相似度及2次算法
def process_hit(hit, item_file_id, ex_list, file_feature, file_similarity, is_opencv_check, token, des1):
    file_list = []
    i = hit["entity"]['file_feature']
    id = hit["id"]
    if item_file_id is not None and id == item_file_id:
        return file_list
    if id in ex_list:
        return file_list
    cosine_similarity = cosine_similarity_method(file_feature, i)
    sdxx = cosine_similarity.numpy()
    if is_opencv_check == 1:
        checkOpencvsft(des1, file_list, file_similarity, hit, sdxx, token)
        return file_list
    else:
        if sdxx > float(file_similarity):  # 使用锁来保护对file_list的修改
            file_list.append(FzObj(hit["id"], str(sdxx)))
    return file_list


# 查询文件
def getFileList(file, file_expr, file_num, file_similarity, search_params, token, colection_name,
                item_file_id, ex_list, is_opencv_check, des1, client):
    file_list = []
    # 查向量数据库
    # 根据特征和搜索条件查询相近的N条数据
    result = client.search(
        collection_name=colection_name,
        data=file.feature,
        anns_field="file_feature",
        search_params=search_params,
        limit=file_num,
        filter=file_expr,
        consistency_level="Strong",
        output_fields=["file_feature", "file_url", "file_id"])
    print("从向量数据库" + colection_name + "中一共获取：【" + str(len(result[0])) + "】个文件")
    hits = result[0]
    for hit in hits:
        ret = process_hit(hit, item_file_id, ex_list, file.feature[0], file_similarity,
                                         is_opencv_check, token, des1)
        if len(ret) > 0:
            file_list.extend(ret)  # 将结果添加到总列表中

    return file_list


# 二次算法
def checkOpencvsft(des1, file_list, file_similarity, hit, sdxx, token):
    # 设置0.2的偏移量
    if float(file_similarity) > 0.83:
        check_num = float(file_similarity) - 0.03
    else:
        check_num = float(file_similarity)
    if sdxx > check_num:
        # 获取相似图片的文件
        file_url_ba = hit["entity"]["file_url"]
        bytes111 = down_image_from_url(file_url_ba, token)
        if bytes111 is None:
            print("文件下载失败")
            file_list.append(FzObj(hit["id"], str(sdxx)))
        else:
            # 提取opencv特征点
            img2_cv = cv2.imdecode(np.asarray(bytearray(bytes111.read()), dtype=np.uint8),
                                   cv2.IMREAD_GRAYSCALE)  # trainImage
            if img2_cv is None:
                print("文件下载失败")
                file_list.append(FzObj(hit["id"], str(sdxx)))
            else:
                img2_cv = cv2.resize(img2_cv, (224, 224))
                # 检查图像是否加载成功
                if img2_cv is not None:
                    sift2 = cv2.SIFT_create()
                    kp1, des2 = sift2.detectAndCompute(img2_cv, None)
                    matcher = cv2.BFMatcher()
                    matches = matcher.knnMatch(des1, des2, k=2)
                    good = []

                    for m, n in matches:
                        if m.distance < 0.75 * n.distance:
                            good.append([m])

                    if len(good) > 80:
                        sdxx = 0.999999

                    if sdxx > float(file_similarity):
                        file_list.append(FzObj(hit["id"], str(sdxx)))
                else:
                    print("文件转换失败2")
