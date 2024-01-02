package com.tongtech.proxy.core.acl;

import com.tongtech.proxy.core.StaticContent;
import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.crypto.SM3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class AclAuthen {
    private final static Log Logger = ProxyConfig.getServerLog();

    private final String userName;
    private final String userPass;
    private final ArrayList<Authen> authens = new ArrayList<>();

    public AclAuthen(String user_name, String user_pass, String user_roles, HashMap<Integer, Authen> roles) {
        this.userName = user_name;
        this.userPass = user_pass;

        String[] str_roles = user_roles.split(",");
        for (String str_role : str_roles) {
            try {
                int roleId = Integer.parseInt(str_role);
                Authen role = roles.get(roleId);
                if (role != null) {
                    authens.add(role);
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 验证密码
     *
     * @param pwd
     * @return
     */
    public boolean auth(String pwd) {
        try {
            String encr = SM3.hash(pwd);
            if (this.userPass.equals(encr)) {
                Logger.infoLog("AclAuthen::auth() User {} authed ok", this.userName);
                return true;
            } else {
                Logger.infoLog("AclAuthen::auth() User {}'s password {} authed failed", this.userName, encr);
            }
        } catch (Exception e) {
            Logger.warnLog("AclAuthen::auth() Error occur: {}", e);
        }
        return false;
    }

    /**
     * 验证命令是否允许操作，且key是否允许访问
     *
     * @param cmd
     * @param key
     * @return
     */
    public boolean authentication(String cmd, byte[] key) {
//        String key = keys != null && keys.length > 0 ? keys[0] : null;

        for (Authen authen : authens) {
            if (authen.authentication(cmd, key)) {
                if (Logger.isDebug()) {
                    Logger.debugLog("AclAuthen:authentication() User {} executes the {} command operation {} ok."
                            , this.userName, cmd, key != null ? StaticContent.escape(key, 0, key.length) : "null");
                }
                return true;
            }
        }
        if (Logger.isInfo()) {
            Logger.infoLog("AclAuthen:authentication() User {} executes the {} command operation {} failed."
                    , this.userName, cmd, key != null ? StaticContent.escape(key, 0, key.length) : "null");
        }
        return false;
    }

    public String getUserName() {
        return this.userName;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(512);
        buf.append("UserName: ").append(this.userName).append('\n');
        buf.append("Password: ").append(this.userPass).append('\n');
        buf.append("Roles: ").append(this.authens.size()).append('\n');
        for (int i = 0; i < this.authens.size(); ++i) {
            Authen authen = this.authens.get(i);
            buf.append("    Role-").append(i).append('\n');
            buf.append("        CmdList: ").append(authen.getAuthedCommand()).append('\n');
            buf.append("        KeyPres: ");
            byte[][] keys = authen.getKeyPres();
            if (keys == null || keys.length == 0) {
                buf.append("*");
            } else {
                buf.append('[');
                for (int j = 0; j < keys.length; ++j) {
                    if (j > 0) {
                        buf.append(", ");
                    }
                    buf.append(new String(keys[j], StandardCharsets.UTF_8)).append('*');
                }
                buf.append(']');
            }
            buf.append('\n');
        }
        return buf.toString();
    }
}
