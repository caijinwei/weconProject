package com.wecon.box.action.dbsel;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.Account;
import com.wecon.box.entity.Page;
import com.wecon.common.redis.RedisManager;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zengzhipeng on 2017/12/5.
 */
@Label("获取Redis数据")
@RestController
public class RedisAction {
    @RequestMapping("redis/getvalue")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output getViewUserList(@RequestParam("redisKey") String redisKey) {
        String value = RedisManager.get(ConstKey.REDIS_GROUP_NAME, redisKey);
        JSONObject data = new JSONObject();
        data.put("value", value);
        return new Output(data);
    }
}
