package com.tongtech.console.enums;


/**
 * Center 配置
 this.deployableFiles.put("vmoptions", new DeployableFile("vmoptions", new File(this.parent, "bin/external.vmoptions")));
 this.deployableFiles.put("cfg", new DeployableFile("cfg", new File(this.parent, "etc/config.properties")));
 this.deployableFiles.put("sync", new DeployableFile("sync", new File(this.parent, "etc/sync.properties")));
 this.deployableFiles.put("cluster", new DeployableFile("cluster", new File(this.parent, "etc/cluster.properties")));
 this.deployableFiles.put("active", new DeployableFile("active", new File(this.parent, "etc/active.properties")));
 this.deployableFiles.put("acl", new DeployableFile("acl", new File(this.parent, "etc/acl.properties")));
 this.deployableFiles.put("lic", new DeployableFile("lic", new File(this.parent, "center.lic")));

 * Worker 配置
 this.deployableFiles.put("vmoptions", new DeployableFile("vmoptions", new File(this.parent, "bin/external.vmoptions")));
 this.deployableFiles.put("cfg", new DeployableFile("cfg", new File(this.parent, "etc/cfg.xml")));
 this.deployableFiles.put("dynamic", new DeployableFile("dynamic", new File(this.parent, "etc/dynamic.xml")));
 this.deployableFiles.put("lic", new DeployableFile("lic", new File(this.parent, "license.dat")));

 * Sentinel 配置
 this.deployableFiles.put("cfg", new DeployableFile("cfg", new File(this.parent, "etc/sentinel.xml")));


 */
public enum TemplateTypeEnum {
    WORKER_CFG("worker-cfg", "工作节点-配置"),
    WORKER_DYNAMIC("worker-dynamic", "工作节点-动态配置"),
    SENTINEL_CFG("sentinel-cfg", "哨兵节点-配置"),
    PROXY_CFG("proxy-cfg", "代理节点-配置"),
    CENTER_CFG("center-cfg", "中心节点-配置"),
    CENTER_CLUSTER("center-cluster", "中心节点-部署管理配置"),
    CENTER_SYNC("center-sync", "中心节点-同步配置"),
    CENTER_ALC("center-alc", "中心节点-用户访问控制");


    private String name;

    private String info;


    TemplateTypeEnum(String name, String info) {
        this.name = name;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public String getInfo() { return info;  }

    public static TemplateTypeEnum parse(String name) {
        if(WORKER_CFG.name.equals(name)) {
            return WORKER_CFG;
        }
        else if(WORKER_DYNAMIC.name.equals(name)) {
            return WORKER_DYNAMIC;
        }
        else if(SENTINEL_CFG.name.equals(name)) {
            return SENTINEL_CFG;
        }
        else if(PROXY_CFG.name.equals(name)) {
            return PROXY_CFG;
        }
        else if(CENTER_CFG.name.equals(name)) {
            return CENTER_CFG;
        }
        else if(CENTER_CLUSTER.name.equals(name)) {
            return CENTER_CLUSTER;
        }
        else if(CENTER_SYNC.name.equals(name)) {
            return CENTER_SYNC;
        }
        else if(CENTER_ALC.name.equals(name)) {
            return CENTER_ALC;
        }
        else {
            return null;
        }

    }

}
