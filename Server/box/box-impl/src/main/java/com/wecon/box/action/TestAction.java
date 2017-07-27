package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.entity.PiBoxCom;
import com.wecon.box.entity.PiBoxComAddr;
import com.wecon.box.entity.RedisPiBoxActData;
import com.wecon.box.param.StcOperateData;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengzp on 2017/7/18.
 */
@RestController
@RequestMapping("testact")
public class TestAction {

    @Autowired
    private RedisPiBoxApi redisPiBoxApi;

    @Description("test")
    @RequestMapping(value = "/gd")
    @WebApi(forceAuth = false, master = true)
    public Output getD() {
        /*RedisPiBoxActData model = new RedisPiBoxActData();
        model.machine_code = "1001";
        model.time = "2017-07-26 10:20:11";
        model.act_time_data_list = new ArrayList<PiBoxCom>();
        PiBoxCom com=new PiBoxCom();
        com.com="1";
        com.addr_list=new ArrayList<PiBoxComAddr>();
        PiBoxComAddr addr=new PiBoxComAddr();
        addr.addr="10";
        addr.value="0.256";
        com.addr_list.add(addr);
        model.act_time_data_list.add(com);
        redisPiBoxApi.saveRedisPiBoxActData(model);*/


        JSONObject data = new JSONObject();
        data.put("d1", 123);
        data.put("d2", 0);
        data.put("reids", redisPiBoxApi.getRedisPiBoxActData("1000"));

        return new Output(data);
    }

    @Description("向盒子发送消息")
    @RequestMapping(value = "/sendboxmsg")
    @WebApi(forceAuth = false, master = true)
    public Output sendBoxMsgTest() {
        List<PiBoxCom> operate_data_list = new ArrayList<PiBoxCom>();

        JSONObject data = new JSONObject();
        return new Output(data);
    }


}
