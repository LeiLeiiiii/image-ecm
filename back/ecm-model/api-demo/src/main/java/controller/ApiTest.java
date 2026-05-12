package controller;

import okhttp3.Call;
import okhttp3.Callback;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiTest {
    private static final MediaType MEDIA_TYPE_OCTET_STREAM = MediaType.parse("application/octet-stream");

    private static String baseUrl = "http://172.1.1.80:8080/web-api";
    //天安测试环境地址
//    private static String baseUrl = "http://192.168.133.195/web-api";

    public static void main(String[] args) {
        //1、文件上传
//        fileUpload("/Users/raochangmei/Downloads/zipkin-server-2.12.9-exec.jar");
        //2 文件上传批量
//        fileUploadBatch("/Users/raochangmei/Desktop/temp");
//
//        //3、文件下载
//        fileDownload("1834495574132359168");
//        //4.文件下载批量
//        ArrayList<String> ids = new ArrayList<>();
//        ids.add("1834495574132359168");
//        fileDownloadBatch(ids);
        //5、业务属性修改
//        busiUpdate();
        //6、业务查询
//        busiQuery();
        //7、业务复用
        busiReuse();
    }


    /**
     * 文件上传-单个
     */
    static void fileUpload(String filepath) {
        // 上传文件的URL
        String url = baseUrl + "/storage/api/storage/oss/splitUpload/useS3Upload";
        // 要上传的文件
        File file = new File(filepath);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(30000, TimeUnit.SECONDS)    // 读取超时时间
                .writeTimeout(30000, TimeUnit.SECONDS)   // 写入超时时间
                .build();
        // 创建表单体部分
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE_OCTET_STREAM, file)) // 添加文件
                .addFormDataPart("fileName", file.getName()) // 添加其他表单参数
                .addFormDataPart("fileSource", "OpenApi")
                .addFormDataPart("isEncrypt", "0")
                .addFormDataPart("stEquipmentId", "1689192993912786946") //设备名称
//                .addFormDataPart("stEquipmentId", "1830869404182605824") //设备名称-天安
                .addFormDataPart("userId", "0")
                .addFormDataPart("md5", "0")
                .build();

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // 发送请求并处理响应
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                
                // 处理请求失败
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                // 请求成功，处理响应
                System.out.println(response.body().string());
            }
        });
    }



    /**
     * 文件上传-批量
     */
    static void fileUploadBatch(String filePath) {
        // 调用方法遍历目录
        List<File> files1 = listFilesForFolder(new File(filePath));
        // 上传文件的URL
        String url = baseUrl + "/storage/api/storage/oss/splitUpload/useS3Upload";

        // 要上传的文件
        for (File file : files1) {
            executor.execute(() -> {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file",  file.getName(), RequestBody.create(MEDIA_TYPE_OCTET_STREAM, file)) // 添加文件
                        .addFormDataPart("fileName", file.getName()) // 添加其他表单参数
                        .addFormDataPart("fileSource", "OpenApi")
                        .addFormDataPart("isEncrypt", "0")
//                        .addFormDataPart("stEquipmentId", "1689192993912786946") //设备名称
                        .addFormDataPart("stEquipmentId", "1830869404182605824") //设备名称-天安
                        .addFormDataPart("userId", "0")
                        .addFormDataPart("md5", "0")
                        .build();

                // 创建请求
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        
                        // 处理请求失败
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        // 请求成功，处理响应
                        System.out.println(response.body().string());
                    }
                });

            });
        }
    }

    /**
     * 文件下载
     */
    public static void fileDownload(String id) {
        // API URL
        String url = baseUrl + "/storage/api/storage/deal/shardingDownFile";

        // JSON 请求体
        String jsonRequestBody = "{\n" +
                "    \"isPack\": 0,\n" +//是否打包下载
                "    \"userName\": \"操作员姓名\",\n" +//掉用下载接口的用户名
                "    \"instName\": \"总公司\",\n" +//掉用下载接口的机构名
                "    \"fileId\": [\n" +
                id +//文件id，即上传后返回参数中的id
                "    ]\n" +
                "}";

        // 发送请求并下载文件
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");

            // 设置请求体
            StringEntity entity = new StringEntity(jsonRequestBody, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                // 检查响应状态码
                if (response.getStatusLine().getStatusCode() == 200) {
                    // 获取响应体并写入文件
                    HttpEntity responseEntity = response.getEntity();
                    if (responseEntity != null) {
                        try (FileOutputStream fileOutputStream = new FileOutputStream("/Users/raochangmei/Downloads/xxxx.png")) {
                            responseEntity.writeTo(fileOutputStream);

                            System.out.println("File downloaded successfully.");
                        }
                    }
                } else {
                    System.out.println("Failed to download file: HTTP status code " + response.getStatusLine().getStatusCode());
                }
            }
        } catch (IOException e) {
            
        }

    }

    /**
     * 文件下载
     */
    public static void fileDownloadBatch(ArrayList<String> ids) {
        // API URL
        String url = baseUrl + "/storage/api/storage/deal/shardingDownFile";

        for (String id : ids) {
            executor.execute(() -> {
                // JSON 请求体
                String jsonRequestBody = "{\n" +
                        "    \"isPack\": 0,\n" +//是否打包下载
                        "    \"userName\": \"操作员姓名\",\n" +//掉用下载接口的用户名
                        "    \"instName\": \"总公司\",\n" +//掉用下载接口的机构名
                        "    \"fileId\": [\n" +
                        id +//文件id，即上传后返回参数中的id
                        "    ]\n" +
                        "}";

                // 发送请求并下载文件
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpPost httpPost = new HttpPost(url);

                    // 设置请求头
                    httpPost.setHeader("Content-Type", "application/json");

                    // 设置请求体
                    StringEntity entity = new StringEntity(jsonRequestBody, StandardCharsets.UTF_8);
                    httpPost.setEntity(entity);

                    // 发送请求并获取响应
                    try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                        // 检查响应状态码
                        if (response.getStatusLine().getStatusCode() == 200) {
                            // 获取响应体并写入文件
                            HttpEntity responseEntity = response.getEntity();
                            if (responseEntity != null) {
                                try (FileOutputStream fileOutputStream = new FileOutputStream("/Users/raochangmei/Downloads/xxxx.png")) {
                                    responseEntity.writeTo(fileOutputStream);

                                    System.out.println("File downloaded successfully.");
                                }
                            }
                        } else {
                            System.out.println("Failed to download file: HTTP status code " + response.getStatusLine().getStatusCode());
                        }
                    }
                } catch (IOException e) {
                    
                }
            });
        }


    }


    /**
     * 业务查询
     */
    static void busiQuery() {
        // 目标URL
        String url = baseUrl + "/ecm/api/ecms/operate/query/queryEcm";

        // 要发送的JSON数据
        String json = "{\n" +
                "  \"ecmBaseInfoDTO\": {\n" +
                "    \"orgCode\": \"XYD\",\n" + //掉用方机构code
                "    \"orgName\": \"总公司\",\n" + //掉用方机构name
                "    \"roleCode\": \"yx\",\n" + //角色code
                "    \"userCode\": \"scm\",\n" + //掉用方用户名code
                "    \"userName\": \"操作员姓名\"\n" +//掉用方用户名name
                "  },\n" +
                "  \"ecmBusExtendDTOS\": [\n" +
                "    {\n" +
                "      \"appCode\": \"ceshi1\",\n" + //业务类型
                "      \"appName\": \"动态树测试\",\n" +//业务名称
                "      \"ecmBusiAttrDTOList\": [\n" +
                "        {\n" +
                "          \"appAttrValue\": \"01\",\n" +//业务主索引值
                "          \"attrCode\": \"ywbh\"\n" + //业务主索引code
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String ret = basePost(url, json);
    }


    /**
     * 业务属性修改
     */
    static void busiUpdate() {
        // 目标URL
        String url = baseUrl + "/ecm/api/ecms/operate/attributes/setBusiAttr";

        // 要发送的JSON数据
        String json = "{\n" +
                "  \"appCode\": \"ceshi1\",\n" + //业务类型code
                "  \"busiNo\": \"02\",\n" + //业务主索引
                "  \"ecmUserDto\":{\n" +
                "    \"orgCode\": \"XYD\",\n" + //掉用方机构code
                "    \"orgName\": \"总公司\",\n" + //掉用方机构name
                "    \"roleCode\": \"yx\",\n" + //角色code
                "    \"userCode\": \"scm\",\n" + //掉用方用户名code
                "    \"userName\": \"ces\"\n" +//掉用方用户名name
                "  },\n" +
                "  \"ecmBusiAttrDTOList\": [\n" +
                "    {\n" +
                "      \"attrCode\": \"ywbh\",\n" + //需要修改的业务属性code
                "      \"appAttrValue\": \"01\"\n" +//需要修改的业务属性值
                "    }\n" +
                "  ]\n" +
                "}";

        String ret = basePost(url, json);
    }

    /**
     * 业务复用
     */
    static void busiReuse() {
        // 目标URL
        String url = baseUrl + "/ecm/api/ecms/operate/copy/busiDocDuplicate";

        // 要发送的JSON数据
        String json = "{\n" +
                "  \"typeNo\": \"cxlp\",\n" + //复用的源业务对应的业务类型
                "  \"busiNo\": \"20240202003\",\n" + //复用的源业务主索引
                "  \"docNo\": [\n" +
                "    \"lpxy\"\n" + //复用的源业务下的资料节点
                "  ],\n" +
                "  \"busiDocDuplicateVos\": [\n" +
                "    {\n" +
                "      \"typeNo\": \"cxlp\",\n" +
                "      \"busiNo\": \"20240202002\"\n" +
                "    }" +
                ",\n" +
                "    {\n" +
                "      \"typeNo\": \"cxcb\",\n" +
                "      \"busiNo\": \"202403020022\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"ecmUserDto\": {\n" +
                "    \"orgCode\": \"XYD\",\n" + //掉用方机构code
                "    \"orgName\": \"总公司\",\n" + //掉用方机构name
                "    \"roleCode\": \"yx\",\n" + //角色code
                "    \"userCode\": \"scm\",\n" + //掉用方用户名code
                "    \"userName\": \"操作员姓名\"\n" +//掉用方用户名name
                "  }\n" +
                "}";

        String ret = basePost(url, json);

    }


//********************************************共用方法*************************************************************

    public static String getMd5(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            String md5 = calculateMD5(inputStream);
            return md5;
        } catch (IOException | NoSuchAlgorithmException e) {
            
        }

        return null;
    }

    /**
     * @param inputStream
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String calculateMD5(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        DigestInputStream dis = new DigestInputStream(inputStream, md);

        // 读取输入流中的数据，触发 MD5 计算
        byte[] buffer = new byte[4096];
        while (dis.read(buffer) != -1) {
            // 读取输入流的数据，不做任何处理
        }

        // 获取计算得到的 MD5 值
        byte[] md5Bytes = md.digest();

        // 将字节数组转换为十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Bytes) {
            sb.append(String.format("%02x", b));
        }

        // 返回 MD5 值的十六进制字符串表示
        return sb.toString();
    }


    static String basePost(String url, String json) {
        // 创建HttpClient实例
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建HttpPost实例
            HttpPost httpPost = new HttpPost(url);

            // 设置请求头信息
            httpPost.setHeader("Content-Type", "application/json");

            // 将JSON字符串设置为POST请求的实体
            StringEntity entity = new StringEntity(json);
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
            
        }
        return null;
    }

    private static final ExecutorService executor = new ThreadPoolExecutor(
            150, // 核心线程数
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

    /**
     * 递归遍历指定目录及其子目录中的所有文件
     *
     * @param folder 要遍历的目录
     */
    public static List<File> listFilesForFolder(File folder) {
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> arrayList = new ArrayList();

        if (listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    if(listOfFile.getName().equals(".DS_Store")){
                        continue;
                    }
                    arrayList.add(listOfFile);
                    // 如果是文件，则打印文件路径
                    System.out.println(listOfFile.getAbsolutePath());
                } else if (listOfFile.isDirectory()) {
                    // 如果是目录，则递归调用
//                    listFilesForFolder(listOfFile);
                }
            }
        } else {
            System.out.println("指定的路径不是一个目录，或者是一个无法访问的目录");
        }
        return arrayList;
    }

}