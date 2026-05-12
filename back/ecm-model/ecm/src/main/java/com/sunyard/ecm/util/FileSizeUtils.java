package com.sunyard.ecm.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * @author P-JWei
 * @date 2023/12/4 10:35:35
 * @description 文件大小处理工具类
 */
public class FileSizeUtils {

    private final static String RANGE = "range";



    /**
     * 加载pdf
     *
     * @param response 响应头
     * @param request  请求头
     * @param filePath pdf文件路径
     * @throws FileNotFoundException 异常
     */
    public static void loadFileBig(HttpServletResponse response, HttpServletRequest request, String filePath) throws FileNotFoundException {
        // 以下为pdf分片的代码
        try (InputStream is = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(is);
             OutputStream os = response.getOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(os)) {

            // 下载的字节范围
            int startByte;
            int endByte;
            int totalByte;
            // 根据HTTP请求头的Range字段判断是否为断点续传
            if (request == null || request.getHeader(RANGE) == null) {
                // 如果是首次请求，返回全部字节范围 bytes 0-7285040/7285041
                totalByte = is.available();
                startByte = 0;
                endByte = totalByte - 1;
                // 跳过输入流中指定的起始位置
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // 断点续传逻辑
                String[] range = request.getHeader(RANGE).replaceAll("[^0-9\\-]", "").split("-");
                // 文件总大小
                totalByte = is.available();
                // 下载起始位置
                startByte = Integer.parseInt(range[0]);
                // 下载结束位置
                endByte = range.length > 1 ? Integer.parseInt(range[1]) : totalByte - 1;

                // 跳过输入流中指定的起始位置
                long skip = bis.skip(startByte);

                // 表示服务器成功处理了部分 GET 请求，返回了客户端请求的部分数据。
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            }
            // 表明服务器支持分片加载
            response.setHeader("Accept-Ranges", "bytes");
            // Content-Range: bytes 0-65535/408244，表明此次返回的文件范围
            response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + totalByte);
            // 告知浏览器这是一个字节流，浏览器处理字节流的默认方式就是下载
            response.setContentType("application/octet-stream");
            // 表明该文件的所有字节大小
            response.setContentLength(endByte - startByte + 1);
            // 需要设置此属性，否则浏览器默认不会读取到响应头中的Accept-Ranges属性，
            // 因此会认为服务器端不支持分片，所以会直接全文下载
            response.setHeader("Access-Control-Expose-Headers", "Accept-Ranges,Content-Range");
            // 第一次请求直接刷新输出流，返回响应
            int bytesRead;
            int length = endByte - startByte + 1;
            byte[] buffer = new byte[1024 * 10];
            while ((bytesRead = bis.read(buffer, 0, Math.min(buffer.length, length))) != -1 && length > 0) {
                bos.write(buffer, 0, bytesRead);
                length -= bytesRead;
            }
            response.flushBuffer();
            bos.close();
            bis.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * MB数转为byte
     *
     * @param mb
     * @return
     */
    public static Long mbToByte(Long mb) {
        // 1 MB = 1048576 byte
        Long byteNum = (mb * 1048576L);
        return byteNum;
    }


    /**
     * @param array 目标数组
     * @return 排列组合结果
     * @description: 根据给定二维数组，输出排列组合结果
     */
    public static ArrayList<String> generateList(String[][] array) {
        ArrayList<Integer> lengthArr = new ArrayList<>();
        ArrayList<Integer> productArr = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();
        int length = 1;
        for (int i = 0; i < array.length; i++) {
            int len = array[i].length;
            lengthArr.add(len);
            int product = i == 0 ? 1 : array[i - 1].length * productArr.get(i - 1);
            productArr.add(product);
            length *= len;
        }
        for (int i = 0; i < length; i++) {
            StringBuilder item = new StringBuilder();
            for (int j = 0; j < array.length; j++) {
                item.append(array[j][(int) (Math.floor(i / productArr.get(j)) % lengthArr.get(j))]+";");
            }
            result.add(item.toString());
        }
        return result;
    }

//    public static void main(String[] args) {
//        String[][] array = {{"1月","2月"},{"张三","李四"},{">13",">15",">20"},{"暂无"}};
//
//        //获取集合
//        ArrayList<String> list = generateList(array);
//        for (int i = 0; i < list.size(); i++) {
//            //System.out.println(i+1+":"+list.get(i));
//        }
//    }

    /**
     * 输入流转文件
     *
     * @param ins
     * @param file
     */
    public static void inputStreamToFile(InputStream ins, File file) {
        BufferedOutputStream bos = null;
        BufferedInputStream bis = new BufferedInputStream(ins);
        try {
            //设置为true表示追加
            bos = new BufferedOutputStream(new FileOutputStream(file,true));
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = bis.read(buffer, 0, 8192)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException e) {
                }
                ins = null;
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                }
                bos = null;
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
                bis = null;
            }
        }
    }
}
