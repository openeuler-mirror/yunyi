package com.tongtech.system.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 对象缓存Service接口
 *
 * @author Zhang ChenLong
 * @date 2023-04-04
 */
public interface SysObjectCacheService
{

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     */
    <T extends Serializable> void setCacheObject(String key, T value);

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     */
    <T extends Serializable> void setCacheObject(String key, T value, Integer timeout, TimeUnit timeUnit);

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    <T extends Serializable> T getCacheObject(String key);

    /**
     * 删除单个对象
     *
     * @param key
     */
    boolean deleteObject(String key);

    /**
     * 删除集合对象
     *
     * @param keyList 多个对象
     * @return 删除的对象数量
     */
    boolean deleteObject(Collection<String> keyList);

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    Collection<String> keys(final String pattern);

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    Boolean hasKey(String key);

    /**
     * 清除缓存中的所有数据
     */
    void cleanCache();
}
