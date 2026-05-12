# 反欺诈接口入口
import asyncio
import json
from PIL import Image
from fastapi import FastAPI, File, Form, UploadFile, Body, Depends
from io import BytesIO
from typing import List
from afmService import afmService
from entity.FileVo import FileVo, FileBase, ServerVo,DelFileVo,FileTextVo
from entity.FzObj import FileWithMetadata
from entity.ResObj import ResObj
import concurrent.futures

import subprocess
app = FastAPI()


# 获取实现层
def getAfmService():
    return afmService()


# 文件相似度查询根据文件url
@app.post("/saveFeatureSingle")
async def saveFeature(es: afmService =
                      Depends(getAfmService),
                      fileVo: FileVo = Body(None)):
    loop = asyncio.get_running_loop()
    with concurrent.futures.ThreadPoolExecutor() as pool:
        result = await loop.run_in_executor(pool,es.saveFeature,fileVo)
        if result is None:
            resobj = ResObj(501, "操作失败", None, False)
        else:
            resobj = ResObj(200, "操作成功", None, True)
    return resobj

# 文本查重：存特征
@app.post("/saveFeatureSingleByText")
async def saveFeatureSingleByText(es: afmService =
                      Depends(getAfmService),
                      fileVo: FileTextVo = Body(None)):
    loop = asyncio.get_running_loop()
    with concurrent.futures.ThreadPoolExecutor() as pool:
        result = await loop.run_in_executor(pool,es.saveFeatureByText,fileVo)
        if result is None:
            resobj = ResObj(501, "操作失败", None, False)
        else:
            resobj = ResObj(200, "操作成功", None, True)
    return resobj

@app.post("/deleteVector")
async def deleteVector(es: afmService =
                                  Depends(getAfmService),
                                  fileVos: DelFileVo = Body(None)):
    loop = asyncio.get_running_loop()
    with concurrent.futures.ThreadPoolExecutor() as pool:
        result = await loop.run_in_executor(pool,es.deleteVector,fileVos)
        if result is None:
            resobj = ResObj(501, "操作失败", None, False)
        else:
            resobj = ResObj(200, "操作成功", None, True)
    return resobj

# 文件相似度查询根据文件url
@app.post("/saveFeatureList")
async def saveFeature(es: afmService =
                      Depends(getAfmService),
                      params: str = Form(...),
                      server: str = Form(...),
                      files: List[UploadFile] = File(...)
                      ):
    if files is None:
        return ResObj(501, "操作失败", None, False)

    params = json.loads(params)
        # 检查文件和参数的数量是否匹配
    if len(files) != len(params):
        return ResObj(501, "操作失败", None, False)

    all = []
    for file, param in zip(files, params):
        all.append(FileWithMetadata(file,param))

    tasks = []
    for file in all:
        task = asyncio.create_task(process_image(file))
        tasks.append(task)
        # 等待所有异步任务完成
    results = await asyncio.gather(*tasks)

    images =[]
    for result in results:
        images.append(result)
    result = es.saveFeatureList(images,server)
    return result


# 文件相似度查询根据文件url
@app.post("/delFile")
async def delFile(es: afmService =
                  Depends(getAfmService),
                  fileVo: DelFileVo = Body(None)):
    result = es.delFile(fileVo)
    return result


async def process_image(all) -> Image:
    # 读取文件内容到内存中
    image_bytes = await all.files.read()

    # 使用 BytesIO 将字节数据转换为文件对象
    image_stream = BytesIO(image_bytes)

    # 使用 PIL 打开图像
    image = Image.open(image_stream)
    all.setImage(image)
    # 这里可以对图像进行进一步处理，比如调整大小、转换格式等
    return all


# 文件相似度查询根据文件url
@app.post("/updateData")
async def updateData(es: afmService =
                     Depends(getAfmService),
                     fileVo: FileVo = Body(None)):
    loop = asyncio.get_running_loop()
    with concurrent.futures.ThreadPoolExecutor() as pool:
        resobj = await loop.run_in_executor(pool,es.updateData,fileVo)
    return resobj


# 文件相似度查询根据文件url
@app.post("/queryFilesSingle")
async def queryFilesSingle(es: afmService =
                           Depends(getAfmService),
                           file: FileVo = Body(None)):
    loop = asyncio.get_running_loop()
    with concurrent.futures.ThreadPoolExecutor() as pool:
        result = await loop.run_in_executor(pool, es.queryFilesSingle, file)
    return result

# 文本查重：保存文本信息并返回重复文件
@app.post("/saveFeatureAndQueryFilesByText")
async def saveFeatureAndQueryFilesByText(es: afmService =
                           Depends(getAfmService),
                           file: FileTextVo = Body(None)):
    loop = asyncio.get_running_loop()
    with concurrent.futures.ThreadPoolExecutor() as pool:
        result = await loop.run_in_executor(pool, es.saveFeatureAndQueryFilesByText, file)
    return result


# 文本查重返回重复文本信息
@app.post("/queryFilesSingleByTextNoSave")
async def queryFilesSingleByTextNoSave(es: afmService =
                                 Depends(getAfmService),
                                 file: FileTextVo = Body(None)):
    loop = asyncio.get_running_loop()
    with concurrent.futures.ThreadPoolExecutor() as pool:
        result = await loop.run_in_executor(pool, es.queryFilesSingleByTextNoSave, file)
    return result

# 查重不存特征
@app.post("/queryFilesSingleNoSave")
async def queryFilesSingleNoSave(es: afmService =
                                 Depends(getAfmService),
                                 file: FileVo = Body(None)):
    loop = asyncio.get_running_loop()
    with concurrent.futures.ThreadPoolExecutor() as pool:
        result = await loop.run_in_executor(pool, es.queryFilesSingleNoSave, file)
    return result


# 文件相似度查询根据文件url
@app.post("/queryFilesSingleNoSaveByBase64")
async def queryFilesSingleNoSaveByBase64(es: afmService =
                                         Depends(getAfmService),
                                         fileVo: FileBase = Body(None)):
    loop = asyncio.get_running_loop()
    with concurrent.futures.ThreadPoolExecutor() as pool:
        result = await loop.run_in_executor(pool, es.queryFilesSingleNoSaveByBase64, fileVo)
    return result


# 文件相似度查询根据文件url
@app.post("/queryServerUseSize")
async def queryServerUseSize(es: afmService =
                             Depends(getAfmService),
                             serverVo: ServerVo = Body(None)):
    result = es.queryServerUsed(serverVo)
    return result

if __name__ == '__main__':
    command = [
        'nohup',
        'uvicorn',
        'afmCnn:app',
        '--reload',
        '--host=0.0.0.0',
        '--port=8000'
    ]

    # 使用 subprocess.Popen 来运行命令
    process = subprocess.Popen(command)