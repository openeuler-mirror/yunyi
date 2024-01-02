package com.tongtech.proxy.core.acl;

import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.center.ProxyData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AccessController {
    private final static Log Logger = ProxyConfig.getServerLog();
    private final static ConcurrentHashMap<String, AclAuthen> USER_AUTHEN = new ConcurrentHashMap<>();

    /**
     * 用户密码认证，认证成功返回Acl控制类，否则返回空
     *
     * @param user
     * @param pass
     * @return
     */
    public static AclAuthen auth(String user, String pass) {
        AclAuthen acl = USER_AUTHEN.get(user);
        if (acl != null && acl.auth(pass)) {
            return acl;
        }
        return null;
    }

    public static void setAclDataFromCenter(List data) {
        int offset = 0;
        if ((Long) data.get(offset++) != ProxyData.MESSAGETYPE_MEMDBACLCHG) {
            Logger.warnLog("AccessController::setAclDataFromCenter() Invalid message received.");
            return;
        }
        USER_AUTHEN.clear();
        int role_size = ((Long) data.get(offset++)).intValue();
        if (role_size > 0) {
            HashMap<Integer, Authen> roles = new HashMap<>();
            for (int i = 0; i < role_size; ++i) {
                int id = ((Long) data.get(offset++)).intValue();
                String cmd_list = (String) data.get(offset++);
                String pr_list = (String) data.get(offset++);
                roles.put(id, new Authen(cmd_list, pr_list));
            }
            int user_size = ((Long) data.get(offset++)).intValue();
            for (int i = 0; i < user_size; ++i) {
                String userName = (String) data.get(offset++);
                String userPass = (String) data.get(offset++);
                String userRoles = (String) data.get(offset++);
                USER_AUTHEN.put(userName, new AclAuthen(userName, userPass, userRoles, roles));
            }
        }

        writeLogFile();
    }

    public static void writeLogFile() {
        String fileName = ProxyConfig.getServerHome() + File.separator + "logs" + File.separator + "users.acl";
        if (USER_AUTHEN.size() > 0) {
            try (OutputStream os = new FileOutputStream(fileName)) {
                for (AclAuthen aclAuthen : USER_AUTHEN.values()) {
                    os.write(aclAuthen.toString().getBytes(StandardCharsets.UTF_8));
                    os.write('\n');
                }
            } catch (Throwable t) {
                Logger.warnLog("AccessController::writeLogFile() Error writing file: {}", t);
            }
        } else {
            try {
                new File(fileName).delete();
            } catch (Throwable t) {
                Logger.warnLog("AccessController::writeLogFile() Error deleting file: {}", t);
            }
        }
    }
}
