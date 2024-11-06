package com.tongtech.common.utils;

import com.tongtech.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * 测试 redis服务是否可用
 */
public class RedisUtils {

    protected final static Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 检查redis是否存活
     * @param url  服务器地址
     * @param port 端口
     * @return
     */
    public static void testRedis(String url, int port) {
        String result;
        try(Jedis jedis = new Jedis(url, port)) {
            String ping = jedis.ping();
            if (ping.equalsIgnoreCase("PONG") == false)  {
                result = "Response: '" + ping + "'. Error, It should be 'PONG' !";
                throw new ServiceException(result);
            }
        } catch (Exception e) {
            throw e;
        }
    }



    /**
     * 检查redis是否存活
     * @param url 服务器地址
     * @param port 端口
     * @param password redis的密码
     * @return null 表示连接成功；失败会返回失败原因；
     */
    public static void testRedis(String url, int port, String password) {
        try(Jedis jedis = new Jedis(url, port)) {
            jedis.auth(password);//密码
            String ping = jedis.ping();
            if ("PONG".equalsIgnoreCase(ping) == false) {
                String result = "Response: '" + ping + "'. Error, It should be 'PONG' !";
                throw new ServiceException(result);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static void main(String[] args) {
        testRedis("127.0.0.1", 6379, "foobared");
        testRedis("127.0.0.1", 6379);
    }
}
