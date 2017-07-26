package com.wecon.common.test;

import com.wecon.common.redis.RedisConfig;
import com.wecon.common.redis.RedisManager;
import org.junit.Test;

import java.util.*;

/**
 * Created by zengzhipeng on 2016/2/15.
 */
public class RedisManagerTest
{
    @Test
    public void testRedisManagerForPassword()
    {
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
    public void testRedisManagerNoPassword()
    {
        RedisManager redisManager = new RedisManager();

        Map<String, RedisConfig> configMap = new HashMap<>();
        RedisConfig configPassword = new RedisConfig();
        configPassword.setHost("caijinwei.win");
        configPassword.setPort("6379");
//        //configPassword.setPassword("n66j!rVM");
//
        configMap.put("nopassword", configPassword);
        redisManager.setRedisConfig(configMap);

//        RedisManager.set("nopassword", "testRedisManagerNoPassword", "11111", 0);

        String value = RedisManager.get("nopassword", "mykey");//testRedisManagerNoPassword

        System.out.println("value =" + value);

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
    public void testS()
    {
        /*RedisManager redisManager = new RedisManager();

        Map<String, RedisConfig> configMap = new HashMap<>();
        RedisConfig configPassword = new RedisConfig();
        configPassword.setHost("172.17.149.92");
        configPassword.setPort("6379");
        //configPassword.setPassword("n66j!rVM");

        configMap.put("nopassword", configPassword);

        redisManager.setRedisConfig(configMap);

        RedisManager.del("nopassword", "test_hset_sort");*/

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

        /*RedisManager.zadd("nopassword", "test_sortset_sort", "10075152", 305);
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
