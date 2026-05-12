package com.sunyard.module.storage.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class WordTextExtractor {

    // 只处理前50KB
    private static final int MAX_DOC_SAMPLE_SIZE = 50 * 1024;
    private static final int MAX_TEXT_LENGTH = 1500;

    public static byte[] copyInputStream(InputStream input)  {
        ByteArrayOutputStream buffer = null;
        try {
            buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;

            while ((nRead = input.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.toByteArray();
    }
    /**
     * 混合方法：支持DOC和DOCX格式
     */
    public static String extractTextHybrid(byte[] docInputStream)  {
        long startTime = System.currentTimeMillis();
        InputStream streamForDetection = null;
        InputStream streamForProcessing = null;
        try {
            // 首先复制流，因为检测和解析都需要读取流
            InputStream[] streams = duplicateInputStream(docInputStream);
            streamForDetection = streams[0];
            streamForProcessing = streams[1];


            // 检查文件格式
            if (isDocFile(streamForDetection)) {
                String result = extractFromDoc(streamForProcessing);
                return result;
            } else {
                String result = extractFromDocx(streamForProcessing);
                return result;
            }
        }catch (Exception e){
            log.error("系统异常",e);
        }finally {
            // 确保流关闭
            closeQuietly(streamForDetection);
            closeQuietly(streamForProcessing);
        }
        return null;
    }

    /**
     * 复制输入流，用于多次读取
     */
    private static InputStream[] duplicateInputStream(byte[] data) throws IOException {
        // 读取所有数据到字节数组
        return new InputStream[] {
                new java.io.ByteArrayInputStream(data),
                new java.io.ByteArrayInputStream(data)
        };
    }

    /**
     * 读取输入流的所有字节
     */
    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384]; // 16KB buffer

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    /**
     * 检测是否为DOC格式文件
     */
    public static boolean isDocFile(InputStream inputStream) throws IOException {
        if (!inputStream.markSupported()) {
            inputStream = new java.io.BufferedInputStream(inputStream);
        }

        inputStream.mark(8);
        byte[] header = new byte[8];
        int bytesRead = inputStream.read(header);
        inputStream.reset();

        if (bytesRead < 8) {
            return false;
        }

        // DOC文件头特征：D0 CF 11 E0 A1 B1 1A E1
        return (header[0] == (byte) 0xD0 && header[1] == (byte) 0xCF &&
                header[2] == (byte) 0x11 && header[3] == (byte) 0xE0 &&
                header[4] == (byte) 0xA1 && header[5] == (byte) 0xB1 &&
                header[6] == (byte) 0x1A && header[7] == (byte) 0xE1);
    }

    /**
     * 从DOCX文件中提取文本
     */
    private static String extractFromDocx(InputStream docxInputStream) throws IOException {
        // 确保流支持mark/reset
        if (!docxInputStream.markSupported()) {
            docxInputStream = new java.io.BufferedInputStream(docxInputStream);
        }

        try {
            // 方法1：极速ZIP解析
            return extractWithZipParsing(docxInputStream);
        } catch (Exception e) {
            // 方法1失败，重置流尝试方法2
            docxInputStream.reset();
            return extractWithRegex(docxInputStream);
        }
    }

    private static String extractWithZipParsing(InputStream docxInputStream) throws IOException {
        byte[] buffer = new byte[8192];
        StringBuilder textBuilder = new StringBuilder();
        boolean foundDocument = false;

        try (ZipInputStream zis = new ZipInputStream(docxInputStream)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    foundDocument = true;
                    int bytesRead;
                    while ((bytesRead = zis.read(buffer)) != -1) {
                        String chunk = new String(buffer, 0, bytesRead, "UTF-8");
                        extractTextFromChunk(chunk, textBuilder);
                        // 如果已经收集到足够文本，提前退出
                        if (textBuilder.length() > 1500) {
                            break;
                        }
                    }
                    break;
                }
                zis.closeEntry();
            }
        }

        if (!foundDocument) {
            throw new IOException("未找到document.xml");
        }

        String result = textBuilder.toString().replaceAll("\\s+", " ").trim();
        if (result.length() > 1500) {
            return result.substring(0, 1500) + "...";
        }
        return result;
    }

    private static void extractTextFromChunk(String xmlChunk, StringBuilder textBuilder) {
        int start = 0;
        while (true) {
            int textStart = xmlChunk.indexOf(">", start);
            if (textStart == -1) break;

            int textEnd = xmlChunk.indexOf("<", textStart + 1);
            if (textEnd == -1) break;

            String text = xmlChunk.substring(textStart + 1, textEnd).trim();
            if (text.length() > 1 && !text.startsWith("?") && !text.startsWith("!")) {
                textBuilder.append(text).append(" ");
            }

            start = textEnd + 1;
        }
    }

    private static String extractWithRegex(InputStream inputStream) throws IOException {
        // 读取前48KB内容
        byte[] buffer = new byte[48 * 1024];
        int bytesRead = inputStream.read(buffer);
        String content = new String(buffer, 0, bytesRead, "UTF-8");

        // 快速文本提取
        StringBuilder result = new StringBuilder();
        int pos = 0;
        int count = 0;

        while (pos < content.length() && count < 100) {
            int gt = content.indexOf('>', pos);
            if (gt == -1) break;

            int lt = content.indexOf('<', gt + 1);
            if (lt == -1) break;

            String text = content.substring(gt + 1, lt).trim();
            if (text.length() > 2) {
                result.append(text).append(" ");
                count++;
            }

            pos = lt + 1;
        }

        return result.toString().replaceAll("\\s+", " ").trim();
    }

    /**
     * 安静关闭流
     */
    private static void closeQuietly(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // 忽略关闭异常
            }
        }
    }


    /**
     * 从DOC文件中提取文本（仅处理前50KB优化版）
     */
    private static String extractFromDoc(InputStream docInputStream) throws IOException {
        // 包装输入流，限制只读取前50KB
        try (LimitedInputStream limitedStream = new LimitedInputStream(docInputStream, 50 * 1024);
             BufferedInputStream bufferedStream = new BufferedInputStream(limitedStream);
             POIFSFileSystem fs = new POIFSFileSystem(bufferedStream);
             HWPFDocument document = new HWPFDocument(fs);
             WordExtractor extractor = new WordExtractor(document)) {

            // 提取所有文本
            String fullText = extractor.getText();

            // 清理文本，移除多余的空格和换行
            fullText = fullText.replaceAll("\\s+", " ").trim();

            // 限制返回长度，提高性能
            if (fullText.length() > 1500) {
                return fullText.substring(0, 1500) + "...";
            }
            return fullText;
        } catch (Exception e) {
            // 如果因为数据截断导致解析失败，尝试快速文本扫描
            try {
                docInputStream.reset();
                byte[] sampleData = new byte[50 * 1024];
                int bytesRead = docInputStream.read(sampleData);
                if (bytesRead > 0) {
                    String quickText = extractTextFromSample(sampleData, bytesRead);
                    if (!quickText.isEmpty()) {
                        return quickText;
                    }
                }
            } catch (Exception ex) {
                log.error("系统异常",e);
                // 忽略二次错误
            }
            throw new IOException("解析DOC文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 限制读取大小的输入流包装器
     */
    private static class LimitedInputStream extends InputStream {
        private final InputStream original;
        private final long maxBytes;
        private long bytesRead;

        public LimitedInputStream(InputStream original, long maxBytes) {
            this.original = original;
            this.maxBytes = maxBytes;
            this.bytesRead = 0;
        }

        @Override
        public int read() throws IOException {
            if (bytesRead >= maxBytes) {
                return -1;
            }
            int result = original.read();
            if (result != -1) {
                bytesRead++;
            }
            return result;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (bytesRead >= maxBytes) {
                return -1;
            }
            int remaining = (int) (maxBytes - bytesRead);
            int toRead = Math.min(len, remaining);
            int result = original.read(b, off, toRead);
            if (result > 0) {
                bytesRead += result;
            }
            return result;
        }

        @Override
        public void close() throws IOException {
            original.close();
        }
    }

    /**
     * 从样本数据中快速提取文本
     */
    private static String extractTextFromSample(byte[] sampleData, int length) {
        StringBuilder result = new StringBuilder();

        // 尝试常见编码
        try {
            String content = new String(sampleData, 0, length, "UTF-16LE");
            extractTextContent(content, result);
        } catch (Exception e1) {
            try {
                String content = new String(sampleData, 0, length, "UTF-8");
                extractTextContent(content, result);
            } catch (Exception e2) {
                // 如果编码解析失败，尝试二进制扫描
                scanPrintableText(sampleData, length, result);
            }
        }

        return formatText(result.toString());
    }
    /**
     * 文本格式化
     */
    private static String formatText(String text) {
        if (text == null) return "";

        String cleaned = text.replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.length() > MAX_TEXT_LENGTH) {
            return cleaned.substring(0, MAX_TEXT_LENGTH) + "...";
        }
        return cleaned;
    }


    /**
     * 从字符串内容中提取文本
     */
    private static void extractTextContent(String content, StringBuilder result) {
        if (content == null) return;

        // 简单的文本提取：查找连续的字母数字序列
        StringBuilder currentWord = new StringBuilder();
        for (int i = 0; i < content.length() && result.length() < 1500; i++) {
            char c = content.charAt(i);
            if (Character.isLetterOrDigit(c) || isChineseCharacter(c)) {
                currentWord.append(c);
            } else {
                if (currentWord.length() > 1) {
                    result.append(currentWord).append(" ");
                }
                currentWord.setLength(0);
            }
        }

        // 处理最后一个词
        if (currentWord.length() > 1) {
            result.append(currentWord).append(" ");
        }
    }

    /**
     * 二进制方式扫描可打印文本
     */
    private static void scanPrintableText(byte[] data, int length, StringBuilder result) {
        StringBuilder currentWord = new StringBuilder();

        for (int i = 0; i < length && result.length() < 1500; i++) {
            byte b = data[i];

            if (isPrintableASCII(b)) {
                currentWord.append((char) b);
            } else {
                if (currentWord.length() > 2) {
                    String word = currentWord.toString();
                    if (isLikelyText(word)) {
                        result.append(word).append(" ");
                    }
                }
                currentWord.setLength(0);
            }
        }

        // 处理最后一个词
        if (currentWord.length() > 2) {
            String word = currentWord.toString();
            if (isLikelyText(word)) {
                result.append(word).append(" ");
            }
        }
    }

    /**
     * 判断是否为中文字符
     */
    private static boolean isChineseCharacter(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS;
    }

    /**
     * 判断是否为可打印ASCII字符
     */
    private static boolean isPrintableASCII(byte b) {
        int value = b & 0xFF;
        return (value >= 0x20 && value <= 0x7E) || value == 0x09 || value == 0x0A || value == 0x0D;
    }

    /**
     * 判断是否为可能的文本
     */
    private static boolean isLikelyText(String text) {
        if (text == null || text.length() < 2) return false;

        int letterCount = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) letterCount++;
        }
        return (double) letterCount / text.length() > 0.6;
    }



}
