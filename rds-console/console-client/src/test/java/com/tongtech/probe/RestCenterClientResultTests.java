package com.tongtech.probe;

import com.alibaba.fastjson2.JSON;
import com.tongtech.probe.stat.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;

import static com.tongtech.probe.ProbeClientTestUtils.loadResource;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestCenterClientResultTests {

    @Test
    @Order(1)
    void testServices() throws IOException {
        String json = ProbeClientTestUtils.loadResource("json/services.json");
        List<StatService> res =  JSON.parseArray(json, StatService.class);
        for(StatService s : res) {   s.reprocessNodes();  } //重新处理一下节点的附加属性

        System.out.println(res);
        Assertions.assertTrue(res.size() >= 2);
        Assertions.assertEquals(res.get(0).getType(), "SCALABLE");
        Assertions.assertTrue(res.get(0).getStatistics().getCpuSystemLoad() > 0);
        Assertions.assertTrue(res.get(0).getNodes().size() >= 2);
        Assertions.assertTrue(res.get(0).getNodes().get(0).getRunning() >= 10L);

        StatWorkerNode node0 = res.get(0).getNodes().get(0);
        System.out.println("node0:" + node0);
        Assertions.assertEquals(0.9, node0.getThroughput().getAverage10());
        Assertions.assertEquals(node0.getMaster(), true);
        Assertions.assertEquals(node0.getShard(), 0);
        Assertions.assertEquals(node0.getSlot(), "0-614");
        Assertions.assertEquals(node0.isHotSpares(), false);
        Assertions.assertEquals(node0.getSecureLevel(), 0);

        StatProxyNode proxy0 = res.get(0).getProxies().get(0);
        System.out.println("proxy0:" + proxy0);
        Assertions.assertEquals(proxy0.getSecureLevel(), 2);



        Assertions.assertEquals(res.get(0).getNodes().get(4).isHotSpares(), true);
        Assertions.assertEquals(res.get(0).getNodes().get(5).isHotSpares(), true);


        Assertions.assertTrue(res.get(0).getNodes().get(0).getUsed().get(0).getUsed() >= 0L);

        StatService clusterServ = res.get(2);
        System.out.println("clusterServ.getShard():" +  clusterServ.getShard());
        Assertions.assertEquals(clusterServ.getType(), "CLUSTER");
        Assertions.assertEquals(clusterServ.getShard().size(), 2);

        StatWorkerNode node_c_0 = clusterServ.getNodes().get(0);
        System.out.println("node_c_0：" + node_c_0);
        Assertions.assertEquals(0, node_c_0.getShard());
        Assertions.assertEquals("0-8499", node_c_0.getSlot());

        StatWorkerNode node_c_3 = clusterServ.getNodes().get(3);
        System.out.println("node_c_3：" + node_c_3);
        Assertions.assertEquals(1, node_c_3.getShard());
        Assertions.assertEquals("8500-16383", node_c_3.getSlot());


    }


    @Test
    @Order(2)
    void testSentinels() throws IOException {
        String json = ProbeClientTestUtils.loadResource("json/sentinels.json");
        List<StatSentinelNode> res =  JSON.parseArray(json, StatSentinelNode.class);
        System.out.println(res);

        Assertions.assertEquals(res.size(), 2);
        Assertions.assertEquals(res.get(0).getRemote(), "192.168.0.90");
        Assertions.assertTrue(res.get(0).getRuntime().getCpuSystemLoad() > 0D);

        StatSentinelNode sentinel0 = res.get(0);
        Assertions.assertEquals(sentinel0.getServices().size(), 1);
        Assertions.assertEquals(sentinel0.getGroup(), "62C2AB3B4C19236B7686");
        StatSentinelNode.StatEndPoint[] endPoints = sentinel0.getServices().get("WebSession");
        Assertions.assertNotNull(endPoints);
        Assertions.assertEquals(endPoints.length, 3);
        Assertions.assertEquals(endPoints[0].getEndPoint(), "192.168.0.90:7379");
        Assertions.assertEquals(endPoints[0].getHost(), "192.168.0.90");
        Assertions.assertEquals(endPoints[0].getPort(), 7379);
        Assertions.assertEquals(endPoints[0].getMaster(), true);
        Assertions.assertEquals(endPoints[0].getAlive(), true);

        System.out.println("sentinels 0 getCpuSystemLoad:" + res.get(0).getRuntime().getCpuSystemLoad());

    }

    @Test
    @Order(3)
    void testLicenseUsing() throws IOException {
        String json = ProbeClientTestUtils.loadResource("json/licenseUsing.json");
        StatLicense res =  JSON.parseObject(json, StatLicense.class);
        System.out.println(res);

        Assertions.assertEquals(res.getCode(), 200);
        Assertions.assertEquals(res.getMsg(), "ok");
        Assertions.assertEquals(res.getPercent(), "0.0%");
    }


    @Test
    @Order(4)
    void testCenters() throws IOException {
        String centersJson = ProbeClientTestUtils.loadResource("json/centers.json");
        StatCenterNode[] centers;
        List<StatCenterNode> res = JSON.parseArray(centersJson, StatCenterNode.class);
        System.out.println(res);

        Assertions.assertEquals(res.size(), 3);
        Assertions.assertEquals(res.get(0).getRemote(), "192.168.0.90");
        Assertions.assertTrue(res.get(0).getRuntime().getCpuSystemLoad() > 0D);
    }



}
