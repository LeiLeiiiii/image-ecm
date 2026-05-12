# springboot 项目
1、初始化项目（配置好nacos、调整分离文件内的数据库配置和初始化脚本配置、配置好分离文件application.yml）
2、启动service、启动getway
3、启动前端

依赖数据库和nacos配置中心 启动方式 java -jar xxx.jar -Dspring.config.additional-location=D:/environment/application.yml

登录名：sunam
密码：!Sunyard123456


## 主要技术栈 

```
|--maven
|--springboot
|--shiro
|--nacos
|--dubbo
|--redis
|--activiti
```

## 工程目录结构介绍

```
|--SunAM                                              父级
|   |--SunAM-dao                                           数据层
|   |--SunAM-common                                        基础通用工具
|   |--SunAM-gateway                                       web门户
|   |--SunAM-service                                       业务层
|   |--SunAM-service-api                                   业务层对外接口
```

## jar依赖关系介绍

```
|--SunAM
|   |-spring-boot-starter-test                         springboot 测试包
|   |-spring-boot-starter-aop                          aop切面
|   |-spring-retry                                     重试框架
|   |-lombok                                           lombok
--------------------------------------------------------------------
|--SunAM-dao
|   |-mybatis-spring-boot-starter                      springboot的mybatis依赖包
|   |-mybatis-plus-boot-starter                        springboot的mybatis-plus依赖包
|   |-mybatis-plus-generator                           mybatis-plus的代码生成器
|   |-spring-boot-starter-freemarker                   主要用以mybatis-plus根据模板生成代码
|   |-pagehelper-spring-boot-starter                   分页工具类
|   |-ojdbc8                                           oracle-jdbc驱动包
|   |-orai18n                                          oracle中文乱码处理
--------------------------------------------------------------------
|--SunAM-common
|   |--SunAM-dao
|   |-spring-boot-starter-web                          
|   |-spring-boot-starter-cache                        缓存注解
|   |-spring-boot-starter-data-redis                   Redis连接池+RedisTemplate
|   |-commons-pool2                                    lettuce 依赖pool2
|   |-spring-cloud-starter-alibaba-nacos-config        springboot的nacos注册中心
|   |-spring-cloud-starter-alibaba-nacos-discovery     springboot的nacos配置中心
|   |-spring-cloud-starter-dubbo                       springboot的dubbo
|   |-spring-cloud-starter-sleuth                      分布式链路追踪
|   |-brave-instrumentation-dubbo                      dubbo集成sleuth
|   |-shiro-spring                                     shiro权限框架
|   |-shiro-redis                                      重新封装的shiro集成redis工具包
|   |-httpmime                                         HttpClient工具包
|   |-httpclient
|   |-commons-codec                                    常见的编码解码工具Base64、MD5、Hex、SHA1、DES等。
|   |-commons-lang3                                    常用高度重用的Util类
|   |-commons-lang                                     常用高度重用的Util类
|   |-fastjson                                         阿里json工具类
|   |-easy-captcha                                     视觉验证码
|   |-easyexcel                                        easyexcel导入导出
--------------------------------------------------------------------
|--SunAM-service-api
|   |--SunAM-common
--------------------------------------------------------------------
|--SunAM-service
|   |--SunAM-service-api
|   |-activiti-spring-boot-starter                      activiti流程引擎
--------------------------------------------------------------------
|--SunAM-gateway
|   |--SunAM-service-api
```````


