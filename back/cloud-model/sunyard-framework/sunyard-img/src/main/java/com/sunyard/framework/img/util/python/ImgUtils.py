import cv2
import fitz
import json
import numpy as np
import pytesseract
import sys
import uuid
from PIL import Image
from difflib import SequenceMatcher


#图像预处理
def preprocess_image(image):
    # 转换为灰度图像
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # 应用高斯模糊来降噪（可选，但通常有助于减少OCR错误）
    blurred = cv2.GaussianBlur(gray, (5, 5), 0)

    # 执行自适应阈值处理以实现二值化
    # 这里使用了局部阈值方法，适合背景亮度不均匀的情况
    binary = cv2.adaptiveThreshold(blurred, 255, cv2.ADAPTIVE_THRESH_MEAN_C,
                                   cv2.THRESH_BINARY, 11, 2)
    return binary

#图像对比
def find_differences(line1, line2):
    # 使用SequenceMatcher来比较两个字符串的相似度
    similarity = SequenceMatcher(None, line1, line2).ratio()
    if similarity < 0.8:  # 阈值可以根据需要调整
        array = []
        line2_1 = ''.join(ch for ch in line2 if ch.isalnum())
        line1_1 = ''.join(ch for ch in line1 if ch.isalnum())
        for char in line1_1:
            if char != ' ':
                array.append(char)

        array2 = []
        for char in line2_1:
            if char != ' ':
                array2.append(char)
        return array,array2
    return None,None

def get_ocr_string(thresh):
    # 使用OCR识别文本并获取详细数据
    custom_config = r'--oem 3 --psm 6'
    details = pytesseract.image_to_data(thresh, config=custom_config, lang='chi_sim',
                                        output_type=pytesseract.Output.DICT)
    text = pytesseract.image_to_string(thresh, config=custom_config, lang='chi_sim')
    lines = text.strip().split('\n')
    return details,lines

def get_differences(lines1,lines2):
    differences1 = []
    differences2 = []
    for line1, line2 in zip(lines1, lines2):
        diff, diff1 = find_differences(line1, line2)
        if diff:
            differences1.append(diff)
        if diff1:
            differences2.append(diff1)
    return differences1,differences2

def get_box_position(details):
    # 解析文本行和位置信息
    box = []
    texts = []
    for i in range(len(details['text'])):
        ch = details['text'][i]
        if ch.isalnum() == False:
            continue
        if ch == "":
            continue
        (x, y, w, h) = (
            int(details['left'][i]), int(details['top'][i]), int(details['width'][i]), int(details['height'][i]))
        box.append((x, y,  w,  h))
        texts.append(details['text'][i])
    return box,texts

def find_subarrays_positions(main_array, subarrays,box):
    positions = {}
    for subarray in subarrays:
        subarray_tuple = tuple(subarray)  # 将子数组转换为元组，以便用作字典键
        # 使用循环来找到子数组在 main_array 中的起始位置
        for i in range(len(main_array) - len(subarray) + 1):
            if main_array[i:i + len(subarray)] == subarray:
                start_pos = i
                end_pos = i + len(subarray) - 1
                positions[subarray_tuple] = (start_pos, end_pos)
                break  # 找到第一个匹配项后就跳出循环

    differences = []
    # 打印结果
    for subarray, (start, end) in positions.items():
        while start<end:
            differences.append(box[start])
            start = start+1
    return differences

def get_position_box_line(imageover,differences):
    output_image = imageover.copy()  # 或 image2，取决于你想在哪张图上显示差异
    line_length = 10
    line_thickness = 2
    for box in differences:
        if box is None:
            continue
        x = box[0];y = box[1];w = box[2];h = box[3];
        cv2.line(output_image, (x, y + h + line_length // 2), (x + w, y + h + line_length // 2), (0, 0, 255),
                 line_thickness)
    return output_image

def get_hz(image1,contours):
    result_image = image1.copy()
    line_length = 10
    line_thickness = 2
    # 遍历每个不一致的区域，并在其上方绘制上划线
    for contour in contours:
        # 获取轮廓的边界框
        x, y, w, h = cv2.boundingRect(contour)
        # 在每个区域的顶部绘制上划线（y坐标减去line_length/2并向上绘制）
        # cv2.line(result_image, (x, y - line_length // 2), (x + w, y - line_length // 2), (0, 0, 255), line_thickness)
        # 在每个区域的底部绘制下划线（y坐标加上h加上line_length/2并向下绘制）
        cv2.line(result_image, (x, y + h + line_length // 2), (x + w, y + h + line_length // 2), (0, 0, 255),
                 line_thickness)
    return result_image

#根据像素进行比对
def compare_and_draw_lines(image1, image2,  threshold=30):
    # 确保图像大小相同
    if image1.shape != image2.shape:
        raise ValueError("Images must have the same dimensions")

    # 转换为灰度图像
    gray1 = cv2.cvtColor(image1, cv2.COLOR_BGR2GRAY)
    gray2 = cv2.cvtColor(image2, cv2.COLOR_BGR2GRAY)

    # 计算绝对差异
    diff = cv2.absdiff(gray1, gray2)

    # 阈值处理
    _, thresholded = cv2.threshold(diff, threshold, 255, cv2.THRESH_BINARY)

    # 找到不一致的像素位置
    contours, _ = cv2.findContours(thresholded, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    # 复制第一张图像以在其上绘制线条
    result_image = get_hz(image1,contours)
    result_image_1 = get_hz(image2,contours)

    # 保存结果图像
    return result_image,result_image_1


def get_compare_ocr(imageover,imageover2):
    image1 = preprocess_image(imageover)
    image2 = preprocess_image(imageover2)
    # 二值化图像（你可能需要调整阈值以适应你的图像）
    _, thresh1 = cv2.threshold(image1, 127, 255, cv2.THRESH_BINARY_INV)
    _, thresh2 = cv2.threshold(image2, 127, 255, cv2.THRESH_BINARY_INV)

    # 获取两个ocr识别出来的字符和位置
    details1, lines1 = get_ocr_string(thresh1)
    details2, lines2 = get_ocr_string(thresh2)

    differences1, differences2 = get_differences(lines1, lines2)
    box1, texts1 = get_box_position(details1)
    box2, texts2 = get_box_position(details2)

    differences = find_subarrays_positions(texts1, differences1, box1)
    differences_1 = find_subarrays_positions(texts2, differences2, box2)

    output_image = get_position_box_line(imageover, differences)
    output_image_1 = get_position_box_line(imageover2, differences_1)
    return output_image, output_image_1


#根据字符进行匹配
def compare_base(imageover,imageover2):
    size1 = imageover.size
    size2 = imageover2.size
    if size1 == size2:
        return compare_and_draw_lines(imageover,imageover2)
    else:
        return get_compare_ocr(imageover,imageover2)


def compare_imgobj(img1,img2):
    img1 = np.array(img1)
    img2 = np.array(img2)
    return compare_base(img1,img2)

def compare_img(path1,path2):
    # 加载图像
    imageover = cv2.imread(path1)
    imageover2 = cv2.imread(path2)
    return compare_base(imageover,imageover2)




def pdf_to_images(pdf_path):
    doc = fitz.open(pdf_path)
    page_numbers = list(range(len(doc)))
    images = []
    for page_num in page_numbers:
        page = doc.load_page(page_num)  # page numbers are 0-indexed internally
        pix = page.get_pixmap()
        img = Image.frombytes("RGB", [pix.width, pix.height], pix.samples)
        images.append(img)
    return images


def compare_pdfs(pdf1_path, pdf2_path):
    images1 = pdf_to_images(pdf1_path)
    images2 = pdf_to_images(pdf2_path)

    results = []
    for img1, img2 in zip(images1, images2):
        diff_image, images2 = compare_imgobj(img1,img2)
        results.append((diff_image,images2))

    return results



def display_images(images,path3,path4):
    arr1 = []
    arr2 = []
    for item1,item2 in images:
        unique_string = path3+str(uuid.uuid4())+".png"
        cv2.imwrite(unique_string,item1)
        arr1.append(unique_string)
        unique_string1 = path4+str(uuid.uuid4()) + ".png"
        cv2.imwrite(unique_string1, item2)
        arr2.append(unique_string1)

    # create_pdf(arr1,path3)
    # create_pdf(arr2,path4)

    # #删除临时文件
    # for item in arr1:
    #     # 检查文件是否存在，如果存在则删除
    #     if os.path.exists(item):
    #         os.remove(item)
    #
    #     # 删除临时文件
    # for item in arr2:
    #     # 检查文件是否存在，如果存在则删除
    #     if os.path.exists(item):
    #         os.remove(item)


def Img_Outline_img(original_img):
    gray_img = cv2.cvtColor(original_img, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray_img, (9, 9), 0)  # 高斯模糊去噪（设定卷积核大小影响效果）
    _, RedThresh = cv2.threshold(blurred, 165, 255, cv2.THRESH_BINARY)  # 设定阈值165（阈值影响开闭运算效果）
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (5, 5))  # 定义矩形结构元素
    closed = cv2.morphologyEx(RedThresh, cv2.MORPH_CLOSE, kernel)  # 闭运算（链接块）
    opened = cv2.morphologyEx(closed, cv2.MORPH_OPEN, kernel)  # 开运算（去噪点）
    return original_img, opened

def Img_Outline(input_dir):
    original_img = cv2.imread(input_dir)
    gray_img = cv2.cvtColor(original_img, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray_img, (9, 9), 0)  # 高斯模糊去噪（设定卷积核大小影响效果）
    _, RedThresh = cv2.threshold(blurred, 165, 255, cv2.THRESH_BINARY)  # 设定阈值165（阈值影响开闭运算效果）
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (5, 5))  # 定义矩形结构元素
    closed = cv2.morphologyEx(RedThresh, cv2.MORPH_CLOSE, kernel)  # 闭运算（链接块）
    opened = cv2.morphologyEx(closed, cv2.MORPH_OPEN, kernel)  # 开运算（去噪点）
    return original_img, opened


def findContours_img(original_img, opened):
    contours, hierarchy = cv2.findContours(opened, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    c = sorted(contours, key=cv2.contourArea, reverse=True)  # 计算最大轮廓的旋转包围盒
    cc = c[0]
    rect = cv2.minAreaRect(cc)
    angle = rect[2]
    if abs(angle)==90.0 or abs(angle)==0.0:
        cc = c[1]
        rect = cv2.minAreaRect(cc)
        angle = rect[2]
        if abs(angle) ==0.0:
            return original_img

    if angle > 45:
        angle = angle - 90
    rows, cols = original_img.shape[:2]
    M = cv2.getRotationMatrix2D((cols / 2, rows / 2), angle, 1)
    # 执行仿射变换（旋转）
    rotated_image = cv2.warpAffine(original_img, M, (cols,rows),borderValue=(255,255,255,0))
    return rotated_image

def preprocess_image_blurry(image):
    # 转换为灰度图像
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # 直方图均衡化增强对比度
    equalized = cv2.equalizeHist(gray)

    # 使用形态学操作去除噪声（开运算）
    kernel = np.ones((3, 3), np.uint8)
    opening = cv2.morphologyEx(equalized, cv2.MORPH_OPEN, kernel)

    return opening


def is_blurry(image, threshold):
    # 预处理图像
    preprocessed = preprocess_image_blurry(image)

    # 使用Canny边缘检测
    edges = cv2.Canny(preprocessed, 50, 150)

    # 计算拉普拉斯算子
    laplacian = cv2.Laplacian(preprocessed, cv2.CV_64F)

    # 计算拉普拉斯算子的绝对值之和
    laplacian_variance = cv2.sumElems(cv2.convertScaleAbs(laplacian))[0]

    # 判断是否模糊
    return laplacian_variance < int(threshold)

def rotate(image, parameters):
    rotationAngle = parameters["rotationAngle"]

    # 获取图像尺寸
    (h, w) = image.shape[:2]
    center = (w // 2, h // 2)
    # 计算旋转矩阵
    M = cv2.getRotationMatrix2D(center, rotationAngle, 1.0)
    # 计算旋转后的边界框
    cos = np.abs(M[0, 0])
    sin = np.abs(M[0, 1])
    # 新的边界尺寸
    newW = int((h * sin) + (w * cos))
    newH = int((h * cos) + (w * sin))
    # 调整旋转矩阵以考虑平移
    M[0, 2] += (newW / 2) - center[0]
    M[1, 2] += (newH / 2) - center[1]
    # 执行旋转并调整大小
    rotated = cv2.warpAffine(image, M, (newW, newH))
    # 如果需要，可以裁剪掉空白部分以匹配原始尺寸（但这对于180度旋转不是必需的）
    # 例如，对于180度旋转，可以直接返回 rotated[:h, :w, :]（但这样会有黑色边框）
    # 或者，如果希望填充空白部分，可以使用其他方法
    if rotationAngle == 180:
        # 对于180度旋转，我们只需要返回中心部分
        return rotated[newH // 2 - h // 2:newH // 2 + h // 2, newW // 2 - w // 2:newW // 2 + w // 2]
    # 对于其他角度，可以选择返回完整大小的旋转图像或进行其他处理
    return rotated


def horizontal_mirror(image, parameters=None):
    """
    水平镜像图像
    :param image: 输入的图像
    :param parameters: 该参数在此函数中未使用，但为了保持函数签名的一致性而保留
    :return: 水平镜像后的图像
    """
    # 使用numpy的fliplr函数进行水平镜像
    mirrored = np.fliplr(image)
    return mirrored

def vertical_mirror(image, parameters=None):
    """
    垂直镜像图像
    :param image: 输入的图像
    :param parameters: 该参数在此函数中未使用，但为了保持函数签名的一致性而保留
    :return: 垂直镜像后的图像
    """
    # 使用numpy的flipud函数进行垂直镜像
    mirrored = np.flipud(image)
    return mirrored


# 检查是否为空和是否等于0的等价函数（这里我们假设参数不会是None，只会是0或正数）
def is_not_empty_and_not_zero(value):
    return value != 0.0


def brighten_img(original_image, brightness_percentage):
    # 确保brightness_percentage在有效范围内，并计算亮度增强系数
    # 这里brightness_percentage实际上是一个增量，所以我们需要将其转换为系数
    brightness_coefficient = 1 + brightness_percentage  # 假设brightness_percentage是以百分比给出的

    # 进行亮度增强操作
    # 方法1：使用cv2.convertScaleAbs（这是推荐的方法，因为它会自动处理溢出并转换为uint8）
    brightened_image = cv2.convertScaleAbs(original_image, alpha=brightness_coefficient)

    # 方法2：使用NumPy进行简单的数组乘法（注意：这可能会导致数据溢出，需要后续处理）
    # brightened_image = np.clip(original_image.astype(np.float32) * brightness_coefficient, 0, 255).astype(np.uint8)

    return brightened_image



# 淡化函数（简单实现，通过减少亮度值）
def downplay_img(image, value):
    if not (0.0 <= value < 1.0):
        raise ValueError("Factor must be between 0.0 and 1.0 (exclusive).")

    # 将图像转换为float32类型以进行数学运算
    image_float = image.astype(np.float32)

    # 应用亮度因子
    darkened_image_float = image_float * value

    # 将结果裁剪到uint8的有效范围，并转换回uint8类型
    darkened_image = np.clip(darkened_image_float, 0, 255).astype(np.uint8)

    return darkened_image


def sharpen_img(original_image, sharpening_percentage):
    # 确保sharpening_percentage在有效范围内
    if not (0.0 <= sharpening_percentage <= 1.0):
        raise ValueError("Sharpening percentage must be between 0.0 and 1.0.")

    # 创建和原始图片相同大小的空白图片（实际上不需要，因为filter2D会直接修改输出图像）
    # 但为了保持与Java代码相似的结构，我们还是创建一个
    sharpened_image = np.zeros_like(original_image)

    # 定义锐化滤波核
    # 注意：这里的核是根据Java代码的逻辑转换的，但可能不是最优的锐化核
    # 通常，锐化核的中心权重会更大，而周围权重为负，但总和应该接近0以保持亮度不变
    # 下面的核是一个简单的例子，可能需要根据实际情况进行调整
    kernel = np.array([[-sharpening_percentage, -sharpening_percentage, -sharpening_percentage],
                       [-sharpening_percentage, 1 + 8 * sharpening_percentage, -sharpening_percentage],
                       [-sharpening_percentage, -sharpening_percentage, -sharpening_percentage]], dtype=np.float32)

    # 注意：上面的核与Java代码的逻辑不完全一致，因为Java代码中的中心权重计算方式可能有问题
    # （它直接加上了9*sharpeningPercentage，这会导致中心权重过大，除非sharpeningPercentage非常小）
    # 下面的核是一个更常见的锐化核，其中中心权重为1+8*sharpeningPercentage（因为周围有8个-1*sharpeningPercentage）
    # 这样，当sharpeningPercentage=0.1时，中心权重为1.8，周围权重为-0.1，总和为0（近似）

    # 进行锐化滤波操作
    sharpened_image = cv2.filter2D(original_image, -1, kernel)

    # 注意：由于我们使用了float32类型的核，输出图像可能会是float32类型
    # 如果需要，可以将其转换回uint8类型，但请注意这可能会导致数据溢出或截断
    # 如果要转换，可以使用以下代码：
    # sharpened_image = np.clip(sharpened_image, 0, 255).astype(np.uint8)

    # 然而，在这个例子中，我们保持输出为float32类型，以便后续处理（如果需要）
    # 或者你可以根据实际需求决定是否转换类型

    return sharpened_image


def crop_img(original_image, region):
    # 提取裁剪区域的坐标和尺寸
    x, y, width, height = region

    # 获取原始图像的尺寸
    img_height, img_width = original_image.shape[:2]

    # 检查并调整裁剪区域，以确保它不会超出图像的边界
    x_end = min(x + width, img_width)
    y_end = min(y + height, img_height)
    width = x_end - x
    height = y_end - y

    # 如果调整后的宽度或高度为零或负数，则抛出异常（这取决于你的错误处理策略）
    if width <= 0 or height <= 0:
        raise ValueError("裁剪区域超出图像边界或无效")

    # 裁剪图像
    cropped_image = original_image[y:y_end, x:x_end]

    return cropped_image


def remove_black_edge_two(img):
    # 检查图像是否为空
    if img is None or img.size == 0:
        return img

    # 高斯滤波
    gaussian_blur_img = cv2.GaussianBlur(img, (3, 3), 2, 2)

    # 转为灰度图、二值化
    gray_img = cv2.cvtColor(gaussian_blur_img, cv2.COLOR_BGR2GRAY)
    _, threshold_img = cv2.threshold(gray_img, 100, 255, cv2.THRESH_BINARY_INV)

    # 形态学闭运算，消除内部黑点
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    threshold_img = cv2.morphologyEx(threshold_img, cv2.MORPH_CLOSE, kernel)

    # 寻找最外围轮廓
    contours, hierarchy = cv2.findContours(threshold_img, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    # 原图上填充非黑边区域
    for contour in contours:
        x, y, w, h = cv2.boundingRect(contour)
        if w > 20 and h > 20:
            # 在原图img上填充白色
            cv2.drawContours(img, [contour], -1, (255, 255, 255), -1)

    # 返回处理后去除了黑边的原图
    return img


if __name__ == "__main__":
    type = sys.argv[1]
    if type=="1":
        input_dir = sys.argv[2]
        output_path = sys.argv[3]
        original_img, opened = Img_Outline(input_dir)
        result_img = findContours_img(original_img, opened)
        # # 将结果图像保存到本地文件
        cv2.imwrite(output_path, result_img)
        print("Succ")
        sys.exit(0)
    if type=="2":
        path1 = sys.argv[2]
        path2 = sys.argv[3]
        path3 = sys.argv[4]
        path4 = sys.argv[5]
        results = compare_pdfs(path1, path2)
        display_images(results,path3,path4)
        print("Succ")
        sys.exit(0)
    if type =="3":
        path1 = sys.argv[2]
        path2 = sys.argv[3]
        path3 = sys.argv[4]
        path4 = sys.argv[5]
        output_image,output_image_1 = compare_img(path1,path2)
        cv2.imwrite(path3,output_image)
        cv2.imwrite(path4,output_image_1)
        print("Succ")
        sys.exit(0)
    if type =="4":
        # 读取图像
        image_path = sys.argv[2]
        threshold = sys.argv[3]
        image = cv2.imread(image_path)
        # 检测图像是否模糊
        if image is not None:
            if is_blurry(image,threshold):
                print(True)
            else:
                print(False)
        else:
            print("Failed to read the image.")
        sys.exit(0)
    if type =="5":
        input_dir = sys.argv[2]
        output_path = sys.argv[3]
        vo = sys.argv[4]
        p = json.loads(vo)

        original_image = cv2.imread(input_dir)
        if p["rotationAngle"] is not None and p["rotationAngle"]>0:
            original_image = rotate(original_image, p)
        if p["horizontalMirror"] is not None and p["horizontalMirror"]>0:
            original_image = vertical_mirror(original_image, p)
        if p["mirrorVertically"] is not None and p["mirrorVertically"]>0:
            original_image = horizontal_mirror(original_image, p)

        if len(p["crop"]) == 4:
            original_image = crop_img(original_image, p["crop"])


        if p["blackEdge"] == True:
            original_image = remove_black_edge_two(original_image)

        if p["corrective"] == True:
            original_image, opened = Img_Outline_img(original_image)
            original_image = findContours_img(original_image, opened)

        # 亮化
        if is_not_empty_and_not_zero(p["brighten"]):
            original_image = brighten_img(original_image, p["brighten"])

        # 淡化
        if is_not_empty_and_not_zero(p["downplay"]):
            original_image = downplay_img(original_image, (1 - p["downplay"]))

        # 锐化
        if is_not_empty_and_not_zero(p["sharpen"]):
            original_image = sharpen_img(original_image, p["sharpen"])

        cv2.imwrite(output_path, original_image)
        print("Succ")
        sys.exit(0)

