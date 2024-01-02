package com.tongtech.common.core.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * 定义缓存的通用接口
 */
public interface ObjectCache {

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     */
    <T> void setCacheObject(String key, T value);

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     */
    <T> void setCacheObject(String key, T value, Integer timeout, TimeUnit timeUnit);

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    <T> T getCacheObject(String key);

    /**
     * 删除单个对象
     *
     * @param key
     */
    boolean deleteObject(String key);

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return 删除的对象数量
     */
    boolean deleteObject(Collection collection);

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

}
