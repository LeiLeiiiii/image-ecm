package com.sunyard.framework.message.util;

import javax.annotation.Resource;

import org.dromara.email.api.MailClient;
import org.dromara.email.comm.config.MailSmtpConfig;
import org.dromara.email.core.factory.MailFactory;
import org.springframework.stereotype.Component;

import com.sunyard.framework.message.config.properties.MailProperties;

/**
 * @author P-JWei
 * @date 2023/7/5 9:20
 * @title 邮箱
 * @description
 */
@Component
public class EmailUtils {

    @Resource
    private MailProperties mailProperties;

    /**
     * 邮箱注册
     *
     * @return Result
     */
    public MailClient getMailClient() {
        MailSmtpConfig config = MailSmtpConfig.builder()
            .password(mailProperties.getPassword()).username(mailProperties.getUsername()).isAuth(mailProperties.getIsAuth())
            .isSSL(mailProperties.getIsSSL()).port(mailProperties.getPort()).smtpServer(mailProperties.getSmtpServer())
            .fromAddress(mailProperties.getFromAddress()).build();
        MailFactory.put("xyd", config);
        return MailFactory.createMailClient("xyd");
    }
}
