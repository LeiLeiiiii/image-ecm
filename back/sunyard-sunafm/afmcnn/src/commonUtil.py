# 查重实现类
import base64
import io
import mimetypes
import requests
import tensorflow as tf
from io import BytesIO
from requests.exceptions import ConnectTimeout, ReadTimeout, HTTPError


# 判断是否是文件类型
def is_image(filename):
        mime = mimetypes.guess_type(filename)[0]
        return mime and mime.startswith('image/')

def getFileObj(file):
    if file.file_base64:
        file_obj = getFileByBase64(file.file_base64)
        return file_obj
    if file.file_url:
        token = file.token
        file_url = file.file_url
        file_obj = down_image_from_url(file_url, token)
        return file_obj
    else:
        return file.multipart_file


#base64转文件
def getFileByBase64(baseStr):
    decoded_bytes = base64.b64decode(baseStr)
    # 使用io库创建一个BytesIO对象，它允许我们将字节数据当作文件流来处理
    file_obj = io.BytesIO(decoded_bytes)
    # 打开BytesIO对象作为一个图像文件，并转换为PIL图像对象
    # file_obj = Image.open(image_stream)
    return file_obj


# 根据文件的url下载文件
def down_image_from_url(url, token):
    # 要设置的cookie，这是一个字典，其中键是cookie的名字，值是cookie的值
    timeout = 5.0  # 设置超时时间为5秒
    try:
        if token is not None:
            cookies = {
                'Sunyard-Token': token
            }
            response = requests.get(url, cookies=cookies, stream=True, timeout=timeout)
        else:
            response = requests.get(url, stream=True, timeout=timeout)
        if response.status_code == 200:
            image = BytesIO(response.content)
            # 重置文件指针到开头
            return image
        else:
            print(f"无法从 {url} 下载图片。HTTP状态码：{response.status_code}")
    except ConnectTimeout:
        print("连接超时")
    except ReadTimeout:
        print("读取超时")
    except HTTPError as http_err:
        print(f"HTTP错误发生: {http_err}")
    except Exception as err:
        print(f"发生其他错误: {err}")

    return None

 # 计算相似度算法
def cosine_similarity_method(vec1, vec2):
        dot_product = tf.reduce_sum(tf.multiply(vec1, vec2))
        norm1 = tf.norm(vec1)
        norm2 = tf.norm(vec2)
        return dot_product / (norm1 * norm2)

 # 旋转图像
def rotate_image( image, angle):
    rotated_img = image.rotate(angle)
    return rotated_img
