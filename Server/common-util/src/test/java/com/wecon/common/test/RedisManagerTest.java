package com.wecon.common.test;

import com.wecon.common.redis.RedisConfig;
import com.wecon.common.redis.RedisManager;
import org.junit.Test;

import java.util.*;

/**
 *
 */
public class RedisManagerTest {
    @Test
    public void testRedisManagerForPassword() {
       /* RedisManager redisManager = new RedisManager();

        Map<String, RedisConfig> configMap = new HashMap<>();
        RedisConfig configPassword = new RedisConfig();
        configPassword.setHost("121.40.126.150");
        configPassword.setPort("6379");
        configPassword.setPassword("n66j!rVM");

        configMap.put("password", configPassword);

        redisManager.setRedisConfig(configMap);

        RedisManager.set("password", "testRedisManagerForPassword", "11111", 3);

        String value = RedisManager.get("password", "testRedisManagerForPassword");

        System.out.println("value =" + value);*/
    }

    @Test
    public void testRedisManagerNoPassword() {
        RedisManager redisManager = new RedisManager();

        Map<String, RedisConfig> configMap = new HashMap<>();
        RedisConfig configPassword = new RedisConfig();
        configPassword.setHost("192.168.45.186");
        configPassword.setPort("6379");
//        //configPassword.setPassword("n66j!rVM");
//
        configMap.put("nopassword", configPassword);
        redisManager.setRedisConfig(configMap);

        String redisKey="pibox:actdata:1000";
        String jsonStr="{\"act_time_data_list\":[{\"addr_list\":[{\"addr\":\"10\",\"value\":\"0.256\"}],\"com\":\"1\"}],\"machine_code\":\"1000\",\"time\":\"2017-07-26 10:20:11\"}";
        RedisManager.set("nopassword", redisKey,jsonStr, 0);

        String value = RedisManager.get("nopassword", redisKey);//testRedisManagerNoPassword
        System.out.println("value =" + value);




        /*byte[][] idsBytes = new byte[2][];
        String redisKey = "pibox:112";
        idsBytes[0] = redisKey.getBytes();
        redisKey = "pibox:123";
        idsBytes[1] = redisKey.getBytes();
        List<byte[]> listVals = RedisManager.mget("nopassword", idsBytes);

        if (listVals != null) {
            for (byte[] value : listVals) {
                if (value != null) {
                    try {
                        String str = new String(value);
                        System.out.println(str);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }


        List<String> vals = RedisManager.lrange("nopassword", "pibox:",0,10);
        System.out.println(vals.size());
        for (String v : vals) {
            System.out.println("v =" + v);
        }*/

//        String pushKey = "testPush";
//        byte[][] xxx = new byte[3][];
//        xxx[0] = "aaaa".getBytes();
//        xxx[1] = "bbbb".getBytes();
//        xxx[2] = "cccc".getBytes();
//        RedisManager.lpush("nopassword", pushKey.getBytes(), 120, xxx);
//
//        List<String> vals = RedisManager.lrange("nopassword", pushKey, 0, 1);
//        for (String v : vals)
//        {
//            System.out.println("v =" + v);
//        }
//        RedisManager.lpop("nopassword", pushKey.getBytes());
//        List<String> vals2 = RedisManager.lrange("nopassword", pushKey, 0, 5);
//        for (String v2 : vals2)
//        {
//            System.out.println("v2 =" + v2);
//        }
    }

    @Test
    public void testS() {
        /*RedisManager redisManager = new RedisManager();

        Map<String, RedisConfig> configMap = new HashMap<>();
        RedisConfig configPassword = new RedisConfig();
        configPassword.setHost("caijinwei.win");
        configPassword.setPort("6379");
        //configPassword.setPassword("n66j!rVM");

        configMap.put("nopassword", configPassword);

        redisManager.setRedisConfig(configMap);

        RedisManager.del("nopassword", "test_hset_sort");

//        RedisManager.hset("nopassword", "test_hset_sort", "10075152", "0", 300);
//        RedisManager.hset("nopassword", "test_hset_sort", "10075150", "0", 300);
//        RedisManager.hset("nopassword", "test_hset_sort", "10075154", "0", 300);
//
//        Set<String> keys = RedisManager.hkeys("nopassword", "test_hset_sort");
//        for(String key : keys)
//        {
//            System.out.println(key);
//        }
//        RedisManager.lpush("nopassword", "test_list_sort".getBytes(), 300, "10075152".getBytes());
//        RedisManager.lpush("nopassword", "test_list_sort".getBytes(), 300, "10075150".getBytes());
//        RedisManager.lpush("nopassword", "test_list_sort".getBytes(), 300, "10075154".getBytes());
//
//        List<String> values = RedisManager.lrange("nopassword", "test_list_sort", 0, 10);
//        for(String val : values)
//        {
//            System.out.println(val);
//        }
//
//        boolean bval = RedisManager.sismember("nopassword", "test_list_sort", "10075154");
//        System.out.println(bval);

        RedisManager.zadd("nopassword", "test_sortset_sort", "10075152", 305);
        RedisManager.zadd("nopassword", "test_sortset_sort", "10075150", 301);
        RedisManager.zadd("nopassword", "test_sortset_sort", "10075154", 302);

        Set<String> keys = RedisManager.zrevrange("nopassword", "test_sortset_sort", 0, 100);
        for (String key : keys)
        {
            System.out.println(key);
        }

        Long rank = RedisManager.zrank("nopassword", "test_sortset_sort", "10075154");
        System.out.println(rank == null ? "null" : rank);

        Long rank1 = RedisManager.zrank("nopassword", "test_sortset_sort", "10075100");
        System.out.println(rank1 == null ? "null" : rank1);*/

    }


}
