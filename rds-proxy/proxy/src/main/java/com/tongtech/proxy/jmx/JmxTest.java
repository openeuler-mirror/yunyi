package com.tongtech.proxy.jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.List;
import java.util.Map;

public class JmxTest {
    public static void main(String[] args) throws Exception {
//        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://192.168.0.60:29094/jmxrmi");
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:29094/jmxrmi");

        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

        //ObjectName的名称与前面注册时候的保持一致
        ObjectName mbeanName = new ObjectName("jmxBean:name=rdsCore");

        // 实例标识名
        String id = (String) mbsc.getAttribute(mbeanName, "Id");

        // 版本号
        String version = (String) mbsc.getAttribute(mbeanName, "Version");

        // 创建一个到Redis仿真端口的连接，并且发送PING命令测试服务是否正常
        // 执行此调用会增加一个Redis仿真端口的总连接数
        boolean isOk = (boolean) mbsc.getAttribute(mbeanName, "Ping");

        // jvm占用的总内存
        Long memoryTotal = (Long) mbsc.getAttribute(mbeanName, "MemoryTotal");

        // 储存数据占用的内存
        Long usedMemory = (Long) mbsc.getAttribute(mbeanName, "UsedMemory");

        // cfg配置文件中配置的内存大小
        Long memoryStatic = (Long) mbsc.getAttribute(mbeanName, "MemoryStatic");

        // 动态分配的超长数据的总内存大小
        Long memoryDynamic = (Long) mbsc.getAttribute(mbeanName, "MemoryDynamic");

        // 动态分配的超长数据的总个数
        Long sizeDynamic = (Long) mbsc.getAttribute(mbeanName, "SizeDynamic");

        // 系统可用内存
        Long totalPhysicalMemory = (Long) mbsc.getAttribute(mbeanName, "TotalPhysicalMemory");

        // 内存使用率
        Double memoryUsedRatio = (Double) mbsc.getAttribute(mbeanName, "MemoryUsedRatio");

        // jvm已经分配的内存
        Long jvmAllocated = (Long) mbsc.getAttribute(mbeanName, "JvmAllocated");

        // jvm已经分配但空闲的内存
        Long jvmFree = (Long) mbsc.getAttribute(mbeanName, "JvmFree");

        // jvm可分配的最大内存
        Long jvmMax = (Long) mbsc.getAttribute(mbeanName, "JvmMax");

        // 当前接入的client连接数量
        Long clientCurrentConnections = (Long) mbsc.getAttribute(mbeanName, "ClientCurrentConnections");

        // 可接入的client的最大连接数量
        Long maxConnections = (Long) mbsc.getAttribute(mbeanName, "MaxConnections");

        // 当前接入的client数量和最大接入数量的比值
        Double connectedRatio = (Double) mbsc.getAttribute(mbeanName, "ConnectedRatio");

        // 自启动以来创建过的客户端连接总数
        Long clientTotalConnections = (Long) mbsc.getAttribute(mbeanName, "ClientTotalConnections");

        // 客户端连接使用率
        Double connectionsRatio = (Double) mbsc.getAttribute(mbeanName, "ConnectionsRatio");

        // 当前每秒钟处理请求量
        Long processSecond = (Long) mbsc.getAttribute(mbeanName, "ProcessSecond");

        // 最近一分钟处理请求量
        Long processMinute = (Long) mbsc.getAttribute(mbeanName, "ProcessMinute");

        // 集群状态，list中的每1项（String数组）代表一个cluster的分片
        // String数组为定长，各项内容分别为：分片主节点名称、主节点地址、主节点状态、分片状态（success 或 fail）、分配槽位号列表
        List<String[]> clusters = (List<String[]>) mbsc.getAttribute(mbeanName, "ClusterStatus");

        // 各表中数据量
        Map<String, Integer> keys = (Map<String, Integer>) mbsc.getAttribute(mbeanName, "Keyspace");

        System.out.println("Id: " + id);
        System.out.println("Version = " + version);
        System.out.println("Ping: " + isOk);
        System.out.println("memoryTotal = " + memoryTotal);
        System.out.println("usedMemory = " + usedMemory);
        System.out.println("memoryUsedRatio = " + memoryUsedRatio);
        System.out.println("memoryStatic = " + memoryStatic);
        System.out.println("memoryDynamic = " + memoryDynamic);
        System.out.println("sizeDynamic = " + sizeDynamic);
        System.out.println("jvmAllocated = " + jvmAllocated);
        System.out.println("jvmUsed = " + (jvmAllocated - jvmFree));
        System.out.println("jvmFree = " + jvmFree);
        System.out.println("jvmMax = " + jvmMax);
        System.out.println("TotalPhysicalMemory = " + totalPhysicalMemory);
        System.out.println("clientCurrentConnections = " + clientCurrentConnections);
        System.out.println("maxConnections = " + maxConnections);
        System.out.println("connectedRatio = " + connectedRatio);
        System.out.println("clientTotalConnections = " + clientTotalConnections);
        System.out.println("connectionsRatio = " + connectionsRatio);
        System.out.println("processSecond = " + processSecond);
        System.out.println("processMinute = " + processMinute);

        if (keys != null) {
            for (String name : keys.keySet()) {
                System.out.println(name + ": " + keys.get(name));
            }
        }

        if (clusters != null) {
            System.out.println("Cluster enabled:");
            for (int i = 0; i < clusters.size(); i++) {
                System.out.print("  Node-" + i + ": ");
                String[] cluster = clusters.get(i);
                System.out.println(String.format("   %22s  %22s  %13s  %7s  %s"
                        , cluster[0], cluster[1], cluster[2], cluster[3], cluster[4]));
            }
        } else {
            System.out.println("Cluster support disabled");
        }
    }

    public static String makeupJMXServiceUrl(String ip, String rmiServerPort, int registerPort) {
        String ipAndPort0 = rmiServerPort == null ? "" : ip + ":" + rmiServerPort;
        String ipAndPort1 = ip + ":" + registerPort;
        return String.format("service:jmx:rmi://%s/jndi/rmi://%s/server", ipAndPort0, ipAndPort1);
    }
}
