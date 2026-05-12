package controller;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;

/**
 * 模拟生成图片工具
 */
public class RandomSizedImageGenerator {

    public static void main(String[] args) throws IOException {
//        getimgsj();
    }
    /**
     * 将文件编码为 Base64 字符串
     *
     * @param file 要编码的文件
     * @return Base64 编码的字符串
     * @throws IOException 如果读取文件时发生错误
     */
    public static byte[] encodeFileToBase64Byte(File file) {
        // 读取文件内容到字节数组
        Path path = Paths.get(file.getAbsolutePath());
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileBytes;
    }


    /**
     * 生成文件
     * @return
     */
    public static MultipartFile getimgsj() {
        // 尝试生成图像
        byte[] imageBytes = new byte[0];
        // 重新生成图像并检查大小
        BufferedImage bufferedImage = generateImage(224, 224);
        String fileName = UUID.randomUUID().toString();
        // 现在你有了一个在目标大小范围内的图像字节数组，可以将其写入文件或进行其他操作
        // 例如，将其写入文件：
        File outputFile = new File("/Users/raochangmei/Downloads/文档/1/" + fileName + ".jpg");
        try {
            ImageIO.write(bufferedImage, "jpg", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MultipartFile as = new ByteArrayMultipartFile(
                outputFile.getName(), // 表单中的文件参数名
                outputFile.getName(), // 原始文件名
                "image/jpeg", // MIME类型
                encodeFileToBase64Byte(outputFile));
        return as;

    }

    private static BufferedImage generateImage(int width, int height) {

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();

            Random random = new Random();

            // 填充背景色（随机）
            Color backgroundColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            g.setColor(backgroundColor);
            g.fillRect(0, 0, width, height);

            // 绘制一些随机颜色的矩形（可选）
            for (int i = 0; i < 100; i++) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                int rectWidth = random.nextInt(100) + 10; // 最小宽度为10
                int rectHeight = random.nextInt(100) + 10; // 最小高度为10
                Color rectColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                g.setColor(rectColor);
                g.fillRect(x, y, rectWidth, rectHeight);
            }

            // 释放Graphics资源
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 将图片写入文件
//            ImageIO.write(image, "png", baos);
            return image;


    }

    private static byte[] writeImageToBytes(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 使用ImageWriter和ImageWriteParam来更精细地控制JPEG压缩（如果需要）
        // ...
        // 或者简单地使用ImageIO的默认设置
        ImageIO.write(image, "jpg", baos); // 注意：这不会应用指定的质量，因为ImageIO的默认方法不使用它

        // 如果你需要控制JPEG压缩质量，你可能需要使用ImageWriter和ImageWriteParam，这更复杂
        // ...

        // 假设你已经有了一个方法来应用指定的质量（这里只是一个占位符）
        // byte[] finalBytes = applyJpegQuality(baos.toByteArray(), quality);

        // 返回字节数组（注意：这只是一个示例，没有实际应用指定的质量）
        return baos.toByteArray();
    }


    /**
     * 字节转为file
     * @return
     * @throws IOException
     */
    public static File multipartFileToFile(String fileName,byte[] bytes) {
        String originalFilename = fileName;
        // 找到文件名中最后一个点的位置
        int lastIndex = originalFilename.lastIndexOf('.');

        // 提取文件的原始名称（不包含扩展名）
        String filenameWithoutExtension = originalFilename.substring(0, lastIndex);
        String extension = originalFilename.substring(lastIndex);
        File tempFile = null;
        try {
            tempFile = Files.createTempFile(filenameWithoutExtension, extension).toFile();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return tempFile;

    }


    // 注意：applyJpegQuality方法是一个伪方法，你需要实现它或使用现有的库来应用JPEG压缩质量
    // private static byte[] applyJpegQuality(byte[] imageBytes, float quality) {
    //     ...
    // }
}