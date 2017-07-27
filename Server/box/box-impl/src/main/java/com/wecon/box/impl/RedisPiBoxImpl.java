package com.wecon.box.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.entity.RedisPiBoxActData;
import com.wecon.box.redis.ConstKey;
import com.wecon.common.redis.RedisManager;
import org.springframework.stereotype.Component;

/**
 * Created by zengzhipeng on 2017/7/26.
 */
@Component
public class RedisPiBoxImpl implements RedisPiBoxApi {

    private final static String groupName = "pibox";

    @Override
    public RedisPiBoxActData getRedisPiBoxActData(String machine_code) {
        String redisKey = String.format(ConstKey.PIBOX_ACT_DATA_KEY, machine_code);
        String jsonStr = RedisManager.get(groupName, redisKey);
        RedisPiBoxActData model = JSON.parseObject(jsonStr, new TypeReference<RedisPiBoxActData>() {
        });
        return model;
    }

    @Override
    public boolean saveRedisPiBoxActData(RedisPiBoxActData model) {
        if (model == null || model.machine_code.isEmpty()) {
            return false;
        } else {
            String jsonStr = JSON.toJSONString(model);
            String redisKey = String.format(ConstKey.PIBOX_ACT_DATA_KEY, model.machine_code);
            RedisManager.set(groupName, redisKey, jsonStr, 0);
            return true;
        }
    }
}
