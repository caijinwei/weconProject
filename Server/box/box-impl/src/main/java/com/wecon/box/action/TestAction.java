package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.utils.api.ItemsApi;
import com.wecon.box.utils.entity.Items;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * Created by zengzp on 2017/7/18.
 */
@RestController
@RequestMapping("testact")
public class TestAction {
    @Autowired
    private ItemsApi itemsApi;

    @Description("test")
    @RequestMapping(value = "/gd")
    @WebApi(forceAuth = false, master = true)
    public Output getD() {
        JSONObject data = new JSONObject();
        data.put("d1", 123);
        data.put("d2", 0);
        Items it =new Items();
        it.setId(100);
        data.put("items",it);

        return new Output(data);
    }
}
