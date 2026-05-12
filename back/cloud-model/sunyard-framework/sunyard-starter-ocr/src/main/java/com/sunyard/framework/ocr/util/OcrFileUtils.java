package com.sunyard.framework.ocr.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

/**
 * @author PJW
 */
@Slf4j
public class OcrFileUtils {

    /**
     * 获取文件base6
     *
     * @param url url
     * @return String
     */
    public static String getBase64(String url) {
        String result = "";
        // 转成Base64
        InputStream httpsFile = null;
        try {
            httpsFile = getHttpsFile(url);
            byte[] bytes = IOUtils.toByteArray(httpsFile);
            result = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("Base64加密异常:",e);
        }
        return result;
    }

    /**
     * 获取文件流
     *
     * @param fileUrl 文件url
     * @return InputStream
     * @throws NoSuchProviderException  异常
     * @throws NoSuchAlgorithmException 异常
     * @throws IOException              异常
     * @throws KeyManagementException   异常
     */
    public static InputStream getHttpsFile(String fileUrl)
            throws NoSuchProviderException, NoSuchAlgorithmException, IOException, KeyManagementException {
        // 创建SSLContext对象，使用指定的信任管理器初始化
        if (fileUrl.contains("https:")) {
            TrustManager[] tm = {new MyX509TrustManagerUtils()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(fileUrl);
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setSSLSocketFactory(ssf);
            httpsConn.setDoInput(true);
            httpsConn.setDoOutput(true);
            return httpsConn.getInputStream();
        } else {// http
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10 * 1000);
            return connection.getInputStream();
        }
    }

}
