# springboot 项目
1、初始化项目（配置好nacos、调整分离文件内的数据库配置和初始化脚本配置、配置好分离文件application.yml）
2、启动service、启动getway
3、启动前端

依赖数据库和nacos配置中心 启动方式 java -jar xxx.jar -Dspring.config.additional-location=D:/environment/application.yml

#管理员帐号
登录名：sunyard
密码：123456


## 主要技术栈 

```
|--springboot
|--spring
|--mybatis-plus
|--maven
|--nacos
|--redis
|--ES
|--mysql
```

## 工程目录结构介绍

```
|--sunicms                                              父级
|   |--ecm                                                 影像业务
|   |--ecm-api                                             对外sdk
|   |--ecm-common                                          公用类
|   |--business-system-demo                                对外接口调用试例
```

## jar依赖关系介绍

```
|--ecm
|   |-spring-boot-starter-test                         springboot 测试包
|   |-spring-boot-starter-aop                          aop切面
|   |-lombok                                           lombok
|   |-mybatis-spring-boot-starter                      springboot的mybatis依赖包
|   |-mybatis-plus-boot-starter                        springboot的mybatis-plus依赖包
|   |-spring-boot-starter-freemarker                   主要用以mybatis-plus根据模板生成代码
|   |-pagehelper-spring-boot-starter                   分页工具类
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
|   |-commons-codec                                    常见的编码解码工具Base64、MD5、Hex、SHA1、DES等。
|   |-commons-lang3                                    常用高度重用的Util类
|   |-commons-lang                                     常用高度重用的Util类
|   |-fastjson                                         阿里json工具类
|   |-easy-captcha                                     视觉验证码
|   |-easyexcel                                        easyexcel导入导出

```````

## 业务系统项目  business-system-demo
```
business-system-demo 这个项目是模拟第三方业务系统对接sdk，具体操作试用流程见《对外接口文档说明》
```


## 新增移动端加密拍照上传相关功能
```
相关文件从放在app目录下（现只支持到android版本）
.apk文件为android安装包
showexif.html可直接再浏览器打开，用于查看图片的exif信息

其中自定以数据中包含md5及加密参数
例如：{"md5":"e5767dbd13a5c191ae795fefcf4eed96","sign":"Kk8zE7qXQNzY1OwJX6j9yHRjaX+gHWyow5g+RkDngWBGT8gNFKZarh00iFNHLIZpe+Z/zEtsuSsDY/mAUUcS8CRzlrQ/O8zurf5fSlJtQhEwV1h2WurvjObxjKvFUfBUNMVZMUhT2DYH2oDw0wPYX8jxRMbogCMC+QEAB9ybeStL6UCjRfSBmqq+5eXzKqAOEVmOJOq1RZI9ngmfszEb/LEXn5/GpFuSOi0Q4QsD9hUa5WfjRNVm9mAX6EFxR0XrnlmHye5mrDycdSACuS3iFXLQXnkJy5au6G4MKSw0/fMRe2ZVSwv/0AE/+TAWOnVXYJyEGbsWwJXVt5qD7w9PcA=="}
可根据这两个值判断图片的安全性

[RSADecryptor.java](ecm/src/main/java/com/sunyard/ecm/util/RSADecryptor.java)为解密工具类
private_key.pem私钥
public_key.pem公钥
如需要替换公钥私钥则需要重新打安装包才行。
操作步骤：安装apk，通过apk进行拍照，再从相册中将图片上传至影像（注意一定要保留exif信息，部分工具如微信上传后，会丢失exif信息），再影像页面中获取该exif信息，后通过解密获得对应的md5信息与文件的md5进行比对，如果一致则代表图片未被篡改。
```