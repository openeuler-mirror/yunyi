package com.tongtech.probe;

import com.tongtech.probe.stat.StatCenterNode;
import com.tongtech.probe.stat.StatLicense;
import com.tongtech.probe.stat.StatSentinelNode;
import com.tongtech.probe.stat.StatService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestCenterClientTests {

    //private RestCenterClient client = new RestCenterClient("http://localhost:8086/");
    private RestCenterClient client = new RestCenterClient("http://192.168.0.90:8806/", "abcef12376se790fowieaawew90qa8ew8e", true);


    @Test
    @Order(1)
    void testServices() throws IOException {
        List<StatService> res = client.getServices().getListData();
        System.out.println(res);

        Assertions.assertTrue(res.size() >= 2);
        Assertions.assertEquals(res.get(0).getType(), "SCALABLE");
        Assertions.assertTrue(res.get(0).getStatistics().getCpuSystemLoad() >= 0);
        Assertions.assertTrue(res.get(0).getNodes().size() >= 2);
        Assertions.assertTrue(res.get(0).getNodes().get(0).getInstance() != null);
        Assertions.assertTrue(res.get(0).getNodes().get(0).getRunning() >= 10L);
        Assertions.assertTrue(res.get(0).getNodes().get(0).getUsed().get(0).getUsed() >= 0L);
        Assertions.assertEquals(res.get(0).getProxies().size(), 2);

        System.out.println("getProxies : " + res.get(0).getProxies());
    }


    @Test
    @Order(2)
    void testSentinels() throws IOException {
        List<StatSentinelNode> res =  client.getSentinels().getListData();
        System.out.println(res);

        Assertions.assertEquals(res.size(), 2);
        Assertions.assertEquals(res.get(0).getRemote(), "192.168.0.90");
        Assertions.assertTrue(res.get(0).getInstance() != null);
        Assertions.assertTrue(res.get(0).getRuntime().getCpuSystemLoad() >= 0F);

    }

    @Test
    @Order(3)
    void testLicenseUsing() throws IOException {
        StatLicense res = client.getLicenseUsing().getData();
        System.out.println(res);

        Assertions.assertEquals(res.getCode(), 200);
        Assertions.assertEquals(res.getMsg(), "ok");
        Assertions.assertEquals(res.getPercent(), "0.0%");
    }


    @Test
    @Order(4)
    void testCenters() throws IOException {
        List<StatCenterNode> res = client.getCenters().getListData();
        System.out.println(res);

        Assertions.assertEquals(res.size(), 3);
        Assertions.assertTrue(res.get(0).getInstance() != null);
        Assertions.assertEquals(res.get(0).getRemote(), "192.168.0.90");
        Assertions.assertTrue(res.get(0).getRuntime().getCpuSystemLoad() >= 0F);
    }

}
