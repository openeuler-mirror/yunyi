package com.tongtech.system.service.impl;

import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;

import com.tongtech.common.config.UhConsoleConfig;
import com.tongtech.system.mapper.SysMenuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tongtech.common.annotation.DataSource;
import com.tongtech.common.constant.CacheConstants;
import com.tongtech.common.constant.UserConstants;
import com.tongtech.common.core.cache.ObjectCache;
import com.tongtech.common.core.text.Convert;
import com.tongtech.common.enums.DataSourceType;
import com.tongtech.common.exception.ServiceException;
import com.tongtech.common.utils.StringUtils;
import com.tongtech.system.domain.SysConfig;
import com.tongtech.system.mapper.SysConfigMapper;
import com.tongtech.system.service.ISysConfigService;
import org.springframework.transaction.annotation.Transactional;

import static com.tongtech.common.constant.ConsoleConstants.CONFIG_SYS_DEVELOPMENT_MODE_KEY;
import static com.tongtech.common.constant.ConsoleConstants.CONFIG_SYS_INITIALIZED_KEY;

/**
 * 参数配置 服务层实现
 *
 * @author XiaoZhangTongZhi
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private ObjectCache redisCache;

    @Autowired
    private UhConsoleConfig config;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct()
    public void init()
    {
        loadingConfigCache();

        initSysMenu();
    }


    @Override
    @Transactional
    public void initSysMenu() {
        boolean sysInitialized = Boolean.parseBoolean(selectConfigByKey(CONFIG_SYS_INITIALIZED_KEY));
        boolean developmentMode = Boolean.parseBoolean(selectConfigByKey(CONFIG_SYS_DEVELOPMENT_MODE_KEY));


        if(sysInitialized == false) {
            //开发模式下，系统不再走部署模式的切换和初始化，CONFIG_SYS_INITIALIZED_KEY 会始终不变。
            if(developmentMode) {
                logger.info("System set to development mode(sys.development.mode=true). All menu was enabled!");
                menuMapper.updateDevelopmentMenuStatus("0");
            }
            else {
                menuMapper.updateDevelopmentMenuStatus("1");
                logger.info("System initialized to deployEnv=" + config.getDeployEnv());
            }

            setConfigValueByKey(CONFIG_SYS_INITIALIZED_KEY, "true");
        }
    }

    /**
     * 查询参数配置信息
     *
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    @DataSource(DataSourceType.MASTER)
    public SysConfig selectConfigById(Long configId)
    {
        SysConfig config = new SysConfig();
        config.setConfigId(configId);
        return configMapper.selectConfig(config);
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    public String selectConfigByKey(String configKey) {
        // 首先尝试从缓存中获取配置值
        String cacheKey = getCacheKey(configKey);
        String configValue = Convert.toStr(redisCache.getCacheObject(cacheKey));
        if (StringUtils.isNotBlank(configValue)) {
            // 如果缓存中有值且不为空，则直接返回
            return configValue;
        }

        // 缓存中没有值，尝试从数据库中获取
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        SysConfig retConfig = configMapper.selectConfig(config);
        if (StringUtils.isNotNull(retConfig))
        {
            redisCache.setCacheObject(cacheKey, retConfig.getConfigValue());
            return retConfig.getConfigValue();
        }

        // 如果数据库中也没有，则返回空字符串
        return StringUtils.EMPTY;
    }

    /**
     * 获取验证码开关
     *
     * @return true开启，false关闭
     */
    @Override
    public boolean selectCaptchaEnabled()
    {
        String captchaEnabled = selectConfigByKey("sys.account.captchaEnabled");
        if (StringUtils.isEmpty(captchaEnabled))
        {
            return true;
        }
        return Convert.toBool(captchaEnabled);
    }

    /**
     * 查询参数配置列表
     *
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    @Override
    public List<SysConfig> selectConfigList(SysConfig config)
    {
        return configMapper.selectConfigList(config);
    }

    /**
     * 新增参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int insertConfig(SysConfig config)
    {
        int row = configMapper.insertConfig(config);
        if (row > 0)
        {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 修改参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int updateConfig(SysConfig config)
    {
        int row = configMapper.updateConfig(config);
        if (row > 0)
        {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 修改参数配置
     *
     * @param configKey 参数key
     * @param configValue 参数value
     * @return 结果
     */
    @Override
    public int setConfigValueByKey(String configKey, String configValue) {
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        int row = configMapper.updateConfigByKey(config);
        if (row > 0)
        {
            redisCache.setCacheObject(getCacheKey(configKey), configValue);
        }
        return row;
    }


    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     */
    @Override
    public void deleteConfigByIds(Long[] configIds)
    {
        for (Long configId : configIds)
        {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType()))
            {
                throw new ServiceException(String.format("内置参数【%1$s】不能删除 ", config.getConfigKey()));
            }
            configMapper.deleteConfigById(configId);
            redisCache.deleteObject(getCacheKey(config.getConfigKey()));
        }
    }

    /**
     * 加载参数缓存数据
     */
    @Override
    public void loadingConfigCache()
    {
        List<SysConfig> configsList = configMapper.selectConfigList(new SysConfig());
        for (SysConfig config : configsList)
        {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
    }

    /**
     * 清空参数缓存数据
     */
    @Override
    public void clearConfigCache()
    {
        Collection<String> keys = redisCache.keys(CacheConstants.SYS_CONFIG_KEY + "*");
        redisCache.deleteObject(keys);
    }

    /**
     * 重置参数缓存数据
     */
    @Override
    public void resetConfigCache()
    {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * 校验参数键名是否唯一
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public String checkConfigKeyUnique(SysConfig config)
    {
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();
        SysConfig info = configMapper.checkConfigKeyUnique(config.getConfigKey());
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String configKey)
    {
        return CacheConstants.SYS_CONFIG_KEY + configKey;
    }
}
