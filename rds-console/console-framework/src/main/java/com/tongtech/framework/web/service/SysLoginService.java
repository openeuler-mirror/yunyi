package com.tongtech.framework.web.service;

import javax.annotation.Resource;

import com.tongtech.common.config.UhConsoleConfig;
import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.system.service.SysObjectCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.tongtech.common.constant.CacheConstants;
import com.tongtech.common.constant.Constants;
import com.tongtech.common.core.domain.entity.SysUser;
import com.tongtech.common.core.domain.model.LoginUser;
import com.tongtech.common.exception.ServiceException;
import com.tongtech.common.exception.user.CaptchaException;
import com.tongtech.common.exception.user.CaptchaExpireException;
import com.tongtech.common.exception.user.UserPasswordNotMatchException;
import com.tongtech.common.utils.DateUtils;
import com.tongtech.common.utils.MessageUtils;
import com.tongtech.common.utils.ServletUtils;
import com.tongtech.common.utils.StringUtils;
import com.tongtech.common.utils.ip.IpUtils;
import com.tongtech.framework.manager.AsyncManager;
import com.tongtech.framework.manager.factory.AsyncFactory;
import com.tongtech.framework.security.context.AuthenticationContextHolder;
import com.tongtech.system.service.ISysConfigService;
import com.tongtech.system.service.ISysUserService;

import java.util.Date;

/**
 * 登录校验方法
 *
 * @author XiaoZhangTongZhi
 */
@Component
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysObjectCacheService cacheService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        // 验证码开关
        if (captchaEnabled)
        {
            validateCaptcha(username, code, uuid);
        }
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());
        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = cacheService.getCacheObject(verifyKey);
        cacheService.deleteObject(verifyKey);
        if (captcha == null)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateLoginInfo(sysUser);
    }





    /**
     * 判断用户密码是否过期
     * @param userId
     * @return
     */
    public boolean isUserExpired(Long userId) {
        if(UhConsoleConfig.isEmbedding() == true) { //嵌入模式下，不判断密码过期，
            return false; //返回未过期
        }

        SysUser user = userService.selectUserById(userId);
        Date expireDate = user.getPasswordExpired();
        if(expireDate != null) {
            if(System.currentTimeMillis() > expireDate.getTime()) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    /**
     * 返回用户密码修改建议
     * @param userId
     * @return
     *    密码过期： msg="密码已过期，请设置新的密码！"，data="expired";
     *    首次登录： msg="首次登录，为保证您的账号安全，请更改密码！"，data="initial"
     *    无需修改： msg="none", data="none"
     */
    public AjaxResult getPasswordSuggestion(Long userId) {
        if(UhConsoleConfig.isEmbedding() == true) { //嵌入模式下，不判断用户密码修改状态
            return AjaxResult.success("none", "none");
        }


        SysUser user = userService.selectUserById(userId);
        Date expireDate = user.getPasswordExpired();
        boolean expired = false;
        if(expireDate != null && System.currentTimeMillis() > expireDate.getTime()) {
            expired = true;
        }

        if(user.getUpdateTime() == null) {
            return AjaxResult.success(MessageUtils.message("user.password.initial"), "initial");
        }
        else if(expired) {
            return AjaxResult.success(MessageUtils.message("user.password.expired"), "expired");
        }
        else {
            return AjaxResult.success("none", "none");
        }

    }

}
