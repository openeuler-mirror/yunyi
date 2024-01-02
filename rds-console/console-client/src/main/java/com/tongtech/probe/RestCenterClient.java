package com.tongtech.probe;

import com.alibaba.fastjson2.JSON;
import com.tongtech.probe.config.ProbeConstants;
import com.tongtech.probe.stat.StatCenterNode;
import com.tongtech.probe.stat.StatLicense;
import com.tongtech.probe.stat.StatSentinelNode;
import com.tongtech.probe.stat.StatService;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class RestCenterClient {

    private HttpUtil util;

    private String loginUrl;

    private String servicesUrl;

    private String licenseUrl;

    private String sentinelsUrl;

    private String centersUrl;

    private String commandUrl;

    private File fileBase; //测试时，临时保存报文路径

    private boolean testing;

    private String authKey;

    private String nextAuthKey;

    public RestCenterClient(String baseUrl) {
        this(baseUrl, null, false);
    }

    public RestCenterClient(String baseUrl, String authKey, boolean testing) {
        this.util = new HttpUtil();
        String bUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

        this.loginUrl = bUrl + "login";
        this.servicesUrl = bUrl + "service";
        this.licenseUrl = bUrl + "license";
        this.sentinelsUrl =  bUrl + "sentinel";
        this.centersUrl = bUrl + "center";

        this.commandUrl = bUrl + "command";
        this.testing = testing;
        this.authKey = authKey;
        this.nextAuthKey = null;
        if(testing == true) {
            this.fileBase = new File("temp").getAbsoluteFile();
        }
    }

//    public RestCenterResult<StatService> getServices(String authKey) throws IOException {
//
//
//        long beginTime = System.currentTimeMillis();
//        HttpUtil.ResponseResult response = HttpUtil.getMessageResult(this.servicesUrl, new BasicHeader(AUTH_KEY_NAME, authKey));
//        if(testing == true) { printInfo("services", response.getEntity());}
//        List<StatService> res = JSON.parseArray(response.getEntity(), StatService.class);
//        for(StatService s : res) {   s.reprocessNodes();  } //重新处理一下节点的附加属性
//        long endTime = System.currentTimeMillis();
//        return new RestCenterResult<StatService>(response.getEntity(), res, new Date(beginTime), endTime - beginTime, response.getHeaderValue("U-Auth-Key"));
//    }

    public RestCenterResult<StatService> getServices() throws IOException {
        long beginTime = System.currentTimeMillis();
        String json = getMessage("services", this.servicesUrl);
        List<StatService> res = JSON.parseArray(json, StatService.class);
        for(StatService s : res) {   s.reprocessNodes();  } //重新处理一下节点的附加属性
        long endTime = System.currentTimeMillis();
        return new RestCenterResult<StatService>(json, res, new Date(beginTime), endTime - beginTime);
    }


    public RestCenterResult<StatLicense> getLicenseUsing() throws IOException {
        long beginTime = System.currentTimeMillis();
        String json = getMessage("licenseUsing", this.licenseUrl);
        StatLicense res =  JSON.parseObject(json, StatLicense.class);
        long endTime = System.currentTimeMillis();
        return new RestCenterResult<StatLicense>(json, res, new Date(beginTime), endTime - beginTime);
    }

    public RestCenterResult<StatSentinelNode> getSentinels() throws IOException {
        long beginTime = System.currentTimeMillis();
        String json = getMessage("sentinels", this.sentinelsUrl);
        List<StatSentinelNode> res =  JSON.parseArray(json, StatSentinelNode.class);
        long endTime = System.currentTimeMillis();
        return new RestCenterResult<StatSentinelNode>(json, res, new Date(beginTime), endTime - beginTime);
    }


    public RestCenterResult<StatCenterNode> getCenters() throws IOException {
        long beginTime = System.currentTimeMillis();
        String json = getMessage("centers", this.centersUrl);
        List<StatCenterNode> res = JSON.parseArray(json, StatCenterNode.class);
        long endTime = System.currentTimeMillis();
        return new RestCenterResult<StatCenterNode>(json, res, new Date(beginTime), endTime - beginTime);
    }


    private synchronized String getMessage(String printType, String url) throws IOException {
        String json;
        if(this.authKey == null) {
            //非认证方式来获取信息
            json = HttpUtil.getMessage(url);
        }
        else {
            //以认证方式来获取信息
            try {
                Header authHeader = new BasicHeader(ProbeConstants.AUTH_KEY_NAME, getNextAuthKey());
                HttpUtil.ResponseResult res = HttpUtil.getMessageResult(url, authHeader);
                this.nextAuthKey = res.getHeaderValue(ProbeConstants.AUTH_KEY_NAME);
                json = res.getEntity();
            }
            catch (IOException ioe) {//如果发生异常，可能是nextAuthKey已经失效，尝试重新登录并获取
                this.nextAuthKey = null;
                Header authHeader = new BasicHeader(ProbeConstants.AUTH_KEY_NAME, getNextAuthKey());
                HttpUtil.ResponseResult res = HttpUtil.getMessageResult(url, authHeader);
                this.nextAuthKey = res.getHeaderValue(ProbeConstants.AUTH_KEY_NAME);
                json = res.getEntity();
            }
        }

        if(testing == true) { printInfo(printType, json); }
        return json;
    }

    private String getNextAuthKey() throws IOException {
        //如果 this.nextAuthKey 是空，进行重新登录，并获取this.nextAuthKey
        if(this.nextAuthKey == null) {
            Header authHeader = new BasicHeader(ProbeConstants.AUTH_KEY_NAME, this.authKey);
            HttpUtil.ResponseResult res = HttpUtil.getMessageResult(this.loginUrl, authHeader);
            this.nextAuthKey = res.getHeaderValue(ProbeConstants.AUTH_KEY_NAME);
        }

        return this.nextAuthKey;
    }

    /**
     * 用于测试时把报文信息打印出来并保存到控制台。
     * @param name
     * @param data
     */
    private void printInfo(String name, String data) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + name + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(data);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~ END \n");

        File file = new File(fileBase, name + ".json");
        try {
            FileUtils.writeStringToFile(file, data,"UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
