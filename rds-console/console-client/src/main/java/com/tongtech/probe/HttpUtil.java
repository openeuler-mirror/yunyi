package com.tongtech.probe;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class HttpUtil {

    private final static int STATUS_ERROR = 400;

    //实例化一个HttpClient
    private final static HttpClient httpClient = HttpClientBuilder.create().build();

    private final static RequestConfig requestConfig;

    static {
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setConnectionRequestTimeout(90000);
        builder.setConnectTimeout(5000); //
        builder.setMaxRedirects(2);
        requestConfig = builder.build();
    }

    public static String postMessage(String url, String entityData, Header... headers) throws IOException {
        return postMessageResult(url, entityData, headers).getEntity();
    }

    public static ResponseResult postMessageResult(String url, String entityData, Header... headers) throws IOException {
        HttpPost post = new HttpPost(url);

        if(headers != null) {
            for(Header h : headers) {
                post.addHeader(h);
            }
        }

        ResponseResult result = null;
        try {
            post.setConfig(requestConfig);
            Header header = new BasicHeader("Accept-Encoding", null);
            post.setHeader(header);

            post.setEntity(new StringEntity(entityData, ContentType.APPLICATION_JSON));

            HttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() < STATUS_ERROR) {//请求成功
                //取得请求内容
                HttpEntity entity = response.getEntity();

                result = new ResponseResult(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
                //目前仅保存request中传入同名的Header值
                for(Header h : headers) {
                    String hName = h.getName();
                    Header[] resHeaders = response.getHeaders(hName);
                    if(resHeaders != null) {
                        String[] values = new String[resHeaders.length];
                        for(int i=0 ; i<resHeaders.length ; i++) {
                            values[i] = resHeaders[i].getValue();
                        }
                        result.addHeader(hName, values);
                    }
                }

            } else {
                throw new IOException("http response " + response.getStatusLine().getStatusCode() + ": " + response.getStatusLine().getReasonPhrase());
            }
        }
        finally {
            post.releaseConnection();
        }

        return result;
    }

    public static String getMessage(String url, Header... headers) throws IOException {
        ResponseResult res = getMessageResult(url, headers);
        return res.getEntity();
    }

    public static ResponseResult getMessageResult(String url, Header... headers) throws IOException {
        HttpGet get = new HttpGet(url);

        for(Header h : headers) {
            get.setHeader(h);
        }

        ResponseResult result = null;
        try {
            get.setConfig(requestConfig);

            Header header = new BasicHeader("Accept-Encoding", null);
            get.setHeader(header);

            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() < STATUS_ERROR) {//请求成功
                //取得请求内容
                HttpEntity entity = response.getEntity();

                result = new ResponseResult(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
                //目前仅保存request中传入同名的Header值
                for(Header h : headers) {
                    String hName = h.getName();
                    Header[] resHeaders = response.getHeaders(hName);
                    if(resHeaders != null) {
                        String[] values = new String[resHeaders.length];
                        for(int i=0 ; i<resHeaders.length ; i++) {
                            values[i] = resHeaders[i].getValue();
                        }
                        result.addHeader(hName, values);
                    }
                }

            } else {
                throw new IOException("http response " + response.getStatusLine().getStatusCode() + ": " + response.getStatusLine().getReasonPhrase());
            }
        }
        finally {
            get.releaseConnection();
        }

        return result;
    }

    public static String uploadFile(String url, File file, Header... headers) throws IOException {
        return uploadFileResult(url, file).getEntity();
    }

    public static ResponseResult uploadFileResult(String url, File file, Header... headers) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        ResponseResult result = null;
        try {
            HttpPost httpPost = new HttpPost(url);

            for(Header h : headers) {
                httpPost.setHeader(h);
            }

            //HttpMultipartMode.RFC6532参数的设定是为避免文件名为中文时乱码
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
            //httpPost.addHeader("header1", "111");//头部放文件上传的head可自定义

            String fileName = file.getName();
            builder.addBinaryBody("file", file, ContentType.MULTIPART_FORM_DATA, fileName);
            //builder.addTextBody("params1", "1");//其余参数，可自定义
            //builder.addTextBody("params2", "2");
            HttpEntity postEntity = builder.build();
            httpPost.setEntity(postEntity);
            response = httpClient.execute(httpPost);// 执行提交

            if (response.getStatusLine().getStatusCode() < STATUS_ERROR) {//请求成功
                //取得请求内容
                HttpEntity responseEntity = response.getEntity();

                result = new ResponseResult(response.getStatusLine().getStatusCode(), EntityUtils.toString(responseEntity));
                //目前仅保存request中传入同名的Header值
                for(Header h : headers) {
                    String hName = h.getName();
                    Header[] resHeaders = response.getHeaders(hName);
                    if(resHeaders != null) {
                        String[] values = new String[resHeaders.length];
                        for(int i=0 ; i<resHeaders.length ; i++) {
                            values[i] = resHeaders[i].getValue();
                        }
                        result.addHeader(hName, values);
                    }
                }

            } else {
                throw new IOException("http response " + response.getStatusLine().getStatusCode() + ": " + response.getStatusLine().getReasonPhrase());
            }
        }finally {//处理结束后关闭httpclient的链接
            httpClient.close();
        }
        return result;
    }


    private final static String getFileFromUrl(String url) {
        if (url == null) {
            return null;
        }

        // 去掉url后面的query部分
        int para = url.lastIndexOf('?');
        if (para > 0) {
            url = url.substring(0, para);
        }

        int start = url.lastIndexOf('/');
        if (start < 0) {
            return null;
        } else if (start + 1 == url.length()) {
            return "";
        }
        return url.substring(start + 1);
    }

    /**
     * HttpResponse中的信息对象
     */
    public static class ResponseResult {
        private int statusCode;
        private String entity;
        private Map<String, String[]> headers;
        private String[] values;

        public ResponseResult() {
            this.headers = new HashMap<String, String[]>();
        }

        public ResponseResult(int statusCode, String entity) {
            this.statusCode = statusCode;
            this.headers = new HashMap<String, String[]>();
            this.entity = entity;
        }

        public void addHeader(String name, String[] value) {
            headers.put(name, value);
        }

        public String[] getHeaderValues(String name) {
            return headers.get(name);
        }

        public String getHeaderValue(String name) {
            String[] values = headers.get(name);
            if(values != null && values.length > 0) {
                return values[0];
            }
            else {
                return null;
            }
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getEntity() {
            return entity;
        }


        @Override
        public String toString() {
            return "ResponseResult{" +
                    "statusCode=" + statusCode +
                    ", entity='" + entity + '\'' +
                    ", headers=" + toStringHeaders() +
                    '}';
        }

        private String toStringHeaders() {
            StringBuilder buf = new StringBuilder();
            buf.append('{');
            for(String name : headers.keySet()) {
                String[] values = headers.get(name);
                buf.append(name).append('=').append(Arrays.toString(values)).append(',');
            }
            buf.append('}');
            return buf.toString();
        }
    }

}
