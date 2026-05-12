package com.sunyard.ecm.config;

import com.sunyard.ecm.api.EcmApi;
import com.sunyard.ecm.api.EcmApiImpl;
import com.sunyard.ecm.constant.ApiConstants;
import com.sunyard.framework.oauth.SunApiClient;

/**
 * @author pjw
 * @Description 创建鉴权实例工厂对象配置类
 * @since 2024/2/22 10:56
 */
public class EcmApiFactoryConfig {

    private static EcmApiImpl ecmApiObj;

    private static SunApiClient sunApiClient;
    private static Integer key = 4;

    public static EcmApi getEcmApi(String version) {
        if (null == ecmApiObj) {
            synchronized (key) {
                //由于可能多个线程都进入getEcmApi了,由于锁定机制，一个线程进入该代码块时，其他线程
                //仍在排队进入该代码块，如果不做判断，当前线程即使创造了实例，下一个线程也不知道，就会继续创建一个实例
                if (null == ecmApiObj) {
                    ecmApiObj = new EcmApiImpl();
                }
            }
        }
        if (version.equals(ApiConstants.VERSION)) {
            return ecmApiObj;
        } else {
            return ecmApiObj;
        }
    }

    public static SunApiClient getSunApiClient(String appId, String appSecret, String baseIp) {
        //先判断该sunApiClient变量是否为空
        if (null == sunApiClient) {
            synchronized (key) {
                //由于可能多个线程都进入getSunApiClient了,由于锁定机制，一个线程进入该代码块时，其他线程
                //仍在排队进入该代码块，如果不做判断，当前线程即使创造了实例，下一个线程也不知道，就会继续创建一个实例
                if (null == sunApiClient) {
                    sunApiClient = new SunApiClient(appId, appSecret, baseIp);
                }
            }
        }
        return sunApiClient;
    }
}
