package com.tongtech.system.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.tongtech.common.utils.ObjectSerializeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Service;
import com.tongtech.system.mapper.SysObjectCacheMapper;
import com.tongtech.system.domain.SysObjectCache;
import com.tongtech.system.service.SysObjectCacheService;

/**
 * 对象缓存Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-04-04
 */
@Service
public class SysObjectCacheServiceImpl implements SysObjectCacheService
{

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private SysObjectCacheMapper sysObjectCacheMapper;

    private final static long CHECK_INTERVAL = 1000; //过期扫描的时间间隔

    private long lastCheckingTime = System.currentTimeMillis(); //过期扫描的时间间隔

    /**
     * 启动时初始化，先清除缓存表中的所有数据
     */
    @PostConstruct
    public void init() {
        this.cleanCache();
    }


    @Override
    public <T extends Serializable> void setCacheObject(String key, T value) {
        removeExpired();

        byte[] valueBytes = ObjectSerializeUtils.serialize(value);
        SysObjectCache objCache = new SysObjectCache(key, valueBytes);
        sysObjectCacheMapper.insertSysObjectCache(objCache);
    }

    @Override
    public <T extends Serializable> void setCacheObject(String key, T value, Integer timeout, TimeUnit timeUnit) {
        removeExpired();

        SysObjectCache objCache;
        byte[] valueBytes = ObjectSerializeUtils.serialize(value);
        if(timeout > 0) {
            Long expireTime = System.currentTimeMillis() + TimeoutUtils.toMillis(timeout, timeUnit);
            objCache = new SysObjectCache(key, valueBytes, expireTime);
        }
        else {
            objCache = new SysObjectCache(key, valueBytes);
        }

        sysObjectCacheMapper.insertSysObjectCache(objCache);
    }

    @Override
    public <T extends Serializable> T getCacheObject(String key) {
        SysObjectCache c = sysObjectCacheMapper.selectSysObjectCacheByObjKey(key);
        long time = System.currentTimeMillis();
        if(c != null) {
            if(c.getExpireTime() == null || c.getExpireTime() > time ) {
                return (T)ObjectSerializeUtils.deserialize(c.getObjValue());
            }
            else {
                deleteObject(key); //删除过期的Key
                return null;
            }
        }
        else {
            return null;
        }

    }

    @Override
    public boolean deleteObject(String key) {
        return sysObjectCacheMapper.deleteSysObjectCacheByObjKey(key) > 0;
    }

    @Override
    public boolean deleteObject(Collection<String> keyList) {
        int len = keyList.size();
        String[] keys  = new String[len];
        int i = 0;
        for(String key : keyList) {
            keys[i++] = key;
        }
        return sysObjectCacheMapper.deleteSysObjectCacheByObjKeys(keys) > 0;
    }

    @Override
    public void cleanCache() {
        sysObjectCacheMapper.deleteAll();
        logger.info("All data in sys_object_cache has been deleted!");
    }

    @Override
    public Collection<String> keys(String pattern) {

        char[] chars = pattern.trim().toCharArray();
        int lastIndex = chars.length - 1;
        if(chars[0] == '*') {
            chars[0] = '%';
        }

        if(chars[lastIndex] == '*') {
            chars[lastIndex] = '%';
        }

        String newPattern = new String(chars);
        return sysObjectCacheMapper.selectKeysByKey(newPattern);
    }

    @Override
    public Boolean hasKey(String key) {
        SysObjectCache c = sysObjectCacheMapper.selectSysObjectCacheByObjKey(key);
        return c != null;
    }


    /**
     * 删除所有过期的数据，针对设置了timeout的数据。
     * @return
     */
    private void removeExpired() {
        long time = System.currentTimeMillis();
        if( time > (lastCheckingTime + CHECK_INTERVAL) ) {

            sysObjectCacheMapper.deleteExpiredSysObjectCache(time);
            //Update the time stamp
            lastCheckingTime =  System.currentTimeMillis();
        }

    }
}
