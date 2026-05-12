# springboot 项目
1、初始化项目（配置好nacos、调整分离文件内的数据库配置和初始化脚本配置、配置好分离文件application.yml）
2、启动service、启动getway
3、启动前端

依赖数据库和nacos配置中心 启动方式 java -jar xxx.jar -Dspring.config.additional-location=D:/environment/application.yml

登录名：sunam
密码：!Sunyard123456

## 工程目录结构介绍

```
|--sunafm                                           父级
|   |--afm                                          反欺诈应用（java）
|   |--afmcnn                                       大模型应用（python）
|   |--aunyard-module-afm-api                       业务层对外接口
```


# 主要内容
```
1、首页
2、检测记录
3、在线检测
4、系统管理
5、忘记密码
6、登陆用户拥有角色展示
```
# python项目
```
1、cd到afmCnn所在文件夹
2、执行 uvicorn afmCnn:app --reload --host=0.0.0.0 --port=9210(python3.12 -m uvicorn afmCnn:app --reload --host=0.0.0.0 --port=9210)
3、访问http://xxx:9210/docs
4、清华镜像 -i https://pypi.tuna.tsinghua.edu.cn/simple
```