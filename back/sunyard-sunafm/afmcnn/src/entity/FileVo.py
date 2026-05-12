# 所有查重定义实体类
from pydantic import BaseModel
from typing import List


# 文件详情
class FileVo(BaseModel):
    # 文件可下载的url
    file_url: str
    file_base64:str=None
    # 文件md5
    file_id: str
    #文件元数据
    file_exif: str
    #文件可忽略的值
    ex_list:List[str]
    #查询范围
    query_expr: str
    token: str
    file_limit: int
    file_similarity: float
    is_opencv_check: int
    thread_num:int
    server:str = None
    query_server:str = None


class FileTextVo(BaseModel):
    file_text: str
    query_text_num: int
    server:str = None
    query_server:str = None
    file_exif: str
    # 文件md5
    file_id: str
    #查询范围
    query_expr: str

class DelFileVo(BaseModel):
    # 文件可下载的url
    # 文件md5
    file_id: str
    server: str
    file_type: int

class DelFileVoList(BaseModel):
    file_list: List[DelFileVo]

# 文件详情
class FileObj(BaseModel):
    param: FileVo
# 文件详情

# class FileList(BaseModel):


# 文件详情
class FileBase(BaseModel):
    # 文件base64
    file_base64: str
    query_expr: str = None
    token: str
    file_limit: int
    file_similarity: float
    ex_list: List[str]
    is_opencv_check: int
    query_expr: str
    thread_num:int
    server: str = None
    query_server:str = None

# 文件详情
class ServerVo(BaseModel):
    servers: str = None

