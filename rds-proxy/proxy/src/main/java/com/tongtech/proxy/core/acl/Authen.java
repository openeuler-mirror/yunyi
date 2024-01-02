package com.tongtech.proxy.core.acl;

import com.tongtech.proxy.core.protocol.Commands;
import com.tongtech.proxy.core.utils.BytesUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class Authen {
    private final HashSet<String> AuthedCommand = new HashSet<>(200);
    private final byte[][] KeyPres;

    Authen(String cmd_list, String pre_list) {
        if (pre_list != null && pre_list.length() > 0 && !"*".equals(pre_list)) {
            String[] pres = pre_list.split(",");

            KeyPres = new byte[pres.length][];
            for (int i = 0; i < pres.length; ++i) {
                KeyPres[i] = pres[i].getBytes(StandardCharsets.UTF_8);
            }
        } else {
            KeyPres = null;
        }

        if (cmd_list != null && !"NULL".equalsIgnoreCase(cmd_list)) {
            String[] cmds = cmd_list.split("[ \t]*,[ \t]*");
            for (String cmd : cmds) {
                if (cmd.charAt(0) == '+') {
                    cmd = cmd.substring(1);
                    if (cmd.charAt(0) == '@') {
                        Commands.addCommands(AuthedCommand, cmd);
                    } else {
                        AuthedCommand.add(cmd);
                    }
                } else if (cmd.charAt(0) == '-') {
                    cmd = cmd.substring(1);
                    if (cmd.charAt(0) == '@') {
                        Commands.removeCommands(AuthedCommand, cmd);
                    } else {
                        AuthedCommand.remove(cmd);
                    }
                } else {
                    if (cmd.charAt(0) == '@') {
                        Commands.addCommands(AuthedCommand, cmd);
                    } else {
                        AuthedCommand.add(cmd);
                    }
                }
            }
        }
    }

    public boolean authentication(String cmd, byte[] key) {
        // cmd为空时不做操作命令的鉴权
        if (cmd != null && !AuthedCommand.contains(cmd)) {
            // 命令不在允许列表内
            return false;
        }

        // keys为空时不做key前缀的鉴权
        if (KeyPres == null || key == null || key.length == 0) {
            // 不需要判断前缀
            return true;
        }

        // 判断前缀是否符合
//        for (String key : keys) {
//            key= BinaryStringUtil.decode(key);
        boolean authOk = false;
        for (byte[] pre : KeyPres) {
            if (BytesUtil.startWith(key, pre)) {
                authOk = true;
                break;
            }
        }
        if (!authOk) {
            return false;
        }
//        }

        return true;
    }

    public HashSet<String> getAuthedCommand() {
        return AuthedCommand;
    }

    public byte[][] getKeyPres() {
        return KeyPres;
    }
}
