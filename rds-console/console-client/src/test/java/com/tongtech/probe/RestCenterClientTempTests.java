package com.tongtech.probe;

import com.tongtech.probe.stat.StatCenterNode;
import com.tongtech.probe.stat.StatLicense;
import com.tongtech.probe.stat.StatSentinelNode;
import com.tongtech.probe.stat.StatService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestCenterClientTempTests {

    //private static String client_url = "http://s0.v100.vip:33519";
    private static String client_url = "http://192.168.0.86:8086/";

    private static RestCenterClient client = new RestCenterClient(client_url,
            "abcef12376se790fowieaawew90qa8ew8e", true);

    @Test
    @Order(1)
    void testServices() throws IOException {
        List<StatService> res = client.getServices().getListData();
        System.out.println(res);

        Assertions.assertTrue(res.size() >= 1);
    }


    @Test
    @Order(2)
    void testSentinels() throws IOException {
        List<StatSentinelNode> res =  client.getSentinels().getListData();
        System.out.println(res);

        Assertions.assertEquals(res.size(), 1);
        Assertions.assertEquals(res.get(0).getRemote(), "192.168.0.86");
        Assertions.assertTrue(res.get(0).getInstance() != null);
    }

    @Test
    @Order(3)
    void testLicenseUsing() throws IOException {
        StatLicense res = client.getLicenseUsing().getData();
        System.out.println(res);

        Assertions.assertEquals(res.getCode(), 200);
        Assertions.assertEquals(res.getMsg(), "ok");
    }


    @Test
    @Order(4)
    void testCenters() throws IOException {
        List<StatCenterNode> res = client.getCenters().getListData();
        System.out.println(res);

        Assertions.assertTrue(res.size() >= 1);
        Assertions.assertTrue(res.get(0).getInstance() != null);
        Assertions.assertEquals(res.get(0).getRemote(), "192.168.0.86");
    }

}
