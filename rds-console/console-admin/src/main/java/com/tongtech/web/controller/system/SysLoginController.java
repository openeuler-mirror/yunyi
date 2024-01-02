package com.tongtech.web.controller.system;

import java.util.List;
import java.util.Set;

import com.tongtech.common.utils.AESUtils;
import com.tongtech.common.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.tongtech.common.constant.Constants;
import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.common.core.domain.entity.SysMenu;
import com.tongtech.common.core.domain.entity.SysUser;
import com.tongtech.common.core.domain.model.LoginBody;
import com.tongtech.common.utils.SecurityUtils;
import com.tongtech.framework.web.service.SysLoginService;
import com.tongtech.framework.web.service.SysPermissionService;
import com.tongtech.system.service.ISysMenuService;

/**
 * 登录验证
 *
 * @author XiaoZhangTongZhi
 */
@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/web-api/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();

        String password = AESUtils.decryptAES(loginBody.getPassword());

        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), password, loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 返回密码是否过期
     * @return
     */
    @GetMapping("/web-api/passwordSuggestedChanges")
    public AjaxResult passwordSuggestedChanges() {
        if (loginService.isUserExpired(SecurityUtils.getUserId())) {
            return AjaxResult.success(MessageUtils.message("user.password.expired"), true);
        }
        else {
            return AjaxResult.success(false);
        }
    }

    /**
     * 返回用户密码修改建议
     * @return
     *    密码过期： msg="密码已过期，请设置新的密码！"，data="expired";
     *    首次登录： msg="首次登录，为保证您的账号安全，请更改密码！"，data="initial"
     *    无需修改： msg="none", data="none"
     */
    @GetMapping("/web-api/passwordSuggest")
    public AjaxResult passwordSuggest() {
        return loginService.getPasswordSuggestion(SecurityUtils.getUserId());
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/web-api/getInfo")
    public AjaxResult getInfo()
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        user.setPassword("");
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("/web-api/getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
