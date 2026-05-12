package com.sunyard.framework.mybatis.config;
/*
 * Project: com.sunyard.am.config
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.Date;
import java.util.Objects;
import java.util.Properties;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.sunyard.framework.mybatis.aop.PageClearAspect;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;

import cn.hutool.core.util.IdUtil;

/**
 * @author zhouleibin
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan("com.sunyard.**.mapper")
public class MybatisAutoConfiguration {
    /**
     *
     * @return
     */
    @Bean
    public SnowflakeUtils snowflakeUtil() {
        return SnowflakeUtils.build();
    }

    /**
     * 动态切换数据库，如在MySQL、PostgreSQL与Oracle之间进行切换，并使用同一个Dao接口。 如何进行同一个函数调用mapper中不同的sql语句？ 答案就是使用 DatabaseIdProvider 进行配置
     */
    @Bean
    public DatabaseIdProvider getDatabaseIdProvider() {
        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties p = new Properties();
        p.setProperty("MySQL", "mysql");
        p.setProperty("Oracle", "oracle");
        p.setProperty("DM DBMS", "dm");
        p.setProperty("PostgreSQL", "postgresql");
        p.setProperty("TiDB", "tidb");
        p.setProperty("DB2", "db2");
        p.setProperty("SQL Server", "sqlserver");
        databaseIdProvider.setProperties(p);
        return databaseIdProvider;
    }

    /**
     * 注入ID生成器
     * @return IdentifierGenerator
     */
    @Bean
    public IdentifierGenerator identifierGenerator() {
        return new IdentifierGenerator() {
            @Override
            public Long nextId(Object entity) {
                return IdUtil.getSnowflake().nextId();
            }
        };
    }

    /**
     * 自动填充参数类
     * @return MetaObjectHandler
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                Object createTime = getFieldValByName("createTime", metaObject);
                if (Objects.isNull(createTime)) {
                    setFieldValByName("createTime", new Date(), metaObject);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时间为空，则以当前时间为更新时间
                Object modifyTime = getFieldValByName("updateTime", metaObject);
                if (Objects.isNull(modifyTime)) {
                    setFieldValByName("updateTime", new Date(), metaObject);
                }
            }
        };
    }

    /**
     * 自动填充参数类
     * @return PageClearAspect
     */
    @Bean
    public PageClearAspect pageClearAspect() {
        return new PageClearAspect();
    }
}
