## 🐶 新手必读

* 演示地址：
* 启动文档：

## 🐨 技术栈

### 工程目录结构介绍

```
Sunyard
├── sunyard-dependencies                         Maven 依赖版本管理
└── sunyard-framework                            Java 框架拓展
   ├── sunyard-common                            定义基础 pojo 类、枚举、工具类等
   ├── sunyard-img                               图片处理工具
   ├── sunyard-ofd                               国产板式文件处理工具包
   ├── sunyard-img                               图片处理工具
   ├── sunyard-monitor                           springboot admin监控、数据库文档
   ├── sunyard-starter-job                       定时任务，基于 quartZ 实现，支持集群模式
   ├── sunyard-starter-mybatis                   数据库操作，基于 MyBatis Plus 实现
   ├── sunyard-starter-redis                     缓存操作，基于 Spring Data Redis，Lock4J缓存锁
   ├── sunyard-starter-log                       基于Tlog分布式日志框架，aop异步处理处理日志
   ├── sunyard-starter-rpc                       服务调用，基于openFeign实现，开发环境隔离
   └── sunyard-spire                             spire供应商工具包
├── sunyard-gateway                              网关
└── sunyard-module                               XXX 功能的 Module 模块
   └── sunyard-module-auth                       认证服务
      ├── sunyard-module-auth-api                认证服务-rpc接口包
      └── sunyard-module-auth-biz                认证服务-实现
   └── sunyard-module-storage                    存储服务
      ├── sunyard-module-storage-api             存储服务-rpc接口包
      └── sunyard-module-storage-biz             存储服务-实现：多种存储方式实现oss、minio、ftp、nas、S3、缓存、压缩、加密、特殊处理
   └── sunyard-module-system                     系统服务
      ├── sunyard-module-system-api              系统服务--rpc接口包
      └── sunyard-module-system-biz              系统服务-实现：组织、用户、角色、菜单、字典、系统管理、基础配置、监控、定时任务管理
```````


