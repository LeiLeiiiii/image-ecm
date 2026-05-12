package com.sunyard.framework.common.util.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author liugang
 * @Type com.sunyard.sunam.utils
 * @Desc
 * @date 13:41 2021/11/16
 */
@Slf4j
public class HttpUtils {
    // todo 很多地方用了http ，但是都没用这个工具类
    /**
     * httpclient连接池
     */
    private static PoolingHttpClientConnectionManager pcm;

    static {
        pcm = new PoolingHttpClientConnectionManager();
        // 整个连接池最大连接数
        pcm.setMaxTotal(50);
        // 每路由最大连接数，默认值是2
        pcm.setDefaultMaxPerRoute(50);
    }

    /**
     * http连接
     */
    private CloseableHttpClient httpClient = null;
    /**
     * 连接超时时间
     */
    private int connectTimeout = 120000;
    /**
     * 从连接池获取连接超时时间
     */
    private int connectionRequestTimeout = 10000;
    /**
     * 获取数据超时时间
     */
    private int socketTimeout = 300000;
    private String charset = "utf-8";
    /**
     * 请求配置
     */
    private RequestConfig requestConfig = null;
    /**
     * build requestConfig
     */
    private Builder requestConfigBuilder = null;
    private List<NameValuePair> nvps = new ArrayList<>();
    private List<Header> headers = new ArrayList<>();
    private String requestParam = "";

    /**
     * 默认设置
     *
     * @author Liu Jiong
     * @createDate 2016年10月30日
     */
    private static HttpUtils defaultInit() {
        HttpUtils httpUtils = new HttpUtils();
        if (httpUtils.requestConfig == null) {
            httpUtils.requestConfigBuilder = RequestConfig.custom().setConnectTimeout(httpUtils.connectTimeout)
                .setConnectionRequestTimeout(httpUtils.connectionRequestTimeout)
                .setSocketTimeout(httpUtils.socketTimeout);
            httpUtils.requestConfig = httpUtils.requestConfigBuilder.build();
        }
        return httpUtils;
    }

    /**
     * 初始化 httpUtil
     */
    public static HttpUtils init() {
        HttpUtils httpUtils = defaultInit();
        if (httpUtils.httpClient == null) {
            httpUtils.httpClient = HttpClients.custom().setConnectionManager(pcm).build();
        }
        return httpUtils;
    }

    /**
     * 初始化 httpUtil
     * @param paramMap 参数map
     * @return httpUtils
     */
    public static HttpUtils init(Map<String, String> paramMap) {
        HttpUtils httpUtils = init();
        httpUtils.setParamMap(paramMap);
        return httpUtils;
    }

    /**
     * 验证初始化
     * @param ip 地址
     * @param port 端口
     * @param username 账号
     * @param password 密码
     * @return httpUtils
     */
    public static HttpUtils initWithAuth(String ip, int port, String username, String password) {
        HttpUtils httpUtils = defaultInit();
        if (httpUtils.httpClient == null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(ip, port, AuthScope.ANY_REALM),
                new UsernamePasswordCredentials(username, password));
            httpUtils.httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider)
                .setConnectionManager(pcm).build();
        }
        return httpUtils;
    }

    /**
     * from请求
     *
     * @param url 请求url
     * @param params 参数map
     * @return Result
     */
    public static String form(String url, Map<String, String> params) {
        URL u = null;
        HttpURLConnection con = null;
        // 构建请求参数
        StringBuffer sb = new StringBuffer();
        if (params != null) {
            for (Entry<String, String> e : params.entrySet()) {
                sb.append(e.getKey());
                sb.append("=");
                sb.append(e.getValue());
                sb.append("&");
            }
            sb.substring(0, sb.length() - 1);
        }
        // 尝试发送请求
        try {
            u = new URL(url);
            con = (HttpURLConnection)u.openConnection();
            //// POST 只能为大写，严格限制，post会不识别
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            osw.write(sb.toString());
            osw.flush();
            osw.close();
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        // 读取返回内容
        StringBuffer buffer = new StringBuffer();
        BufferedReader br = null;
        try {
            // 一定要有返回值，否则无法把请求发送给server端。
            if (con != null) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            }
            String temp;
            while ((temp = br.readLine()) != null) {
                buffer.append(temp);
                buffer.append("\n");
            }
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }

        return buffer.toString();
    }

    /**
     * 设置请求头
     * @param name header的key
     * @param value header的值
     * @return HttpUtils
     */
    public HttpUtils setHeader(String name, String value) {
        Header header = new BasicHeader(name, value);
        headers.add(header);
        return this;
    }

    /**
     * 设置请求头
     * @param headerMap 请求头map
     * @return HttpUtils
     */
    public HttpUtils setHeaderMap(Map<String, String> headerMap) {
        for (Entry<String, String> param : headerMap.entrySet()) {
            Header header = new BasicHeader(param.getKey(), param.getValue());
            headers.add(header);
        }
        return this;
    }

    /**
     * 设置请求头
     * @param name 参数的key
     * @param value 参数的值
     * @return HttpUtils
     */
    public HttpUtils setParam(String name, String value) {
        nvps.add(new BasicNameValuePair(name, value));
        return this;
    }

    /**
     * 设置请求头
     * @param paramMap 参数map
     * @return HttpUtils
     */
    public HttpUtils setParamMap(Map<String, String> paramMap) {
        for (Entry<String, String> param : paramMap.entrySet()) {
            nvps.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        return this;
    }

    /**
     * 设置字符串参数
     * @param requestParam 请求参数
     * @return HttpUtils
     */
    public HttpUtils setStringParam(String requestParam) {
        this.requestParam = requestParam;
        return this;
    }

    /**
     * 设置连接超时时间
     * @param connectTimeout 超时时间
     * @return HttpUtils
     */
    public HttpUtils setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        this.requestConfigBuilder = requestConfigBuilder.setConnectTimeout(connectTimeout);
        requestConfig = requestConfigBuilder.build();
        return this;
    }

    /**
     * http get 请求
     * @param url 请求地址
     * @return 返参map
     */
    public Map<String, String> get(String url) {
        Map<String, String> resultMap = new HashMap<>(12);
        // 获取请求URI
        URI uri = getUri(url);
        if (uri != null) {
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);
            if (!CollectionUtils.isEmpty(headers)) {
                Header[] header = new Header[headers.size()];
                httpGet.setHeaders(headers.toArray(header));
            }

            // 执行get请求
            try {
                CloseableHttpResponse response = httpClient.execute(httpGet);
                return getHttpResult(response, url, httpGet, resultMap);
            } catch (Exception e) {
                httpGet.abort();
                resultMap.put("result", e.toString());
                log.error("获取http GET请求返回值失败 url======" + url, e);
                throw new RuntimeException(e);
            }
        }
        return resultMap;
    }

    /**
     * http post 请求
     * @param url 请求地址
     * @return 返参map
     */
    public Map<String, String> post(String url) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        if (!CollectionUtils.isEmpty(headers)) {
            Header[] header = new Header[headers.size()];
            httpPost.setHeaders(headers.toArray(header));
        }
        if (!CollectionUtils.isEmpty(nvps)) {
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));
            } catch (UnsupportedEncodingException e) {
                log.error("http post entity form error", e);
                throw new RuntimeException(e);
            }
        }
        if (StringUtils.hasText(requestParam)) {
            try {
                httpPost.setEntity(new StringEntity(requestParam, charset));
            } catch (UnsupportedCharsetException e) {
                log.error("http post entity form error", e);
                throw new RuntimeException(e);
            }
        }
        Map<String, String> resultMap = new HashMap<>(12);
        // 执行post请求
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            return getHttpResult(response, url, httpPost, resultMap);
        } catch (Exception e) {
            httpPost.abort();
            resultMap.put("result", e.getCause().getMessage());
            log.error("获取http POST请求返回值失败 url======" + url, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * post 上传文件
     * @param url 请求地址
     * @param fileParam 文件map
     * @return 返参map
     */
    public Map<String, String> postUploadFile(String url, Map<String, File> fileParam) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        if (!CollectionUtils.isEmpty(headers)) {
            Header[] header = new Header[headers.size()];
            httpPost.setHeaders(headers.toArray(header));
        }

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        if (fileParam != null) {
            for (Entry<String, File> entry : fileParam.entrySet()) {
                // 将要上传的文件转化为文件流
                FileBody fileBody = new FileBody(entry.getValue());
                // 设置请求参数
                builder.addPart(entry.getKey(), fileBody);
            }
        }

        if (!CollectionUtils.isEmpty(nvps)) {
            for (NameValuePair nvp : nvps) {
                String value = nvp.getValue();
                if (StringUtils.hasText(value)) {
                    builder.addTextBody(nvp.getName(), value, ContentType.create("text/plain", charset));
                }
            }
        }
        httpPost.setEntity(builder.build());
        Map<String, String> resultMap = new HashMap<>(6);
        // 执行post请求
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            return getHttpResult(response, url, httpPost, resultMap);
        } catch (Exception e) {
            httpPost.abort();
            resultMap.put("result", e.toString());
            log.error("获取http postUploadFile 请求返回值失败 url======" + url, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取请求返回值
     * @param response 响应头
     * @param url 请求地址
     * @param request 请求头
     * @param resultMap 返参数map
     * @return 返参数map
     */
    private Map<String, String> getHttpResult(CloseableHttpResponse response, String url, HttpUriRequest request,
        Map<String, String> resultMap) {
        String result = "";
        int statusCode = response.getStatusLine().getStatusCode();
        resultMap.put("statusCode", statusCode + "");
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                result = EntityUtils.toString(entity, charset);
                // 释放连接
                EntityUtils.consume(entity);
            } catch (Exception e) {
                request.abort();
                log.error("获取http请求返回值解析失败", e);
                throw new RuntimeException(e);
            }
        }
        if (statusCode != 200) {
            result = "HttpClient status code :" + statusCode + "  request url===" + url;
            log.info("HttpClient status code :" + statusCode + "  request url===" + url);
            request.abort();
        }
        resultMap.put("result", result);
        return resultMap;
    }

    /**
     * 获取重定向url返回的location
     * @param url 请求url
     * @return String
     */
    public String redirectLocation(String url) {
        String location = "";
        // 获取请求URI
        URI uri = getUri(url);
        if (uri != null) {
            HttpGet httpGet = new HttpGet(uri);
            // 设置自动重定向false
            requestConfig = requestConfigBuilder.setRedirectsEnabled(false).build();
            httpGet.setConfig(requestConfig);
            if (!CollectionUtils.isEmpty(headers)) {
                Header[] header = new Header[headers.size()];
                httpGet.setHeaders(headers.toArray(header));
            }

            try {
                // 执行get请求
                CloseableHttpResponse response = httpClient.execute(httpGet);
                int statusCode = response.getStatusLine().getStatusCode();
                // 301 302
                if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                    Header header = response.getFirstHeader("Location");
                    if (header != null) {
                        location = header.getValue();
                    }
                }
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    EntityUtils.consume(entity);
                }
            } catch (Exception e) {
                httpGet.abort();
                log.error("获取http GET请求获取 302 Location失败 url======" + url, e);
                throw new RuntimeException(e);
            }
        }
        return location;
    }

    /**
     * 获取输入流
     * @param url 请求url
     * @return 输入流
     */
    public InputStream getInputStream(String url) {
        // 获取请求URI
        URI uri = getUri(url);
        if (uri != null) {
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);
            if (!CollectionUtils.isEmpty(headers)) {
                Header[] header = new Header[headers.size()];
                httpGet.setHeaders(headers.toArray(header));
            }
            // 执行get请求
            try {
                CloseableHttpResponse response = httpClient.execute(httpGet);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    log.info("HttpClient status code :" + statusCode + "  request url===" + url);
                    httpGet.abort();
                } else {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream in = entity.getContent();
                        return in;
                    }
                }
            } catch (Exception e) {
                httpGet.abort();
                log.error("获取http GET inputStream请求失败 url======" + url, e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * 请求url
     * @param url 请求地址
     * @return URI
     */
    private URI getUri(String url) {
        URI uri = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (!CollectionUtils.isEmpty(nvps)) {
                uriBuilder.setParameters(nvps);
            }
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            log.error("url 地址异常", e);
            throw new RuntimeException(e);
        }
        return uri;
    }

}
