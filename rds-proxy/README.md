# 前言

TongRDS开源云原生组件的proxy模块运行时需要先安装TongRDS企业版（2.2.1.4版本以上），TongRDS企业版配合proxy模块实现TongRDS的云原生模式运行。

# TongRDS企业版安装

TongRDS企业版安装分成Center中心节点安装和Memdb服务节点安装2部分，Center中心节点负责管理软件授权使用、控制服务节点运行模式、实时监控服务节点运行状态、收集服务节点运行指标等。



## 配置安装

TongRDS通过创建ConfigMap资源实现软件配置，安装TongRDS节点实例前需要先创建对应的ConfigMap资源。

1、采用如下yaml文件内容创建命令空间（源码参见deploy目录下的“namespace.yaml”文件）：

```
#
# kubectl create -f namespace.yaml
#
apiVersion: v1
kind: Namespace
metadata:
  name: rds

```

2、采用如下yaml文件内容创建ConfigMap资源配置例如（源码参见deploy目录下的“config.yaml”文件）：

```yaml
# kubectl -n rds apply -f config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tong-rds-center
data:
  center.k8s.service: "rds-center"
  active.7e4f22b2931f320ab01e2745ab62e1ea07a8d1e4b7c8f4b53dc6dc2e7f327c7c: "*"
  license.signature: "nsqgusegwukdl6bwwntn0ptxp4og0k2lsgz11zmnc8xqdaembwtutidn6bpgh1lx5mke1kptr09ypnll4jxqrti1sug0nkgcm4uoxze2wxrxv3k5035rrq3ig74mrhsr3chq87j9br9yrx5g7aiqqam8z7cp1drgtk5k1yo9did61tvl78udpam3dvwgg3uqqahue80dcpraogtkoejzxum6a1sov5ieh38n23yq5y39k9csqb7fk06ujm0fjnvjkakaoq46pd1iaf647wqyu2pte8ctmmy9g2dr52729yo82pce27jm5wmzm8bsiu0v2pk6l0aqiko5qj8rzc8jv5e7brit03ssitx2p4ghba50nuo01dyzrazzaghf8z96hzrpa881fabg4lqwgiocxpd4hyvn2pt8r7vjd4hsbkkmc4xgdx6wbr83vp8m0yll0m3gak0kljq2vagog19o8014kk78wi7os7od1b0gx7cu03su1lqal160urp01vhrgyxy304mez54rxshbx491g2iuef5am9ig1i84l6eh906ot2j6wqu6zksbiuol2jtp6cgqg1guflee9a8r3eq6fly3w4xttwqkobmjgghjo32grfbgyzqopggmbqendqtaxwmfh82rg7d7hwujym264fztttq4np9akhchmhlutx2x4ke3pz98t28wn4jh79cn7fug931p1lg2zgqfgqfaavk33090hkcgk7jxahrns8ja0kjyos1l3533p48saat5871ku09skth7b93raibuak7"
  license.information: "656e64757365723a20e4b89ce696b9e9809a0a70726f6a6563743a20524453e4ba91e58e9fe7949fe7bb84e4bbb6e58c85e5bc80e6ba90e9a1b9e79bae0a70726f647563743a20546f6e675244532d434e2056322e3220e4ba91e58e9fe7949fe4bc81e4b89ae789880a63617061636974793a20e68e88e69d83203132386720e58685e5ad98e4bdbfe794a80a6c6963656e7365547970653a20e4ba91e58e9fe7949fe4bc81e4b89ae78988e4b8b4e697b6e8af81e4b9a62031383020e5a4a9e68e88e69d830ae5a597e695b0efbc9a31e5a59720283129"
  config.server.scalable.movements_times: "4"
  config.server.platform: "k8s"
  config.server.platform_service: "rds-center"
  cluster.WebSession.type: "scalable"
  cluster.WebSession.replicas: "2"
  cluster.WebSession.hot_spare: "2"
  cluster.WebSession.max_slice: "1024"

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: tong-rds-node
data:
  cfg.Server.Common.Service: "WebSession"
  cfg.Server.Tables: "1"

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: tong-rds-proxy
data:
  proxy.Server.Common.Service: "WebSession"
  proxy.Server.Listen.RedisPassword: "1234"
  proxy.Server.Listen.RedisPlainPassword: "true"

```

创建成功后应能查询到如下3个新创建的ConfigMap：

```sh
[root@master ~]# kubectl -n rds get configmap
NAME                     DATA   AGE
tong-rds-center          14     263d
tong-rds-node            3      263d
tong-rds-proxy           3      263d
```



## Center中心节点安装

1、下载TongRDS企业版的中心节点镜像。TongRDS企业版提供x86芯片和arm芯片的镜像下载，也可参考dockerfile目录中的“Dockerfile-center-x86”文件利用TongRDS的绿色版自行制作中心节点镜像。

2、将镜像上传到k8s镜像库。

3、通过yaml文件创建中心节点，yaml文件例如（参考deploy目录下的“center.yaml”文件）：

​		注意：“image: 192.168.0.89:80/library/tongrds-center:2.2.1.4”需更换为k8s实际镜像地址

```yaml
#
# kubectl -n rds apply -f center.yaml
# kubectl -n rds scale statefulset rdscenter --replicas 5
#
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: rdscenter
spec:
  serviceName: rds-center
  replicas: 3
  selector:
    matchLabels:
      app: rdscenter
  template:
    metadata:
      labels:
        app: rdscenter
      annotations:
        pod.alpha.kubernetes.io/initialized: "true"
    spec:
      affinity:
        podAntiAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            - labelSelector:
                matchExpressions:
                  - key: "app"
                    operator: In
                    values:
                      - "rdscenter"
              topologyKey: "kubernetes.io/hostname"
      terminationGracePeriodSeconds: 300
      containers:
        - name: k8srdscenter
          lifecycle:
            preStop:
              exec:
                command: ["./StopCenter.sh"]
          imagePullPolicy: Always
          image: 192.168.0.89:80/library/tongrds-center:2.2.1.4
          ports:
          - containerPort: 8086
            name: server
          - containerPort: 6300
            name: service
          - containerPort: 26379
            name: sentinel
          readinessProbe:
            tcpSocket:
              port: 8086
            initialDelaySeconds: 5
            periodSeconds: 5
            successThreshold: 1

          env:
            - name: POD_NAME
              valueFrom:
                fieldRef:
                  fieldPath: metadata.name
            - name: POD_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
            - name: POD_IP
              valueFrom:
                fieldRef:
                  apiVersion: v1
                  fieldPath: status.podIP
            - name: CENTER_SERVICE
              valueFrom:
                configMapKeyRef:
                  name: tong-rds-center
                  key: center.k8s.service
            - name: CONFIG_VOLUME_PATH
              value: "/projected-volume"
          volumeMounts:
            - name: config
              mountPath: /projected-volume
      volumes:
        - name: config
          configMap:
            name: tong-rds-center
                  
      imagePullSecrets:
        - name: registry-pull-secret

---
apiVersion: v1
kind: Service
metadata:
  name: rds-center
  labels:
    app: rds-center
  annotations:
    service.alpha.kubernetes.io/tolerate-unready-endpoints: "true"

spec:
  type: ClusterIP
  clusterIP: None
  ports:
  - port: 8086
    name: server
    targetPort: 8086
  - port: 6300
    name: service
    targetPort: 6300
  - port: 26379
    name: sentinel
    targetPort: 26379
  selector:
    app: rdscenter

```

创建成功后新增3个中心节点的pod如下：

```shell
[root@master rds-k8s]# kubectl -n rds get pod
NAME           READY   STATUS    RESTARTS   AGE
rdscenter-0    1/1     Running   0          83d
rdscenter-1    1/1     Running   0          23s
rdscenter-2    1/1     Running   0          2m5s
。。。。。。
```

检查中心节点pod日志有“Center start.”字样说明启动成功：

```shell
[root@master rds-k8s]# kubectl -n rds logs rdscenter-0
Load '/opt/pcenter/etc/config.properties' ok.
Found license from volume:
    license.signature: nsqgusegwukdl6bwwntn0ptxp4og0k2lsgz11zmnc8xqdaembwtutidn6bpgh1lx5mke1kptr09ypnll4jxqrti1sug0nkgcm4uoxze2wxrxv3k5035rrq3ig74mrhsr3chq87j9br9yrx5g7aiqqam8z7cp1drgtk5k1yo9did61tvl78udpam3dvwgg3uqqahue80dcpraogtkoejzxum6a1sov5ieh38n23yq5y39k9csqb7fk06ujm0fjnvjkakaoq46pd1iaf647wqyu2pte8ctmmy9g2dr52729yo82pce27jm5wmzm8bsiu0v2pk6l0aqiko5qj8rzc8jv5e7brit03ssitx2p4ghba50nuo01dyzrazzaghf8z96hzrpa881fabg4lqwgiocxpd4hyvn2pt8r7vjd4hsbkkmc4xgdx6wbr83vp8m0yll0m3gak0kljq2vagog19o8014kk78wi7os7od1b0gx7cu03su1lqal160urp01vhrgyxy304mez54rxshbx491g2iuef5am9ig1i84l6eh906ot2j6wqu6zksbiuol2jtp6cgqg1guflee9a8r3eq6fly3w4xttwqkobmjgghjo32grfbgyzqopggmbqendqtaxwmfh82rg7d7hwujym264fztttq4np9akhchmhlutx2x4ke3pz98t28wn4jh79cn7fug931p1lg2zgqfgqfaavk33090hkcgk7jxahrns8ja0kjyos1l3533p48saat5871ku09skth7b93raibuak7
    license.information: 656e64757365723a20e4b89ce696b9e9809a0a70726f6a6563743a20524453e4ba91e58e9fe7949fe7bb84e4bbb6e58c85e5bc80e6ba90e9a1b9e79bae0a70726f647563743a20546f6e675244532d434e2056322e3220e4ba91e58e9fe7949fe4bc81e4b89ae789880a63617061636974793a20e68e88e69d83203132386720e58685e5ad98e4bdbfe794a80a6c6963656e7365547970653a20e4ba91e58e9fe7949fe4bc81e4b89ae78988e4b8b4e697b6e8af81e4b9a62031383020e5a4a9e68e88e69d830ae5a597e695b0efbc9a31e5a59720283129
CenterService start at 6300
Server successfully joined the group(16)
SentinelService start at 26379
Center start.
RestServer start at 8086

```



## Memdb服务节点安装

1、下载TongRDS企业版的服务节点镜像。TongRDS企业版提供x86芯片和arm芯片的镜像下载，也可参考dockerfile目录中的“Dockerfile-node-x86”文件利用TongRDS的绿色版自行制作服务节点镜像。

2、将镜像上传到k8s镜像库。

3、通过yaml文件创建服务节点，yaml文件例如（参考deploy目录下的“memdb.yaml”文件）：

​		注意：“image: 192.168.0.89:80/library/tongrds-node:2.2.1.4”需更换为k8s实际镜像地址

```yaml
# kubectl -n rds apply -f memdb.yaml
# kubectl -n rds scale statefulset rdsmemdb --replicas 8
# kubectl -n rds scale statefulset rdsmemdb --replicas 5
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: rdsmemdb
spec:
  serviceName: rds-memdb
  replicas: 6
  selector:
    matchLabels:
      app: rdsmemdb
  template:
    metadata:
      labels:
        app: rdsmemdb
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 1
              podAffinityTerm:
                labelSelector:
                  matchExpressions:
                    - key: "app"
                      operator: In
                      values:
                      - rdsmemdb
                topologyKey: "kubernetes.io/hostname"
      terminationGracePeriodSeconds: 300
      containers:
        - name: rdsmemdb
          lifecycle:
            preStop:
              exec:
                command: ["./StopServer.sh"]
          imagePullPolicy: Always
          image: 192.168.0.89:80/library/tongrds-node:2.2.1.4
          ports:
            - containerPort: 6200
              name: service
            - containerPort: 6379
              name: simulation
          readinessProbe:
            tcpSocket:
              port: 6379
            initialDelaySeconds: 10
            periodSeconds: 5
            successThreshold: 1
            timeoutSeconds: 1
          env:
            - name: POD_NAME
              valueFrom:
                fieldRef:
                  fieldPath: metadata.name
            - name: POD_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
            - name: POD_IP
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
            - name: NODE_NAME
              valueFrom:
                fieldRef:
                  fieldPath: spec.nodeName
            - name: CENTER_SERVICE
              valueFrom:
                configMapKeyRef:
                  name: tong-rds-center
                  key: center.k8s.service
            - name: CONFIG_VOLUME_PATH
              value: "/projected-volume"
          volumeMounts:
            - name: config
              mountPath: /projected-volume
      volumes:
        - name: config
          configMap:
            name: tong-rds-node

      imagePullSecrets:
        - name: registry-pull-secret


```

创建成功后新增6个服务节点的pod如下：

```shell
[root@master rds-k8s]# kubectl -n rds get pod
NAME           READY   STATUS    RESTARTS   AGE
rdscenter-0    1/1     Running   0          60m
rdscenter-1    1/1     Running   0          61m
rdscenter-2    1/1     Running   0          63m
rdsmemdb-0     1/1     Running   0          22m
rdsmemdb-1     1/1     Running   0          22m
rdsmemdb-2     1/1     Running   0          21m
rdsmemdb-3     1/1     Running   0          21m
rdsmemdb-4     1/1     Running   0          21m
rdsmemdb-5     1/1     Running   0          21m
```

检查服务节点pod日志有“Server started.”字样说明启动成功，“Get authorization from the Center”字样说明已经成功从中心节点获得授权许可：

```shell
[root@master rds-k8s]# kubectl -n rds logs rdsmemdb-0
load config-file '/opt/pmemdb/etc/cfg.xml' ok.

Server belonging to 'WebSession' is starting...
Memory cache create ok.
Start listening to port 6200
Start listening to port 6379

Server started.
Get authorization from the Center

```



## Proxy代理节点安装

proxy代理节点为开源模块，从源码库下载源码、编译后制作镜像文件，然后发布安装。安装步骤如下：

1、下载TongRDS-proxy模块源码。

2、使用idea以项目方式打开TongRDS-proxy源码，右侧工具条执行Gradle->Tasks->Other->tar，执行成功后在工程主目录下的proxy->build->distributions->TongRDS-2.2.1.4.Proxy.tar.gz文件即为proxy的安装包。

3、复制上述步骤2生成的安装包并解压到合适的目录，参考如下配置制作镜像（dockerfile源码参见dockerfile目录中的“Dockerfile-proxy-x86”文件）：

注意：ubuntu原始包中不包含java运行环境，需要自行打包java运行环境。

```dockerfile
FROM ubuntu
MAINTAINER TongRDS
COPY ./java-se-8u/jre/ /usr/lib/jvm/java-8-openjdk/
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk
ENV PATH=${JAVA_HOME}/bin:$PATH
ENV TZ=Asia/Shanghai
COPY ./proxy/ /opt/proxy/
WORKDIR /opt/proxy/bin
EXPOSE 6200 6379
ENTRYPOINT "./StartProxy.sh"

```

4、参照如下yaml文件内容部署proxy应用（yaml源码在deploy目录下的“proxy.yaml”文件）：

```yaml
# 
# kubectl -n rds apply -f proxy.yaml
# kubectl -n rds scale StatefulSet rdsproxy --replicas 2
#
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: rdsproxy
spec:
  serviceName: rds-proxy
  replicas: 2
  template:
    metadata:
      labels:
        app: rdsproxy
      annotations:
        pod.alpha.kubernetes.io/initialized: "true"
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 1
              podAffinityTerm:
                labelSelector:
                  matchExpressions:
                    - key: "app"
                      operator: In
                      values:
                      - rdsproxy
                topologyKey: "kubernetes.io/hostname"
      containers:
        - name: k8srdsproxy
          lifecycle:
            preStop:
              exec:
                command: ["./StopProxy.sh"]
          imagePullPolicy: Always
          image: 192.168.0.89:80/library/tongrds-proxy:2.2.1.4
          ports:
            - containerPort: 6200
              name: rds
            - containerPort: 6379
              name: simulation
          readinessProbe:
            tcpSocket:
              port: 6379
            initialDelaySeconds: 5
            periodSeconds: 3
            successThreshold: 1
            timeoutSeconds: 1
          env:
            - name: POD_NAME
              valueFrom:
                fieldRef:
                  fieldPath: metadata.name
            - name: POD_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
            - name: POD_IP
              valueFrom:
                fieldRef:
                  apiVersion: v1
                  fieldPath: status.podIP
            - name: CENTER_SERVICE
              valueFrom:
                configMapKeyRef:
                  name: tong-rds-center
                  key: center.k8s.service
            - name: CONFIG_VOLUME_PATH
              value: "/projected-volume"
          volumeMounts:
            - name: config
              mountPath: /projected-volume
      volumes:
        - name: config
          configMap:
            name: tong-rds-proxy

      imagePullSecrets:
        - name: registry-pull-secret

  selector:
    matchLabels:
      app: rdsproxy

---
apiVersion: v1
kind: Service
metadata:
  name: rds-proxy
  labels:
    app: proxyservice
spec:
  type: ClusterIP
  ports:
  - port: 6379
    name: redis
    targetPort: 6379
  selector:
    app: rdsproxy


```

创建成功后新增3个代理节点的pod如下：

```shell
[root@master rds-k8s]# kubectl -n rds get pod
NAME           READY   STATUS    RESTARTS   AGE
rdscenter-0    1/1     Running   0          79m
rdscenter-1    1/1     Running   0          81m
rdscenter-2    1/1     Running   0          82m
rdsmemdb-0     1/1     Running   0          41m
rdsmemdb-1     1/1     Running   0          41m
rdsmemdb-2     1/1     Running   0          41m
rdsmemdb-3     1/1     Running   0          40m
rdsmemdb-4     1/1     Running   0          40m
rdsmemdb-5     1/1     Running   0          40m
rdsproxy-0     1/1     Running   0          44d
rdsproxy-1     1/1     Running   0          44d

```

检查proxy代理节点pod日志有“Proxy started.”字样说明启动成功：

```
[root@master rds-k8s]# kubectl -n rds logs rdsproxy-0
load config-file '/opt/proxy/etc/proxy.xml' ok.
Start listening to port 6200
Start listening to port 6379

Proxy started.
```

5、参考以下yaml文件内容创建NodePort服务为k8s外的应用提供访问能力（yaml源码参见deploy目录下的“proxy_nodeport.yaml”文件）：

```yaml
---
# ------------------- App NodePort ------------------ #
apiVersion: v1
kind: Service
metadata:
  name: proxy-nodeport
spec:
  type: NodePort
  ports:
    - port: 6379
      name: simulation
      targetPort: 6379
      nodePort: 30279
  selector:
    app: rdsproxy

```


