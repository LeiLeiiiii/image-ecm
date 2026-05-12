

# 相似度返回值
class ServerObj:
    def __init__(self, num,server):
        self.num = num
        self.server = server

    def __repr__(self):
        return f'ServerVo({self.num},{self.server})'

