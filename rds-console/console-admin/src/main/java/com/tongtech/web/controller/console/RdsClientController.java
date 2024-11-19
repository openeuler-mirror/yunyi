package com.tongtech.web.controller.console;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tongtech.common.annotation.Log;
import com.tongtech.common.core.controller.BaseController;

import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.common.enums.BusinessType;
import com.tongtech.common.utils.AssertUtils;
import com.tongtech.common.utils.StringUtils;
import com.tongtech.console.enums.DeployModeEnum;
import com.tongtech.console.service.RdsClientService;
import com.tongtech.console.utils.JedisDataClient;
import com.tongtech.web.controller.console.vo.RdsVo;
import com.tongtech.web.controller.console.vo.ScanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.io.UnsupportedEncodingException;
import java.util.*;

@RestController
@RequestMapping("/web-api/console/rdsClient")
public class RdsClientController extends BaseController {

    private static int LIST_FETCH_SIZE = 16; //获得key list时，每次取得的条数

    private static String NONE_CURSOR = "0"; //开始的cursor，scan到结尾的cursor都是这个


    @Autowired
    private RdsClientService rdsService;

    @PostMapping("/getDB")
    public AjaxResult getDB(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            int size;
            if (jedisClient.getDeployMode() == DeployModeEnum.CLUSTER) {
                size = 1;
            } else {
                List<String> list = jedisClient.configGet("databases");
                if(list.size() > 0) {
                    size = Integer.valueOf(list.get(1));
                }
                else {
                    size  = 1;
                }
            }

            JSONObject result = new JSONObject();
            JSONArray array = new JSONArray();
            for (int i = 0; i < size; i++) {
                JSONObject obj = new JSONObject();
                obj.put("key", "DB" + i);
                obj.put("value", i);
                array.add(obj);
            }

            result.put("dbList", array);
            result.put("serviceId", rdsVo.getServiceId());
            return AjaxResult.success(result);
        }
    }

    @GetMapping("/disconnectAll")
    public AjaxResult disconnectAll() {
        rdsService.deleteConnections();
        return AjaxResult.success();
    }


    @Log(title = "RDS数据维护-updateTtl", businessType = BusinessType.UPDATE)
    @PostMapping("updateTtl")
    public AjaxResult updateTtl(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            if (rdsVo.getExpireTime() < 0) {
                jedisClient.persist(rdsVo.getKey());
            } else {
                jedisClient.expire(rdsVo.getKey(), (long) rdsVo.getExpireTime());
            }
            return AjaxResult.success();
        }
    }

    @Log(title = "RDS数据维护-setKey", businessType = BusinessType.INSERT)
    @PostMapping("setKey")
    public AjaxResult setKey(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            if (rdsVo.getExpireTime() <= 0) {
                jedisClient.set(rdsVo.getKey(), rdsVo.getValue());
            } else {
                jedisClient.setex(rdsVo.getKey(), rdsVo.getExpireTime(), rdsVo.getValue());
            }
            return AjaxResult.success("设置成功");
        }
    }

    @PostMapping("getType")
    public AjaxResult getType(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            String result = jedisClient.type(rdsVo.getKey());
            Long ttl = jedisClient.ttl(rdsVo.getKey());

            Map<String, Object> map = new HashMap<>();
            map.put("result", StringUtils.defaultString(result));
            map.put("ttl", ttl);
            return AjaxResult.success(map);
        }
    }

    @PostMapping("getKey")
    public AjaxResult getKey(@RequestBody RdsVo rdsVo) throws UnsupportedEncodingException {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            JSONObject obj = new JSONObject();
            String value = jedisClient.get(rdsVo.getKey());
            Object type = jedisClient.type(rdsVo.getKey());
            Long ttl = jedisClient.ttl(rdsVo.getKey());

            obj.put("value", value);
            obj.put("type", type);
            obj.put("size", value == null ? 0 : value.getBytes("UTF-8").length + "B");
            obj.put("ttl", ttl);
            return AjaxResult.success(obj);
        }
    }

    @PostMapping("keyList")
    public AjaxResult keyList(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            ScanParams params = new ScanParams();
            if (StringUtils.isEmpty(rdsVo.getKey())) {
                params.match("*");
            } else {
                if (rdsVo.getKeyPatternPrecise()) {
                    params.match(rdsVo.getKey());
                } else {
                    params.match("*" + rdsVo.getKey() + "*");
                }
            }

            //scan函数返回下次开始的 cursor， 如果是"0"表示没有数据或已经到了结尾。前端页面会不断传回上次scan的cursor
            //endpoint是上次查询的节点key, 格式如："192.168.0.100:6379"
            ScanVO resultVO = new ScanVO();
            if (jedisClient.getDeployMode() == DeployModeEnum.CLUSTER) {
                //设置是否是第一页, endpoint为空 并且 cursor 为 "0"
                boolean firstPage = StringUtils.isEmpty(rdsVo.getEndpoint()) && NONE_CURSOR.equals(rdsVo.getCursor());
                resultVO.setFirstPage(firstPage);


                SortedSet<String> sortedEndpoints = new TreeSet<String>(jedisClient.getMasterNodes().keySet());

                String nextEndpoint = (firstPage) ? sortedEndpoints.first() : rdsVo.getEndpoint();  //下一步要scan的 endpoint
                String nextCursor = (firstPage) ? NONE_CURSOR : rdsVo.getCursor();                  //下一步要scan的 cursor
                int fetchedSize = 0;
                for (String endpoint : sortedEndpoints) {

                    if (nextEndpoint == null) { //循环的上一个 endpoint 中的数据已经取完
                        nextEndpoint = endpoint;
                    }

                    int fetchSize = LIST_FETCH_SIZE - fetchedSize; //剩余有多少数据需要取出
                    if (fetchSize > 0 && endpoint.equals(nextEndpoint)) {
                        JedisPool pool = jedisClient.getMasterNodes().get(endpoint);
                        try (Jedis jedis = pool.getResource()) {
                            params.count(fetchSize);
                            ScanResult<String> scanRes = jedis.scan(nextCursor, params);
                            fetchedSize = resultVO.addScanResult(scanRes);
                            resultVO.setEndpoint(endpoint);

                            nextCursor = scanRes.getCursor();
                            if (NONE_CURSOR.equals(nextCursor)) {
                                //已经取光当前的 endpoint 中的数据，标识需要取下一个
                                nextEndpoint = null;
                            }

                            if (fetchedSize >= LIST_FETCH_SIZE) { //
                                break;
                            }
                        }
                    }
                }

                return AjaxResult.success(resultVO);
            } else {
                params.count(LIST_FETCH_SIZE);
                resultVO.setFirstPage(rdsVo.getCursor().equals("0"));
                resultVO.addScanResult(jedisClient.scan(rdsVo.getCursor(), params));
                return AjaxResult.success(resultVO);
            }

        }

    }

    @Log(title = "RDS数据维护-delKey", businessType = BusinessType.DELETE)
    @PostMapping("delKey")
    public AjaxResult delKey(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.del(rdsVo.getKey());
            return AjaxResult.success(result);
        }
    }

    @Log(title = "RDS数据维护-delKeys", businessType = BusinessType.DELETE)
    @PostMapping("delKeys")
    public AjaxResult delKeys(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.del(rdsVo.getKeys());
            return AjaxResult.success(result);
        }
    }


    @PostMapping("hget")
    public AjaxResult hget(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            String result = jedisClient.hget(rdsVo.getKey(), rdsVo.getField());
            return AjaxResult.success(StringUtils.defaultString(result));
        }
    }

    @Log(title = "RDS数据维护-hdel", businessType = BusinessType.DELETE)
    @PostMapping("hdel")
    public AjaxResult hdel(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.hdel(rdsVo.getKey(), rdsVo.getField());
            return AjaxResult.success(result);
        }
    }

    @PostMapping("hgetAll")
    public AjaxResult hgetAll(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Map map = jedisClient.hgetAll(rdsVo.getKey());
            JSONArray array = new JSONArray();
            for (Object key : map.keySet()) {
                JSONObject obj = new JSONObject();
                obj.put("key", key);
                obj.put("value", map.get(key));
                array.add(obj);
            }
            return AjaxResult.success(array);
        }
    }

    @Log(title = "RDS数据维护-hset", businessType = BusinessType.INSERT)
    @PostMapping("hset")
    public AjaxResult hset(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.hset(rdsVo.getKey(), rdsVo.getField(), rdsVo.getValue());
            return AjaxResult.success(result);
        }
    }

    @Log(title = "RDS数据维护-lpush", businessType = BusinessType.INSERT)
    @PostMapping("lpush")
    public AjaxResult lpush(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.lpush(rdsVo.getKey(), rdsVo.getValue());
            return AjaxResult.success(result);
        }
    }

    @Log(title = "RDS数据维护-lset", businessType = BusinessType.INSERT)
    @PostMapping("lset")
    public AjaxResult lset(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            String result = jedisClient.lset(rdsVo.getKey(), rdsVo.getIndex(),  rdsVo.getValue());

            return AjaxResult.success(result);
        }
    }

    @PostMapping("lrange")
    public AjaxResult lrange(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            List<String> list = jedisClient.lrange(rdsVo.getKey(), rdsVo.getStart(), rdsVo.getEnd());

            JSONArray array = new JSONArray();
            for (Object e : list) {
                JSONObject obj = new JSONObject();
                obj.put("value", e);
                array.add(obj);
            }
            return AjaxResult.success(array);
        }

    }

    @Log(title = "RDS数据维护-lrem", businessType = BusinessType.DELETE)
    @PostMapping("lrem")
    public AjaxResult lrem(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.lrem(rdsVo.getKey(), rdsVo.getCount(), rdsVo.getValue());
            return AjaxResult.success(result);
        }
    }

    //添加、修改
    @Log(title = "RDS数据维护-sadd", businessType = BusinessType.INSERT)
    @PostMapping("sadd")
    public AjaxResult sadd(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.sadd(rdsVo.getKey(), rdsVo.getMember());
            return AjaxResult.success(result);
        }
    }

    //修改
    @Log(title = "RDS数据维护-sedit", businessType = BusinessType.UPDATE)
    @PostMapping("sedit")
    public AjaxResult sedit(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.sadd(rdsVo.getKey(), rdsVo.getNewMember());
            return AjaxResult.success(result);
        }
    }

    //查询
    @PostMapping("smembers")
    public AjaxResult smembers(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Set<String> setList = jedisClient.smembers(rdsVo.getKey());

            JSONArray array = new JSONArray();
            for (Object e : setList) {
                JSONObject obj = new JSONObject();
                obj.put("value", e);
                array.add(obj);
            }

            return AjaxResult.success(array);
        }
    }

    //删除
    @Log(title = "RDS数据维护-srem", businessType = BusinessType.DELETE)
    @PostMapping("srem")
    public AjaxResult srem(@RequestBody RdsVo rdsVo) {

        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.srem(rdsVo.getKey(), rdsVo.getMember());
            return AjaxResult.success(result);
        }

    }

    //zset-添加
    @Log(title = "RDS数据维护-zadd", businessType = BusinessType.INSERT)
    @PostMapping("zadd")
    public AjaxResult zadd(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.zadd(rdsVo.getKey(), rdsVo.getScore(), rdsVo.getMember());
            return AjaxResult.success(result);
        }
    }

    //zset-删除
    @Log(title = "RDS数据维护-zrem", businessType = BusinessType.DELETE)
    @PostMapping("zrem")
    public AjaxResult zrem(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Long result = jedisClient.zrem(rdsVo.getKey(), rdsVo.getMember());
            return AjaxResult.success(result);
        }
    }

    //zset-查询
    @PostMapping("zsetList")
    public AjaxResult zsetList(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            String key = rdsVo.getKey();
            Set<String> sets = jedisClient.zrange(key, 0, jedisClient.zcard(key));

            JSONArray array = new JSONArray();
            for (String member : sets) {
                JSONObject obj = new JSONObject();
                obj.put("member", member);
                obj.put("score", jedisClient.zscore(key, member));
                array.add(obj);
            }
            return AjaxResult.success(array);
        }

    }

    //zset 修改
    @Log(title = "RDS数据维护-zsetEdit", businessType = BusinessType.UPDATE)
    @PostMapping("zsetEdit")
    public AjaxResult zsetEdit(@RequestBody RdsVo rdsVo) {
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            this.zrem(rdsVo);
            Long result = jedisClient.zadd(rdsVo.getKey(), rdsVo.getScore(), rdsVo.getNewMember());
            return AjaxResult.success(result);
        }
    }


    @Log(title = "命令行交互", businessType = BusinessType.OTHER)
    @PostMapping("execCommand")
    public AjaxResult rdsExecCommand(@RequestBody RdsVo rdsVo) {
        AssertUtils.ObjectIsNull(rdsVo, "参数错误！");
        AssertUtils.ObjectIsNull(rdsVo.getServiceId(), "参数错误，参数[serviceId]为空！");

        String res;
        try {
            JedisDataClient jedisClient = selectClientDB(rdsVo);
            res = jedisClient.execCommand(rdsVo.getCommand());
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
        return AjaxResult.success("交互成功", res);
    }


    /**
     * stream 类型 操作
     **/

    /**
     * 新增
     *
     * @param rdsVo key ，value
     * @return
     */
    @Log(title = "RDS数据维护-stream-xadd", businessType = BusinessType.INSERT)
    @PostMapping("xadd")
    public AjaxResult xadd(@RequestBody RdsVo rdsVo) {
        AssertUtils.ObjectIsNull(rdsVo, "参数错误！");
        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            String xadd = jedisClient.xadd(rdsVo.getKey(), rdsVo.getValue());
            HashMap<String, String> result = new HashMap<>();
            result.put("id", xadd);
            return AjaxResult.success(result);
        }
    }

    @Log(title = "RDS数据维护-stream-xdel", businessType = BusinessType.DELETE)
    @PostMapping("xdel")
    public AjaxResult xdel(@RequestBody RdsVo rdsVo) {

        AssertUtils.ObjectIsNull(rdsVo, "参数错误！");
        AssertUtils.StringIsNull(rdsVo.getKey(), "参数 key 为空！");
        AssertUtils.StringIsNull(rdsVo.getId(), "参数 id 为空！");

        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            Boolean xdel = jedisClient.xdel(rdsVo.getKey(), rdsVo.getId());
            return AjaxResult.success(xdel);
        }
    }

    @Log(title = "RDS数据维护-stream-xrevrange", businessType = BusinessType.OTHER)
    @PostMapping("xrevrange")
    public AjaxResult xrevrange(@RequestBody RdsVo rdsVo) {

        AssertUtils.ObjectIsNull(rdsVo, "参数错误！");
        AssertUtils.StringIsNull(rdsVo.getKey(), "参数 key 为空！");

        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            ArrayList<Map<String, String>> xrevrange = jedisClient.xrevrange(rdsVo.getKey());
            return AjaxResult.success(xrevrange);
        }
    }

    @Log(title = "RDS数据维护-stream-loadMore", businessType = BusinessType.OTHER)
    @PostMapping("xrevrangeLoadMore")
    public AjaxResult xrevrangeLoadMore(@RequestBody RdsVo rdsVo) {

        AssertUtils.ObjectIsNull(rdsVo, "参数错误！");
        AssertUtils.StringIsNull(rdsVo.getKey(), "参数 key 为空！");
        AssertUtils.StringIsNull(rdsVo.getId(), "参数 id 为空！");

        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            ArrayList<Map<String, String>> xrevrange = jedisClient.xrevrange(rdsVo.getKey(), rdsVo.getId());
            return AjaxResult.success(xrevrange);
        }
    }

    @Log(title = "RDS数据维护-stream-xrange", businessType = BusinessType.OTHER)
    @PostMapping("xrange")
    public AjaxResult xrange(@RequestBody RdsVo rdsVo) {

        AssertUtils.ObjectIsNull(rdsVo, "参数错误！");
        AssertUtils.StringIsNull(rdsVo.getKey(), "参数 key 为空！");

        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            ArrayList<Map<String, String>> xrange = jedisClient.xrange(rdsVo.getKey());
            return AjaxResult.success(xrange);
        }
    }

    @Log(title = "RDS数据维护-stream-loadMore", businessType = BusinessType.OTHER)
    @PostMapping("xrangeLoadMore")
    public AjaxResult xrangeLoadMore(@RequestBody RdsVo rdsVo) {

        AssertUtils.ObjectIsNull(rdsVo, "参数错误！");
        AssertUtils.StringIsNull(rdsVo.getKey(), "参数 key 为空！");
        AssertUtils.StringIsNull(rdsVo.getId(), "参数 id 为空！");

        try (JedisDataClient jedisClient = selectClientDB(rdsVo)) {
            ArrayList<Map<String, String>> xrange = jedisClient.xrange(rdsVo.getKey(), rdsVo.getId());
            return AjaxResult.success(xrange);
        }
    }


    private JedisDataClient selectClientDB(RdsVo rdsVo) {
        JedisDataClient jedisClient = rdsService.getConnectionClient(rdsVo.getServiceId());

        if (jedisClient.getDeployMode() != DeployModeEnum.CLUSTER) {
            jedisClient.select(rdsVo.getDb());
        }

        return jedisClient;
    }

}
