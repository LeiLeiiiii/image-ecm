# 所有查重定义实体类


# 相似度返回值
class ResObj:
    def __init__(self, code, msg, data, succ):
        self.code = code
        self.msg = msg
        self.data = data
        self.succ = succ

    def setCode(self, code):
        self.code = code

    def setMsg(self, msg):
        self.msg = msg

    def setData(self, data):
        self.data = data

    def setSucc(self, succ):
        self.succ = succ

    def __repr__(self):
        return f'ResObj({self.code},{self.msg},{self.data},{self.succ})'

    def to_dict(self):
        return {
            'code': self.code,
            'msg': self.msg,
            'data': self.data,
            'succ': self.succ
        }
