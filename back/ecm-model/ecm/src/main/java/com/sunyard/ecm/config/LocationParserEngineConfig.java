package com.sunyard.ecm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;
import tech.spiro.addrparser.io.RegionDataInput;
import tech.spiro.addrparser.parser.LocationParserEngine;
import tech.spiro.addrparser.parser.ParserEngineException;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 位置解析引擎服务
 *
 */
@Slf4j
@Configuration
public class LocationParserEngineConfig implements InitializingBean {

    /**
     * 位置解析引擎
     */
    private LocationParserEngine engine;

    /**
     * 使用单线程可重用线程池
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * javax.sql.DataSource 数据源
     */
    @Resource
    private DataSource dataSource;

    private static final String TABLE_NAME = "ecm_area_geodata";


    public LocationParserEngine getEngine() {
        return engine;
    }


    @Override
    public void afterPropertiesSet() {
        RegionDataInput regionDataInput = new OracleRdbmsRegionDataInput(dataSource, TABLE_NAME);
        // 创建并初始化位置解析引擎，一般配置为全局单例
        engine = new LocationParserEngine(regionDataInput);
        StopWatch sw = new StopWatch("初始化位置解析引擎");
        sw.start();
        // 初始化，加载数据，比较耗时
        // 另外开启一个线程，防止阻塞主线程
        executorService.submit(() -> {
            try {
                engine.init();
            } catch (ParserEngineException e) {
                log.error("初始化位置解析引擎异常：", e);
            }
        });

        sw.stop();
        log.info(sw.shortSummary());
    }
}
