package com.tongtech.common.core.cache.impl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.tongtech.common.core.cache.ObjectCache;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Component;

@Component
public class LocalMemCache implements ObjectCache {

    private final static long CHECK_INTERVAL = 1000; //过期扫描的时间间隔

    private long lastCheckingTime = System.currentTimeMillis(); //过期扫描的时间间隔

    private Map<String, Object> data = new ConcurrentHashMap<>();

    private Map<String, Long> dataExpire = new ConcurrentHashMap<String, Long>();

    @Override
    public <T> void setCacheObject(String key, T value) {
        removeExpired();

        data.put(key, value);
    }

    @Override
    public <T> void setCacheObject(String key, T value, Integer timeout, TimeUnit unit) {

        removeExpired();

        //If timeout > 0 save it as expire time.
        if(timeout > 0) {
            Long expireTime = System.currentTimeMillis() + TimeoutUtils.toMillis(timeout, unit);
            dataExpire.put(key, expireTime);
        }

        data.put(key, value);
    }


    @Override
    public <T> T getCacheObject(String key) {
        return (T)data.get(key);
    }

    public boolean deleteObject(final String key) {
        removeExpired();

        if(dataExpire.containsKey(key))  dataExpire.remove(key);
        return  (data.remove(key) != null);
    }

    @Override
    public boolean deleteObject(Collection collection) {
        removeExpired();

        long deleted = 0;
        for(Object keyObj : collection ) {
            String key = keyObj.toString();

            if(dataExpire.containsKey(key))  dataExpire.remove(key);
            if (data.remove(key) != null) {
                deleted ++;
            }
        }

        return deleted > 0;
    }

    /**
     * 获取匹配的缓存keys列表。
     *
     * @param pattern  4种模式：1.后缀模式 "*suffix", 2.前缀模式 preffix*， 3.包含模式"contains" ， 4 如果是""或null 返回所有key
     * @return 对象列表
     * @throws IOException
     */
    public Collection<String> keys(final String pattern) {
        Set<String> allKeys = data.keySet();
        if(pattern == null || "".equals(pattern)) {
            return allKeys;
        }
        else {
            Set<String> fillterKeys = new HashSet<String>();

            if(pattern.endsWith("*")) {
                String prefixPatten = pattern.substring(0, pattern.length() - 2);
                for (String key : allKeys) {
                    if(key.startsWith(prefixPatten)) {
                        fillterKeys.add(key);
                    }
                }
            }
            else if(pattern.startsWith("*")) {
                String suffixPatten = pattern.substring(1);
                for (String key : allKeys) {
                    if(key.endsWith(suffixPatten)) {
                        fillterKeys.add(key);
                    }
                }
            }
            else {
                for (String key : allKeys) {
                    if(key.contains(pattern)) {
                        fillterKeys.add(key);
                    }
                }
            }

            return fillterKeys;
        }
    }

    @Override
    public Boolean hasKey(String key) {
        return data.containsKey(key);
    }

    /**
     * 删除所有过期的数据，针对设置了timeout的数据。
     * @return
     */
    private void removeExpired() {
        long time = System.currentTimeMillis();
        if( time > (lastCheckingTime + CHECK_INTERVAL) ) {
            for(Map.Entry<String, Long> expireEn : dataExpire.entrySet()) {
                Long expire = expireEn.getValue();
                if(time > expire) {
                    String removingKey = expireEn.getKey();
                    data.remove(removingKey);
                    dataExpire.remove(removingKey);
                }
            }

            //Update the time stamp
            lastCheckingTime =  System.currentTimeMillis();
        }

    }

}
