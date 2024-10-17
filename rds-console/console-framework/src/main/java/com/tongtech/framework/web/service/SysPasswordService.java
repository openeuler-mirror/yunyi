package com.tongtech.framework.web.service;

import java.util.Date;

import com.tongtech.common.exception.ServiceException;
import com.tongtech.system.service.ISysConfigService;
import com.tongtech.system.service.ISysUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.tongtech.common.constant.Constants;
import com.tongtech.common.core.domain.entity.SysUser;
import com.tongtech.common.utils.MessageUtils;
import com.tongtech.common.utils.SecurityUtils;
import com.tongtech.framework.manager.AsyncManager;
import com.tongtech.framework.manager.factory.AsyncFactory;
import com.tongtech.framework.security.context.AuthenticationContextHolder;

import javax.annotation.Resource;

/**
 * 登录密码方法
 *
 * @author XiaoZhangTongZhi
 */
@Component
public class SysPasswordService {

    private final static long DAY_TIME = 24 * 60 * 60 * 1000;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private ISysConfigService sysConfigService;

    public void validate(SysUser user) {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        SysUser sysUser = sysUserService.selectUserByUserName(username);

        SysUser userToUpdate = new SysUser();
        userToUpdate.setUserId(sysUser.getUserId());

        int loginRetries = sysUser.getLoginRetries();
        //获取系统配置中登录尝试次数账号锁定的值
        int maxLoginRetries = Integer.parseInt(sysConfigService.selectConfigByKey("sys.login.maxRetries"));

        if (loginRetries >= maxLoginRetries) {
            String msg = MessageUtils.message("user.login.retries.exceed", maxLoginRetries);
            // 异步日志记录
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, msg));

            throw new ServiceException(msg);
        }
        else if (!matches(user, password)) {
            loginRetries ++;
            userToUpdate.setLoginRetries(loginRetries);
            if (loginRetries >= maxLoginRetries) { //已超出认证重试次数
                userToUpdate.setLoginLocked("1");
                sysUserService.updateUser(userToUpdate);
                String msg = MessageUtils.message("user.login.retries.exceed", maxLoginRetries);
                throw new ServiceException(msg);
            }
            else {   //未到达认证重试次数
                userToUpdate.setLoginLocked("0");
                sysUserService.updateUser(userToUpdate);
            }

        } else {
            userToUpdate.setLoginLocked("0");
            userToUpdate.setLoginRetries(0);
            sysUserService.updateUser(userToUpdate);
        }

    }

    public boolean matches(SysUser user, String rawPassword) {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }


    /**
     * 清除登录锁定
     * @param userId
     */
    public void clearLoginLocked(Long userId) {
        SysUser u = new SysUser();
        u.setUserId(userId);
        u.setLoginLocked("0");
        u.setLoginRetries(0);
        sysUserService.updateUser(u);
    }

    /**
     * 已当前时间为准更新到下一个密码过期时间
     * @param userId
     */
    public SysUser renewPasswordExpiredTime(Long userId) {
        SysUser u = new SysUser();
        int expireDays = Integer.parseInt(sysConfigService.selectConfigByKey("sys.password.expireDays"));
        long expireTime = System.currentTimeMillis() + expireDays * DAY_TIME;
        u.setUserId(userId);
        u.setPasswordExpired(new Date(expireTime));
        sysUserService.updateUser(u);
        return u;
    }


}
