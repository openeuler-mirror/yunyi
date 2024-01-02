package com.tongtech.system.mapper;

import java.util.List;
import com.tongtech.system.domain.SysObjectCache;

/**
 * 对象缓存Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-04-04
 */
public interface SysObjectCacheMapper
{
    /**
     * 查询对象缓存
     *
     * @param objKey 对象缓存主键
     * @return 对象缓存
     */
    public SysObjectCache selectSysObjectCacheByObjKey(String objKey);

    /**
     * like 方式查询 objKey
     * @param objKey
     * @return
     */
    public List<String> selectKeysByKey(String objKey);

    /**
     * 查询对象缓存列表
     *
     * @param sysObjectCache 对象缓存
     * @return 对象缓存集合
     */
    public List<SysObjectCache> selectSysObjectCacheList(SysObjectCache sysObjectCache);

    /**
     * 新增对象缓存
     *
     * @param sysObjectCache 对象缓存
     * @return 结果
     */
    public int insertSysObjectCache(SysObjectCache sysObjectCache);

    /**
     * 修改对象缓存
     *
     * @param sysObjectCache 对象缓存
     * @return 结果
     */
    public int updateSysObjectCache(SysObjectCache sysObjectCache);

    /**
     * 删除对象缓存
     *
     * @param objKey 对象缓存主键
     * @return 结果
     */
    public int deleteSysObjectCacheByObjKey(String objKey);

    public int deleteExpiredSysObjectCache(Long time);

    /**
     * 批量删除对象缓存
     *
     * @param objKeys 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysObjectCacheByObjKeys(String[] objKeys);

    /**
     * 批量删除所有对象缓存
     * @return
     */
    public int deleteAll();
}
