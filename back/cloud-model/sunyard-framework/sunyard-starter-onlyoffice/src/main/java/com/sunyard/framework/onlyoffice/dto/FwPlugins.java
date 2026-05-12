package com.sunyard.framework.onlyoffice.dto;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author 朱山成
 */

@Data
@Configuration
public class FwPlugins implements Serializable {
    @Value("${onlyoffice.editor.plugins.autostart:}")
    /** 插件的 guid  asc.{4FF5B2DB-BDDA-CC2A-5A36-0087719EB455} */
    private String[] autostart;
    @Value("${onlyoffice.editor.plugins.pluginsData:}")
    /** 插件地址  服务器地址+guid+/config.json  这的guid没有前缀 {4FF5B2DB-BDDA-CC2A-5A36-0087719EB455}*/
    private String[] pluginsData;


}
