class TempVo:
    def __init__(self, feature, item, img):
        self.feature = feature
        self.item = item
        self.img = img

    def __repr__(self):
        return f'TempVo({self.feature},{self.item},{self.img})'