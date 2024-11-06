package com.tongtech.console.utils;


import com.alibaba.fastjson2.JSON;
import com.tongtech.common.exception.ServiceException;
import com.tongtech.common.utils.AssertUtils;
import com.tongtech.common.utils.StringUtils;
import com.tongtech.console.enums.DeployModeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.*;

import static com.tongtech.console.enums.DeployModeEnum.*;

public class JedisDataClient implements AutoCloseable {

    protected final Logger logger = LoggerFactory.getLogger(JedisDataClient.class);

    // 命令行交互模式返回后缀
    private final static String nss = "\r\n";


    /**
     * 连接模式 DeployModeEnum：
     */
    private DeployModeEnum deployMode;

    private Jedis jedis;

    private JedisCluster jedisCluster;

    private Long serviceId;

    private Map<String, JedisPool> masterPools = null;

    public JedisDataClient(Long serviceId, Jedis jedis, DeployModeEnum deployMode) {
        this.serviceId = serviceId;
        this.deployMode = deployMode;
        this.jedis = jedis;
        if(jedis == null) {
            throw new ServiceException("Jedis object is null! in JedisDataClient constructor.");
        }
    }

    public JedisDataClient(Long serviceId, JedisCluster jedisCluster) {
        this.serviceId = serviceId;
        this.jedisCluster = jedisCluster;
        this.deployMode = CLUSTER;
        if(jedisCluster == null) {
            throw new ServiceException("JedisCluster object is null! in JedisDataClient constructor.");
        }
    }

    public Map<String, JedisPool> getClusterNodes() {
        return jedisCluster.getClusterNodes();
    }

    public Map<String, JedisPool> getMasterNodes() {
        if (this.masterPools == null) {
            Map<String, JedisPool> nodes = jedisCluster.getClusterNodes();
            Set<String> keys = nodes.keySet();
            String clusterNodes = null;
            List<String> masterKeys = new ArrayList<>();
            for (String key : keys) {
                try (Jedis jd = nodes.get(key).getResource()) {
                    if (clusterNodes == null) {
                        clusterNodes = jd.clusterNodes();
                        String[] lines = clusterNodes.split("\\r?\\n");
                        for (String line : lines) {
                            String[] vars = line.split(" ");
                            if (vars.length > 3) {
                                if (vars[2].contains("master")) {
                                    masterKeys.add(vars[1]);
                                }
                            }
                        }
                        break;
                    }
                } catch (Exception e) {
                    //防止集群中有些节点是停用状态，只要从一个节点获取集群信息即可
                    logger.warn("Fail to get clusterNodes in getMasterNodes()", e);
                }
            }

            if (masterKeys.size() > 0) {
                this.masterPools = new HashMap<String, JedisPool>(masterKeys.size());
                for (String masterKey : masterKeys) {
                    JedisPool pool = nodes.get(masterKey);
                    if (pool != null) {
                        masterPools.put(masterKey, pool);
                    } else {
                        throw new RuntimeException("Failed to get master node, nodeKey=" + masterKey);
                    }
                }
                return masterPools;
            } else {
                throw new RuntimeException("Failed to get master node for serviceId=" + serviceId);
            }
        } else {
            return this.masterPools;
        }
    }

    public DeployModeEnum getDeployMode() {
        return deployMode;
    }

    public Long getServiceId() {
        return this.serviceId;
    }


    public ScanResult<String> scan(String cursor, ScanParams params) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.scan(cursor, params);
            default:
                return jedis.scan(cursor, params);
        }
    }


    public String set(final String key, final String value) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.set(key, value);
            default:
                return jedis.set(key, value);
        }
    }


    public String setex(final String key, final long seconds, final String value) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.setex(key, seconds, value);
            default:
                return jedis.setex(key, seconds, value);
        }
    }

    public String get(final String key) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.get(key);
            default:
                return jedis.get(key);
        }
    }

    public String type(final String key) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.type(key);
            default:
                return jedis.type(key);
        }
    }


    public Long ttl(final String key) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.ttl(key);
            default:
                return jedis.ttl(key);
        }
    }


    public List<String> configGet(final String pattern) {
        if(deployMode == CLUSTER) {
            Map<String, JedisPool> jedisPoolMap = jedisCluster.getClusterNodes();
            for (Map.Entry<String, JedisPool> entry : jedisPoolMap.entrySet()) {
                Jedis jedis = entry.getValue().getResource();
                return jedis.configGet(pattern);
            }
            throw new RuntimeException("configGet failed to get Jedis object!");
        }
        else {
            return jedis.configGet(pattern);
        }
    }

    public Long hset(String key, String field, String value) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.hset(key, field, value);
            default:
                return jedis.hset(key, field, value);
        }
    }

    public Long hset(String key, Map<String, String> hash) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.hset(key, hash);
            default:
                return jedis.hset(key, hash);
        }
    }

    public String hget(String key, String field) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.hget(key, field);
            default:
                return jedis.hget(key, field);
        }
    }

    /**
     * Select the DB with
     * having the specified zero-based numeric index.
     *
     * @param index the index
     * @return a simple string reply OK
     */
    public String select(int index) {
        if (deployMode != CLUSTER) {
            return jedis.select(index);
        } else {
            throw new RuntimeException("Not support select database in cluster mode!");
        }
    }


    /**
     * Undo a  at turning the expire key into a normal key.
     * <p>
     * Time complexity: O(1)
     *
     * @param key
     * @return Integer reply, specifically: 1: the key is now persist. 0: the key is not persist (only
     * happens when key not set).
     */
    public Long persist(final String key) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.persist(key);
            default:
                return jedis.persist(key);
        }
    }


    /**
     * Set a timeout on the specified key. After the timeout the key will be automatically deleted by
     * the server. A key with an associated timeout is said to be volatile in Redis terminology.
     * <p>
     * Volatile keys are stored on disk like the other keys, the timeout is persistent too like all
     * the other aspects of the dataset. Saving a dataset containing expires and stopping the server
     * does not stop the flow of time as Redis stores on disk the time when the key will no longer be
     * available as Unix time, and not the remaining seconds.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key already having an expire
     * set. It is also possible to undo the expire at all turning the key into a normal key using the
     * {@link #persist(String) PERSIST} command.
     * <p>
     * Time complexity: O(1)
     *
     * @param key
     * @param seconds
     * @return Integer reply, specifically: 1: the timeout was set. 0: the timeout was not set since
     * the key already has an associated timeout (this may happen only in Redis versions &lt;
     * 2.1.3, Redis &gt;= 2.1.3 will happily update the timeout), or the key does not exist.
     * @see <a href="http://redis.io/commands/expire">Expire Command</a>
     */
    public Long expire(String key, long seconds) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.expire(key, seconds);
            default:
                return jedis.expire(key, seconds);
        }
    }


    /**
     * Remove the specified member from the sorted set value stored at key. If member was not a member
     * of the set no operation is performed. If key does not not hold a set value an error is
     * returned.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the sorted set
     *
     * @param key
     * @param members
     * @return Integer reply, specifically: 1 if the new element was removed 0 if the new element was
     * not a member of the set
     */

    public Long zrem(final String key, final String... members) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.zrem(key, members);
            default:
                return jedis.zrem(key, members);
        }

    }

    public Set<String> zrange(String key, long start, long stop) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.zrange(key, start, stop);
            default:
                return jedis.zrange(key, start, stop);
        }
    }

    /**
     * Return the sorted set cardinality (number of elements). If the key does not exist 0 is
     * returned, like for empty sorted sets.
     * <p>
     * Time complexity O(1)
     *
     * @param key
     * @return the cardinality (number of elements) of the set as an integer.
     */
    public Long zcard(final String key) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.zcard(key);
            default:
                return jedis.zcard(key);
        }

    }

    /**
     * Return the score of the specified element of the sorted set at key. If the specified element
     * does not exist in the sorted set, or the key does not exist at all, a special 'nil' value is
     * returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     *
     * @param key
     * @param member
     * @return the score
     */
    public Double zscore(final String key, final String member) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.zscore(key, member);
            default:
                return jedis.zscore(key, member);
        }
    }

    /**
     * Add the specified member having the specified score to the sorted set stored at key. If member
     * is already a member of the sorted set the score is updated, and the element reinserted in the
     * right position to ensure sorting. If key does not exist a new sorted set with the specified
     * member as sole member is created. If the key exists but does not hold a sorted set value an
     * error is returned.
     * <p>
     * The score value can be the string representation of a double precision floating point number.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the sorted set
     *
     * @param key
     * @param score
     * @param member
     * @return Integer reply, specifically: 1 if the new element was added 0 if the element was
     * already a member of the sorted set and the score was updated
     */
    public Long zadd(final String key, final double score, final String member) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.zadd(key, score, member);
            default:
                return jedis.zadd(key, score, member);
        }
    }

    /**
     * Remove the specified member from the set value stored at key. If member was not a member of the
     * set no operation is performed. If key does not hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     *
     * @param key
     * @param members
     * @return Integer reply, specifically: 1 if the new element was removed 0 if the new element was
     * not a member of the set
     */
    public Long srem(final String key, final String... members) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.srem(key, members);
            default:
                return jedis.srem(key, members);
        }
    }

    /**
     * Return all the members (elements) of the set value stored at key. This is just syntax glue for
     * .
     * <p>
     * Time complexity O(N)
     *
     * @param key
     * @return Multi bulk reply
     */
    public Set<String> smembers(final String key) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.smembers(key);
            default:
                return jedis.smembers(key);
        }
    }

    /**
     * Add the specified member to the set value stored at key. If member is already a member of the
     * set no operation is performed. If key does not exist a new set with the specified member as
     * sole member is created. If the key exists but does not hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     *
     * @param key
     * @param members
     * @return Integer reply, specifically: 1 if the new element was added 0 if the element was
     * already a member of the set
     */
    public Long sadd(final String key, final String... members) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.sadd(key, members);
            default:
                return jedis.sadd(key, members);
        }
    }

    /**
     * Remove the first count occurrences of the value element from the list. If count is zero all the
     * elements are removed. If count is negative elements are removed from tail to head, instead to
     * go from head to tail that is the normal behaviour. So for example LREM with count -2 and hello
     * as value to remove against the list (a,b,c,hello,x,hello,hello) will leave the list
     * (a,b,c,hello,x). The number of removed elements is returned as an integer, see below for more
     * information about the returned value. Note that non existing keys are considered like empty
     * lists by LREM, so LREM against non existing keys will always return 0.
     * <p>
     * Time complexity: O(N) (with N being the length of the list)
     *
     * @param key
     * @param count
     * @param value
     * @return Integer Reply, specifically: The number of removed elements if the operation succeeded
     */
    public Long lrem(final String key, final long count, final String value) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.lrem(key, count, value);
            default:
                return jedis.lrem(key, count, value);
        }
    }

    /**
     * Return the specified elements of the list stored at the specified key. Start and end are
     * zero-based indexes. 0 is the first element of the list (the list head), 1 the next element and
     * so on.
     * <p>
     * For example LRANGE foobar 0 2 will return the first three elements of the list.
     * <p>
     * start and end can also be negative numbers indicating offsets from the end of the list. For
     * example -1 is the last element of the list, -2 the penultimate element and so on.
     * <p>
     * <b>Consistency with range functions in various programming languages</b>
     * <p>
     * Note that if you have a list of numbers from 0 to 100, LRANGE 0 10 will return 11 elements,
     * that is, rightmost item is included. This may or may not be consistent with behavior of
     * range-related functions in your programming language of choice (think Ruby's Range.new,
     * Array#slice or Python's range() function).
     * <p>
     * LRANGE behavior is consistent with one of Tcl.
     * <p>
     * <b>Out-of-range indexes</b>
     * <p>
     * Indexes out of range will not produce an error: if start is over the end of the list, or start
     * &gt; end, an empty list is returned. If end is over the end of the list Redis will threat it
     * just like the last element of the list.
     * <p>
     * Time complexity: O(start+n) (with n being the length of the range and start being the start
     * offset)
     *
     * @param key
     * @param start
     * @param stop
     * @return Multi bulk reply, specifically a list of elements in the specified range.
     */
    public List<String> lrange(final String key, final long start, final long stop) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.lrange(key, start, stop);
            default:
                return jedis.lrange(key, start, stop);
        }
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list stored at key. If the key
     * does not exist an empty list is created just before the append operation. If the key exists but
     * is not a List an error is returned.
     * <p>
     * Time complexity: O(1)
     *
     * @param key
     * @param strings
     * @return Integer reply, specifically, the number of elements inside the list after the push
     * operation.
     */
    public Long lpush(final String key, final String... strings) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.lpush(key, strings);
            default:
                return jedis.lpush(key, strings);
        }
    }

    /**
     * Trim an existing list so that it will contain only the specified range of elements specified. Start and end are zero-based indexes. 0 is the first element of the list (the list head), 1 the next element and so on.
     * For example LTRIM foobar 0 2 will modify the list stored at foobar key so that only the first three elements of the list will remain.
     * start and end can also be negative numbers indicating offsets from the end of the list. For example -1 is the last element of the list, -2 the penultimate element and so on.
     * Indexes out of range will not produce an error: if start is over the end of the list, or start > end, an empty list is left as value. If end over the end of the list Redis will threat it just like the last element of the list.
     * Hint: the obvious use of LTRIM is together with LPUSH/RPUSH. For example:
     * lpush("mylist", "someelement"); ltrim("mylist", 0, 99); *
     * The above two commands will push elements in the list taking care that the list will not grow without limits. This is very useful when using Redis to store logs for example. It is important to note that when used in this way LTRIM is an O(1) operation because in the average case just one element is removed from the tail of the list.
     * Time complexity: O(n) (with n being len of list - len of range)
     * Params:
     * key start stop
     * Returns:
     * Status code reply
     * @param key
     * @param index
     * @param value
     * @return
     */
    public String lset(final String key, Long index, String value) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.lset(key, index, value);
            default:
                return jedis.lset(key, index, value);
        }
    }

    /**
     * Return all the fields and associated values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     *
     * @param key
     * @return All the fields and values contained into a hash.
     */
    public Map<String, String> hgetAll(final String key) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.hgetAll(key);
            default:
                return jedis.hgetAll(key);
        }
    }

    /**
     * Remove the specified field from an hash stored at key.
     * <p>
     * <b>Time complexity:</b> O(1)
     *
     * @param key
     * @param fields
     * @return If the field was present in the hash it is deleted and 1 is returned, otherwise 0 is
     * returned and no operation is performed.
     */
    public Long hdel(final String key, final String... fields) {
        switch (deployMode) {
            case CLUSTER:
                return jedisCluster.hdel(key, fields);
            default:
                return jedis.hdel(key, fields);
        }
    }

    /**
     * Remove the specified keys. If a given key does not exist no operation is performed for this
     * key. The command returns the number of keys removed. Time complexity: O(1)
     *
     * @param keys
     * @return Integer reply, specifically: an integer greater than 0 if one or more keys were removed
     * 0 if none of the specified key existed
     */
    public Long del(final String... keys) {
        switch (deployMode) {
            case CLUSTER:
                //集群的情况下，在不同slot上的Key是不能同时删除的，需要追一删除
                long deleted = 0;
                for (String key : keys) {
                    jedisCluster.del(key);
                    deleted++;
                }
                return deleted;
            default:
                return jedis.del(keys);
        }
    }

    /**
     * stream操作
     **/
    /**
     * 新增stream类型
     *
     * @param key   键   若redis中没有该key新增时忽略value字段新增格什为  "New key" "New value"
     * @param value 值   新增的值 为Json 格式（方法中会进行校验）
     * @return
     */
    public String xadd(String key, String value) {

        AssertUtils.StringIsNull(key, "key 为空！");

        Map<String, String> newKey = new HashMap<>();

        // 首先判断key是否存在
        boolean existsKey;
        switch (deployMode) {
            case CLUSTER:
                existsKey = jedisCluster.exists(key);
                break;
            default:
                existsKey = jedis.exists(key);
                break;
        }

        if (!existsKey) {
            newKey.put("New key", "New value");
        } else if (!StringUtils.isEmpty(value)) {
            try {
                newKey = (Map<String, String>) JSON.parse(value);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServiceException("Json 格式化失败");
            }
        } else {
            throw new ServiceException("value 为空！");
        }


        StreamEntryID entryID = null;
        switch (deployMode) {
            case CLUSTER:
                entryID = jedisCluster.xadd(key, StreamEntryID.NEW_ENTRY, newKey);
                break;
            default:
                entryID = jedis.xadd(key, StreamEntryID.NEW_ENTRY, newKey);
                break;
        }

        return entryID.toString();
    }

    /**
     * 删除key下的某个值
     *
     * @param key 要操作的key
     * @param id  该key下的id
     * @return
     */
    public Boolean xdel(String key, String id) {
        Long xdelRes = null;
        switch (deployMode) {
            case CLUSTER:
                xdelRes = jedisCluster.xdel(key, new StreamEntryID(id));
                break;
            default:
                xdelRes = jedis.xdel(key, new StreamEntryID(id));
                break;
        }
        return xdelRes == 1L;
    }

    /**
     * 获取某个key下的内容,反向的列表，返回数据已格式化 前端直接展示即可
     *
     * @param key 要获取的某个key
     * @return
     */
    public ArrayList<Map<String, String>> xrevrange(String key) {
        List<StreamEntry> xrevrange = null;

        switch (deployMode) {
            case CLUSTER:
                xrevrange = jedisCluster.xrevrange(key, null, null, 200);
                break;
            default:
                xrevrange = jedis.xrevrange(key, null, null, 200);
                break;
        }
        AssertUtils.ObjectIsNull(xrevrange, key + " 获取失败！");

        return analyzeXrevrange(xrevrange);
    }

    /**
     * 对某个key下加载更多
     *
     * @param key 键值
     * @param id  键id
     * @return
     */
    public ArrayList<Map<String, String>> xrevrange(String key, String id) {
        List<StreamEntry> xrevrange = null;

        switch (deployMode) {
            case CLUSTER:
                xrevrange = jedisCluster.xrevrange(key, new StreamEntryID(id), null, 200);
                break;
            default:
                xrevrange = jedis.xrevrange(key, new StreamEntryID(id), null, 200);
                break;
        }

//        ArrayList<Map<String, String>> resArray = new ArrayList<>();
//        for (int i = xrevrange.size() - 1; i >= 0; i--) {
//            Map<String, String> item = new HashMap<>();
//            item.put("id", xrevrange.get(i).getID().toString());
//            item.put("fields", JSON.toJSONString(xrevrange.get(i).getFields()));
//            resArray.add(item);
//        }
//        return resArray;
        ArrayList<Map<String, String>> resultArrayList = analyzeXrevrange(xrevrange);
        resultArrayList.remove(0); //移除掉第一个第一个是前一个列表的重复数据
        return resultArrayList;
    }


    /**
     * 获取某个key下的内容列表, 返回数据已格式化 前端直接展示即可
     *
     * @param key 要获取的某个key
     * @return
     */
    public ArrayList<Map<String, String>> xrange(String key) {
        List<StreamEntry> xrange = null;

        switch (deployMode) {
            case CLUSTER:
                xrange = jedisCluster.xrange(key, null, null, 200);
                break;
            default:
                xrange = jedis.xrange(key, null, null, 200);
                break;
        }
        AssertUtils.ObjectIsNull(xrange, key + " 获取失败！");

        return analyzeXrevrange(xrange);
    }

    /**
     * 对某个key下加载更多
     *
     * @param key 键值
     * @param id  键id
     * @return
     */
    public ArrayList<Map<String, String>> xrange(String key, String id) {
        List<StreamEntry> xrange = null;

        switch (deployMode) {
            case CLUSTER:
                xrange = jedisCluster.xrange(key, new StreamEntryID(id), null, 200);
                break;
            default:
                xrange = jedis.xrange(key, new StreamEntryID(id), null, 200);
                break;
        }

//        ArrayList<Map<String, String>> resArray = new ArrayList<>();
//        for (int i = xrevrange.size() - 1; i >= 0; i--) {
//            Map<String, String> item = new HashMap<>();
//            item.put("id", xrevrange.get(i).getID().toString());
//            item.put("fields", JSON.toJSONString(xrevrange.get(i).getFields()));
//            resArray.add(item);
//        }
//        return resArray;
        ArrayList<Map<String, String>> resultArrayList = analyzeXrevrange(xrange);
        resultArrayList.remove(0); //移除掉第一个第一个是前一个列表的重复数据
        return resultArrayList;
    }







    @Override
    public void close() {
        if (deployMode != CLUSTER) {
            jedis.close();
            jedis = null;
        } else {
            // cluster 模式下不做关闭操作。
            jedisCluster = null;
        }

    }

    /**
     * 命令行交互模式 执行函数
     *
     * @param command 交互命令 比如：config get databases
     * @return String类型的结果
     */
    public String execCommand(String command) {
        AssertUtils.StringIsNull(command, "与控制台交互命令为空！");
        String[] commandArr = command.split(" ");

        if (commandArr.length < 1)
            throw new ServiceException("命令格式错误!");

        // 操作命令
        String cmdString = commandArr[0];

        /** 拼接参数 **/
        // 参数数组大小
        int argSize = commandArr.length - 1;
        // 参数列表
        String[] arg;//new String[commandArr.length - 1];
        if (argSize >= 1) {
            arg = new String[argSize];
            for (int i = 0; i < arg.length; i++) {
                arg[i] = commandArr[i + 1];
            }
        } else {
            arg = new String[0];
        }

        // 转换为 Jedis 的操作命令
        Protocol.Command cmd = null;
        try {
            cmd = Protocol.Command.valueOf(cmdString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ServiceException("不存在的交互命令：" + cmdString);
        }

        Object commandResult = null;

        switch (deployMode) {
            case CLUSTER:
                commandResult = jedisCluster.sendCommand(UUID.randomUUID().toString(), cmd, arg);
                break;
            default:
                commandResult = jedis.sendCommand(cmd, arg);
                break;
        }

        // 存放返回的结果
        StringBuilder res = new StringBuilder();
        analyzeSendCommandResult(res, commandResult);
        return res.toString();
    }


    /**
     * 递归解析 sendCommand 返回的信息 解析为string
     *
     * @param resultString 存放解析信息的StringBuild
     * @param object       sendCommand 返回的对象
     */
    private void analyzeSendCommandResult(StringBuilder resultString, Object object) {
        if (object instanceof ArrayList) {
            List<Object> ObjectArray = (ArrayList) object;
            for (Object o : ObjectArray) {
                analyzeSendCommandResult(resultString, o);
            }
        } else if (object instanceof byte[]) {
            resultString.append(new String((byte[]) object)).append(nss);
        } else if (object instanceof Long) {
            resultString.append(object).append(nss);
        }
    }

    /**
     * 解析xrevrange命令的返回结果 解析为前端更好处理的结构
     *
     * @param xrevrange
     * @return
     */
    private ArrayList<Map<String, String>> analyzeXrevrange(List<StreamEntry> xrevrange) {
        ArrayList<Map<String, String>> resArray = new ArrayList<>();
        for (StreamEntry streamEntry : xrevrange) {
            Map<String, String> item = new HashMap<>();
            item.put("id", streamEntry.getID().toString());
            item.put("fields", JSON.toJSONString(streamEntry.getFields()));
            resArray.add(item);
        }
        return resArray;
    }

//    test method
//    public Jedis getJedis() {
//        return jedis;
//    }
}
