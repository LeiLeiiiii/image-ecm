package controller;

import com.alibaba.fastjson.JSONArray;
import net.coobird.thumbnailator.Thumbnails;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟请求
 */
public class ApiTest1 {
    private static final MediaType MEDIA_TYPE_OCTET_STREAM = MediaType.parse("application/octet-stream");

    private static final String base_url = "http://127.0.0.1:58086";

    public static void main(String[] args) {
//        queryServer();
//        long l = System.currentTimeMillis();
//        //10个线程并发
////        fileDownloadListByDir();
        fileDownloadList(2);
//        long l2 = System.currentTimeMillis();
//        System.out.println("总共用时:" + (l2 - l));
//        fileDownloadList(5);
//        long l3 = System.currentTimeMillis();
//        System.out.println("总共用时:" + (l3 - l2));
    }

    private static void fileDownloadListByDir() {
        // 指定要遍历的磁盘路径，例如 C: 盘
        String drivePath = "/Users/raochangmei/Downloads/work/xldata3/0/";

        // 创建 File 对象
        File root = new File(drivePath);
        File[] files = root.listFiles();
        CountDownLatch latch = new CountDownLatch(files.length);
        //线程池
        executor.execute(() -> {
            ArrayList<MultipartFile> objects = new ArrayList<>();
            for (File file : files) {
                if (!file.getName().contains(".db")) {
                    MultipartFile fileupload = fileupload(file, true);
                    objects.add(fileupload);
                }
            }
            fileuploadList(objects);
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void fileDownloadList(int j) {
        //掉用校验接口，自动切换服务器
        queryServer();
        //文件上传逻辑，这里的上传掉一次接口如果成功的话，则全部成功，如果失败的话则全部失败，需要掉用方处理失败逻辑
        CountDownLatch latch = new CountDownLatch(j);
        for (int m = 0; m < j; m++) {
            //线程池
            executor.execute(() -> {
                //一次上传500个文件
                List<MultipartFile> files = new ArrayList<>();
                for (int i = 0; i < 51; i++) {
                    MultipartFile file = RandomSizedImageGenerator.getimgsj();
                    files.add(file);
                }
                fileuploadList(files);
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private static void queryServer() {
        // 目标URL
        String url = base_url + "/api/afm/queryServer";
        String ret = basePost(url, "{}");
    }

    private static void fileuploadList(List<MultipartFile> files) {
        // 目标URL
        String url = base_url + "/api/afm/saveFeatureNow";

        List<MultipartFile> fileList = new ArrayList<>();
        List<String> md5s = new ArrayList<>();
        try {
            for (MultipartFile s : files) {
                //需要用原文件的的流计算md5，这里只是为了模拟md5
                String md5 = Md5Utils.calculateMD5(s.getInputStream());
                fileList.add(s);
                md5s.add(md5);
            }
            List<Map> maps = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < fileList.size(); i++) {
                Map map = new HashMap<>();
                map.put("businessIndex", "busno_" + random.nextInt(200));
                map.put("businessTypeCode", "ceshi1");
                map.put("businessTypeName", "测试的业务类型");
                map.put("fileIndex", UUID.randomUUID().toString());
                map.put("fileLimit", 5);
                map.put("fileMd5", md5s.get(i));
                map.put("fileName", fileList.get(i).getName());
                map.put("fileSimilarity", "0.85");
                map.put("fileUrl", "http://172.1.1.210:8040/web-api/storage/storage/deal/createInputStreamResources?fileId=1843499883395297280");
                map.put("materialTypeCode", "qyzrs");
                map.put("sourceSys", "YX");
                map.put("materialTypeName", "权益转让书");
                map.put("uploadUserCode", "rao1");
                map.put("uploadUserName", "饶昌妹");
                map.put("isOpencvCheck", "0");
                map.put("year", "2024");
                maps.add(map);
            }
            String jsonString = JSONArray.toJSONString(maps);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30000, TimeUnit.SECONDS) // 连接超时时间
                    .readTimeout(30000, TimeUnit.SECONDS)    // 读取超时时间
                    .writeTimeout(30000, TimeUnit.SECONDS)   // 写入超时时间
                    .build();

            // 创建表单体部分
            MultipartBody.Builder json1 = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("json", jsonString);// 添加其他表单参数

            for (MultipartFile file1 : fileList) {
                File file = RandomSizedImageGenerator.multipartFileToFile(file1.getOriginalFilename(), file1.getBytes());
                json1.addFormDataPart("fileList1", file1.getOriginalFilename(), RequestBody.create(MEDIA_TYPE_OCTET_STREAM, file));// 添加文件
            }

            // 创建请求
            Request request = new Request.Builder()
                    .url(url)
                    .post(json1.build())
                    .build();

            // 发送请求并处理响应
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    e.printStackTrace();
//                    // 处理请求失败
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//                    // 请求成功，处理响应
//                    System.out.println(response.body().string());
//                }
//            });

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                // 请求成功，处理响应
                System.out.println(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
                // 处理请求失败
            }

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
     * 将文件编码为 Base64 字符串
     *
     * @param file 要编码的文件
     * @return Base64 编码的字符串
     * @throws IOException 如果读取文件时发生错误
     */
    public static String encodeFileToBase64(File file) {
        // 读取文件内容到字节数组
        Path path = Paths.get(file.getAbsolutePath());
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 使用 Base64 编码器将字节数组编码为字符串
        String base64EncodedString = Base64.getEncoder().encodeToString(fileBytes);
        return base64EncodedString;
    }

    /**
     * 文件上传
     *
     * @return
     */
    public static MultipartFile fileupload(File file1, boolean falg) {
        // 目标URL
        String url = base_url + "/api/afm/saveFeatureNow";
        File file = null;
        if (falg) {
            file = new File("/Users/raochangmei/Downloads/安装包/图像资料查重/demo/123/" + file1.getName());
            try {
                Thumbnails.of(file1)
                        .size(224, 224)
                        .outputFormat("jpg")
                        .toFile(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            file = file1;
        }
        MultipartFile multipartFile = new ByteArrayMultipartFile(
                "file", // 表单中的文件参数名
                file.getName(), // 原始文件名
                "image/jpeg", // MIME类型
                encodeFileToBase64Byte(file)); // 字节数组内容

        return multipartFile;
//        try {
//            String md5 = Md5Utils.calculateMD5(multipartFile.getInputStream());
//
//            // 要发送的JSON数据
//            String json = "{\n" +
//                    "    \n" +
//                    "    \"businessIndex\": \"89\",\n" +
//                    "    \"businessTypeCode\": \"ceshi1\",\n" +
//                    "    \"businessTypeName\": \"测试01\",\n" +
//                    "    \"fileExif\": \"{\\\"materialType\\\":\\\"qyzrs\\\",\\\"businessIndex\\\":\\\"345\\\",\\\"businessType\\\":\\\"ceshi1\\\",\\\"businessTypeName\\\":\\\"承保类型测试\\\",\\\"materialTypeName\\\":\\\"权益转让书\\\"}\",\n" +
//                    "    \"fileIndex\": \"" + UUID.randomUUID().toString() + "\",\n" +
//                    "    \"fileLimit\": 5,\n" +
//                    "    \"fileMd5\": \"" + md5 + "\",\n" +
//                    "    \"fileName\": \"" + file.getName() + "\",\n" +
//                    "    \"fileSimilarity\": \"0.85\",\n" +
//                    "    \"fileToken\": \"04903f04-742a-4e49-8f93-d9ffd62dedcd\",\n" +
//                    "    \"fileUrl\": \"http://172.1.1.210:8040/web-api/storage/storage/deal/createInputStreamResources?fileId=1843499883395297280\",\n" +
//
//                    "    \"materialTypeCode\": \"qyzrs\",\n" +
//                    "    \"materialTypeName\": \"权益转让书\",\n" +
//                    "    \"sourceSys\": \"YX\",\n" +
//                    "    \"uploadUserCode\": \"rao1\",\n" +
//                    "    \"uploadUserName\": \"饶昌妹\",\n" +
//                    "    \"invoiceType\":\"100000\",\n" +
//                    "    \"queryExpr\":\"\",\n" +
//                    "    \"isOpencvCheck\":0,\n" +
//                    "    \"year\":2024,\n" +
//                    "    \"base64\": \"" + encodeFileToBase64(file) + "\"\n" +
//                    "}";
//            String ret = basePost(url, json);
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }


    static String basePost(String url, String json) {
        // 创建HttpClient实例
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建HttpPost实例
            HttpPost httpPost = new HttpPost(url);

            // 设置请求头信息
            httpPost.setHeader("Content-Type", "application/json");

            // 将JSON字符串设置为POST请求的实体
            StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            // 执行请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                // 获取响应状态码
                int statusCode = response.getStatusLine().getStatusCode();

                // 验证响应状态码是否为200（成功）
                if (statusCode == 200) {
                    // 读取响应内容
                    HttpEntity responseEntity = response.getEntity();
                    String result = EntityUtils.toString(responseEntity);
                    System.out.println("Response Content: " + result);
                    return result;
                } else {
                    System.out.println("Failed to execute the request. Status Code: " + statusCode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static final ExecutorService executor = new ThreadPoolExecutor(
            5, // 核心线程数
            200, // 最大线程数
            0L, // 空闲线程存活时间
            TimeUnit.MILLISECONDS, // 时间单位
            new LinkedBlockingQueue<Runnable>(), // 任务队列
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(10);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "MyThreadPool-" + threadNumber.getAndIncrement());
                }
            }
    );

}