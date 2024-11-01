# yunyi

#### Introduction

By incorporating data cache middleware services into management, the cloud native management platform of data caching middleware increases the  cloud service capabilities of data caching products, adapts to the  dynamic expansion, automatic deployment, automatic fault recovery, and  unified interface service capabilities required by the cloud platform,  and carries out cloud management of the original data cache nodes,  including monitoring and data operation of the caching service nodes. 

#### Software architecture

The following directories are included: 

1. rds-console

   The management console application is a Java web application based on the  SpringBoot architecture to monitor and manage data caches. See README.md for details

2. rds-proxy

   Solve the problem of node drift in the cloud environment through a unified  proxy service and provide a unified data access interface. It also  contains Dockerfiles related to self-made container images and reference definitions for K8S-related resources. See README.md for details

3. images

   The container image of TongRDS Enterprise Edition is a dependency of this project. 

#### Installation tutorial

For details, see the README.md files in the subdirectory 

#### Directions for use

For details, see the README.md files in the subdirectory 

#### Get involved

1. Fork this repository 
2. Create a new Feat_xxx branch 
3. Submit the code 
4. Create a new pull request 

#### Features

1. Use Readme_XXX.md to support different languages, such as Readme_en.md, Readme_zh.md 
2. Gitee Official Blog blog.gitee.com 
3. You can https://gitee.com/explore this address to learn about the best open source projects on Gitee
4. GVP stands for Gitee's Most Valuable Open Source Project, and it is an  excellent open source project that has been comprehensively evaluated
5. The official Gitee user manual https://gitee.com/help 
6. The Gitee Cover Character is a section that showcases Gitee members https://gitee.com/gitee-stars/ 