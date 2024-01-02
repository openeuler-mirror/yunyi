package com.tongtech.probe;


import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * ProbeClient测试工具类。
 * 提供功能：加载classpath中资源文件，启动和停止节点通用方法。
 */
public class ProbeClientTestUtils {

    /**
     * 获得classpath中的资源文件的路径，返回资源文件(java.io.File)
     * @param resourceName
     * @return
     */
    public static File getResourceFile(String resourceName) {
        File resFile = null;
        try {
            Enumeration<URL> urls = ProbeClientTestUtils.class.getClassLoader().getResources(resourceName);
            while(urls.hasMoreElements()) {
                URL url = urls.nextElement();
                resFile = new File(url.toURI());
                break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resFile;
    }

    /**
     * 读取classpath中的资源文件的内容，返回为String类型。
     * @param resourceName
     * @return
     */
    public static String loadResource(String resourceName, String charsetName)  {
        String content = null;
        try(InputStream licenseIn = ProbeClientTestUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
            content = IOUtils.toString(licenseIn, charsetName);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    public static String loadResource(String resourceName)  {
        return loadResource(resourceName, "UTF-8");
    }



}
