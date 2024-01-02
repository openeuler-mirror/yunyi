package com.tongtech.common.test;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;



public class ClassResourceUtils {


    /**
     * 获得classpath中的资源文件的路径，返回资源文件(java.io.File)
     * @param resourceName
     * @return
     */
    public static File getResourceFile(String resourceName) {
        File resFile = null;
        try {
            Enumeration<URL> urls = ClassResourceUtils.class.getClassLoader().getResources(resourceName);
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

    public static String loadResource(String resourceName) {
        return loadResource(resourceName, "UTF-8");
    }

    /**
     * 读取classpath中的资源文件的内容，返回为String类型。
     * @param resourceName
     * @return
     */
    public static String loadResource(String resourceName, String charsetName)  {
        String content = null;
        try(InputStream licenseIn = ClassResourceUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
            content = IOUtils.toString(licenseIn, charsetName);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content;
    }


}
