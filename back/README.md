# Sun ECM 企业影像管理系统

基于 Spring Cloud + Nacos 的企业级影像管理微服务系统，支持影像采集、OCR识别、验印、流水处理、反欺诈检测等核心功能。

## 项目结构

```
back/
├── cloud-model/          # 微服务底座框架
│   ├── sunyard-gateway/  # API网关
│   ├── sunyard-module/   # 公共模块
│   │   ├── sunyard-module-system/   # 系统服务（Nacos注册）
│   │   ├── sunyard-module-storage/  # 文件存储服务
│   │   ├── sunyard-module-auth/     # 认证服务
│   │   └── sunyard-module-cache/    # 缓存服务
│   └── sunyard-framework/   # 基础组件库
│       ├── sunyard-starter-mybatis/    # MyBatis starter
│       ├── sunyard-starter-redis/      # Redis starter
│       ├── sunyard-starter-ocr/        # OCR集成
│       ├── sunyard-starter-elasticsearch/  # ES搜索
│       ├── sunyard-starter-job/         # 定时任务
│       ├── sunyard-starter-message/     # 消息队列
│       ├── sunyard-video/               # 视频处理
│       ├── sunyard-ofd/                 # OFD文档
│       ├── sunyard-img/                 # 图像处理
│       └── sunyard-spire/               # Office文档
│
├── ecm-model/            # ecm业务模块
│   ├── ecm-common/       # 公共工具
│   ├── ecm-util/         # 业务工具
│   ├── ecm-api/          # API定义
│   └── ecm/              # 主业务实现
│
├── sunyard-sunafm/       # 反欺诈检测模块
│   ├── afm/              # 欺诈检测服务
│   └── afmcnn-python/    # CNN模型推理（Python独立部署）
│
├── sunyard-sunedm/        # EDM电子dm模块
│
└── SunMigr/              # 数据迁移工具
```

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 2.6 / Spring Cloud Alibaba |
| 注册/配置 | Nacos |
| ORM | MyBatis + MyBatis-Plus |
| 数据库 | Oracle |
| 缓存 | Redis |
| 搜索 | Elasticsearch 7.14 |
| 消息队列 | RabbitMQ |
| 实时通信 | WebSocket |
| 文档处理 | OFD / Office (spire) |
| OCR | imageai (Python OCR服务) |
| 验印 | imageai (Python印章检测) |

## 服务架构

```
外部请求
    ↓
gateway (路由 + 鉴权)
    ↓
ecm-service (主业务) ──→ storage-service (文件存储)
    │                        ├── Nacos注册
    │                        ├── MinIO/本地存储
    │                        └── FastDfs
    │
    ├──── afm-service (反欺诈) ──→ afmcnn-python (CNN推理)
    │         (Nacos注册)         (Docker独立部署)
    │
    └──── imageai (OCR/验印/篡改检测)
              (HTTP调用, Python Docker)
```

## 构建

```bash
# 完整构建
mvn clean package

# 跳过测试
mvn clean package -DskipTests

# 单模块构建
mvn -pl ecm-model/ecm clean package
```

## 开发说明

- Java 版本：**JDK 8**
- 构建工具：Maven 3.6+
- 代码规范：Alibaba P3C / Checkstyle（详见 `AGENTS.md`）
- 无 star import，禁止 System.out.println/TODO 注释
- public 类/方法必须写 Javadoc

## 注意事项

- `imageai/` 目录（OCR/验印 Python 模型，约 1.7GB）未包含在源码中，需单独部署
- `ecm-model/deploy/sql/` 数据库脚本需单独管理
- Oracle 数据库连接等敏感配置通过 Nacos 外部配置中心管理
