# yunyi

#### 介绍
云翼数据缓存中间件云原生管理平台通过将数据缓存中间件服务纳入管理，增加数据缓存产品的云服务能力，适应云平台需要具备的动态扩展、自动部署、故障自动恢复、统一接口服务能力，对原数据缓存节点进行云化管理，包括对缓存服务节点的监控、数据操作等功能。

#### 软件架构
包含以下目录：

1. rds-console

   管理控制台应用，基于springboot架构的java web应用，对数据缓存进行监控、数据管理。详见[README.md](rds-console/README.md)

2. rds-proxy

   通过统一的代理服务解决云环境下的节点漂移问题，提供统一的数据访问接口。同时包含自制容器镜像相关dockerfile，k8s相关资源参考定义。详见[README.md](rds-proxy/README.md)

3. images

   TongRDS企业版容器镜像，为本项目依赖项。


#### 安装教程

详见子目录下的README.md文件

#### 使用说明

详见子目录下的README.md文件

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特色

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
