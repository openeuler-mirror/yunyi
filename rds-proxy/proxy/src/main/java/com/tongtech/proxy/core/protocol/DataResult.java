package com.tongtech.proxy.core.protocol;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.acl.AclAuthen;
import com.tongtech.proxy.core.acl.AclFailedException;
import com.tongtech.proxy.core.slices.ResultCallback;

import java.io.IOException;
import java.util.List;

public interface DataResult extends ResultCallback {

    AclFailedException ACL_FAILED_EXCEPTION = new AclFailedException("ACL failed");

    ChannelHandlerContext getSession();

    /**
     * 每次使用前初始化内部数据
     *
     * @param session
     */
    void init(Object session, List request);

    /**
     * 写或同步数据成功后调用1次该函数
     *
     * @throws IOException
     */
    void setOk() throws IOException;

    void setOk(long i) throws IOException;

    /**
     * 写或同步数据失败时调用该函数1次,参数是错误原因
     *
     * @param msg
     * @throws IOException
     */
    void setErr(int err_code, String msg) throws IOException;

    /**
     * 不做任何拼装直接发送数据
     *
     * @param msg
     * @throws IOException
     * @return
     */
    ChannelFuture send(String msg) throws IOException;

    void sendObject(Object o) throws IOException;

    /**
     * 清空缓冲区
     *
     * @throws IOException
     */
    void flush() throws IOException;

    /**
     * 设置当前连接操作的缺省表id
     *
     * @param id 对应redis select 命令的参数 db
     */
    void setTableId(int id);

    /**
     * 返回当前连接操作的缺省表id
     */
    int getTableId();

    default void setAcl(AclAuthen acl) {
        throw ACL_FAILED_EXCEPTION;
    }

    default AclAuthen getAcl() {
        throw ACL_FAILED_EXCEPTION;
    }

    default void aclAuth(String cmd, byte[] key) {
        throw ACL_FAILED_EXCEPTION;
    }

    default void callback(Object o) throws IOException {
        sendObject(o);
    }

    default boolean isActive() {
        ChannelHandlerContext sess = getSession();
        if (sess != null) {
            return sess.channel().isActive();
        }
        return false;
    }
}
