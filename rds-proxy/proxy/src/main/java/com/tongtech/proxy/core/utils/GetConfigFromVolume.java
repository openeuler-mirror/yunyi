package com.tongtech.proxy.core.utils;

import com.tongtech.proxy.core.utils.config.PropertyPlaceholderHelper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class GetConfigFromVolume {

    private static long UpdateTimestamp = 0;

    private static final File VolumePath = new File(System.getenv(
            "CONFIG_VOLUME_PATH") != null
            ? System.getenv("CONFIG_VOLUME_PATH")
            : (System.getProperty("server.home", ".") + "/etc/properties"));

    private static final ConcurrentHashMap<String, String> VolumeProperties = new ConcurrentHashMap<>();

    /**
     * 将head作为匹配头，从values的start位置开始匹配，如果完全匹配且values随后紧跟的是‘：’，说明匹配成功，取values的后半段返回，否则返回空
     *
     * @param head
     * @param values
     * @param start
     * @param end
     * @return
     */
    private static String getMatchedValue(String head, String values, int start, int end) {
        if (start >= end) {
            return null;
        }

        int head_end = 0;
        if (values.charAt(start) != ':') {
            // 字符‘:’开头将被视为不用匹配head的缺省值配置
            for (int i = start; i < end; ++i) {
                if (head.length() == head_end) {
                    break;
                } else if (head.charAt(head_end) != values.charAt(i)) {
                    return null;
                }
                ++head_end;
            }
        }
        if (start + head_end + 1 >= values.length() || values.charAt(start + head_end) != ':') {
            return null;
        }
        return values.substring(start + head_end + 1, end);
    }

    /**
     * 按照英文的逗号分隔配置中的各项，如果配置内容里有“，”，需要转移
     *
     * @param key
     * @param svalue
     * @return
     */
    private static String getFromStringMap(String key, String svalue) {
        if (key == null || key.length() == 0) {
            return null;
        }

        int start = 0;
        int end = 0;
        String value = null;
        while (end >= 0) {
            try {
                end = svalue.indexOf(',', end);
                if (end < 0) {
                    // 后续没有分隔符了
                    break;
                } else if (end + 1 < svalue.length() && svalue.charAt(end + 1) == ',') {
                    // 英文的逗号是分隔符，如果内容里有逗号，需编码成连续的两个逗号
                    // 连续的2个逗号是配置内容
                    end += 2;
                } else {
                    value = getMatchedValue(key, svalue, start, end);
                    if (value != null) {
                        // 已经找到对应的value
                        break;
                    }
                    start = end + 1;
                    end = start;
                }
            } catch (Throwable t) {
                end = -1;
            }
        }
        if (value == null && end < 0) {
            value = getMatchedValue(key, svalue, start, svalue.length());
        }
        if (value != null && value.indexOf(',') >= 0) {
            value = value.replace(",,", ",");
        }
        return value;
    }

    private synchronized static void reload() {
        long curtime = System.currentTimeMillis() >> 16; // 65 秒
        if (curtime == UpdateTimestamp) {
            return;
        }
        UpdateTimestamp = curtime;

        if (VolumePath.isDirectory()) {
            HashMap<String, String> map = new HashMap<>();
            File[] files = VolumePath.listFiles();
            if (files != null && files.length > 0) {
                // 读各配置内容
                for (File file : files) {
                    try {
                        if (file.isFile()) {
                            String key = file.getName();
                            String value = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

//                            if (!key.startsWith("cfg.") && !key.startsWith("dynamic.")) {
                            if (key.charAt(0) >= 'A' && key.charAt(0) <= 'Z') {
                                // 配置文件的首字母大写，不是普通的一对一的配置内容
                                // 被视为pod的个性化配置项（每个pod值不相同），文件中的内容为一组key、value对，由逗号分隔。格式为：k1:v1,k2:v2,k3,v3...
                                // 支持缺省值配置，例如：“k1:v1,k2:v2,:v”。
                                // 文件名中起始的大写的段（由‘.’分隔）定义为环境变量名，程序根据该名取环境变量的值作为key值在文件中匹配对象的value。
                                // 配置限制：1、文件名的起始段（即环境变量名）必须大写；2、文件中的key不能包含“:”和“,”；3、value中如果有“，”需用“,,”代替
                                int start = key.indexOf('.');
                                if (start <= 0 || start + 1 >= key.length()) {
                                    // 前缀错误
                                    continue;
                                }
                                value = getFromStringMap(System.getenv(key.substring(0, start)), value);
                                if (value == null) {
                                    continue;
                                }
                                key = key.substring(start + 1);
                            }

                            value = PropertyPlaceholderHelper.INSTANCE.replacePlaceholders(value, null);
                            if (key != null && key.length() > 0 && value != null && value.length() > 0) {
                                map.put(key, value);
                            }
                        }
                    } catch (Throwable t) {
//                        Logger.infoLog("GetConfigFromVolume::reload() Load property from {} failed: {}", file, t);
                        System.out.println("ERR for " + file.getName() + ": " + t);
                    }
                }
                // 检查原有配置是否有删减
                for (String key : VolumeProperties.keySet()) {
                    if (!map.containsKey(key)) {
                        VolumeProperties.remove(key);
                    }
                }
                // 更新配置
                for (String key : map.keySet()) {
                    String value = map.get(key);
                    if (!value.equals(VolumeProperties.get(key))) {
                        VolumeProperties.put(key, value);
                    }
                }
                return;
            }
        }
        // 没有配置
        VolumeProperties.clear();
    }

    public static void fillPropertiesFromVolume(String prefix, Properties pro) {
        if (prefix == null || prefix.length() == 0) {
            return;
        }

        reload();

        for (String key : VolumeProperties.keySet()) {
            if (key.startsWith(prefix)) {
                int pos = prefix.length();
                if (key.charAt(pos) == '.') {
                    pos++;
                }
                pro.setProperty(key.substring(pos), VolumeProperties.get(key));
            }
        }
    }
}
