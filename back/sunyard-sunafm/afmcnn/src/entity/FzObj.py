#所有查重定义实体类
from fastapi import UploadFile


#相似度返回值
class FzObj:
    def __init__(self, file_id, similarity):
        self.file_id = file_id
        self.similarity = similarity

    def __repr__(self):
        return f'FzObj(file_id={self.file_id},similarity={self.similarity})'

    def to_dict(self):
        return {
            'file_id': self.file_id,
            'similarity': self.similarity
        }


# 定义一个包含文件和参数的自定义数据结构
class FileWithMetadata:
    def __init__(self, files: UploadFile, metadata):
        self.files = files
        self.metadata = metadata
        self.image = None

    def setImage(self,image):
        self.image = image


# 定义一个包含文件和参数的自定义数据结构
class MivlusVo:
    def __init__(self, file_id: str, file_feature,file_url:str,file_prop):
        self.file_id = file_id
        self.file_feature = file_feature
        self.file_url = file_url
        self.file_prop = file_prop

    def __repr__(self):
        return f'MivlusVo({self.file_id},{self.file_feature},{self.file_url},{self.file_prop})'

    def to_dict(self):
        return {
            'file_id': self.file_id,
            'file_feature': self.file_feature,
            'file_url': self.file_url,
            'file_prop': self.file_prop
        }

