package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.entity.PiBoxCom;
import com.wecon.box.entity.PiBoxComAddr;
import com.wecon.box.util.ServerMqtt;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.Output;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
		/*
		 * RedisPiBoxActData model = new RedisPiBoxActData(); model.machine_code
		 * = "1001"; model.time = "2017-07-26 10:20:11";
		 * model.act_time_data_list = new ArrayList<PiBoxCom>(); PiBoxCom
		 * com=new PiBoxCom(); com.com="1"; com.addr_list=new
		 * ArrayList<PiBoxComAddr>(); PiBoxComAddr addr=new PiBoxComAddr();
		 * addr.addr="10"; addr.value="0.256"; com.addr_list.add(addr);
		 * model.act_time_data_list.add(com);
		 * redisPiBoxApi.saveRedisPiBoxActData(model);
		 */

		JSONObject data = new JSONObject();
		data.put("d1", 123);
		data.put("d2", 0);
		data.put("reids", redisPiBoxApi.getRedisPiBoxActData("1000"));

		return new Output(data);
	}

	@Description("向盒子发送消息")
	@RequestMapping(value = "/sendboxmsg")
	@WebApi(forceAuth = false, master = true)
	public Output sendBoxMsgTest(@RequestParam("machine_code") String machine_code) {
		List<PiBoxCom> operate_data_list = new ArrayList<PiBoxCom>();

		JSONObject data = new JSONObject();
		return new Output(data);
	}

	/*
	 * 这边写 根据机器码获取redis里面的数据
	 */
	@Description("根据获取的机器码向redis服务器请求数据")
	@RequestMapping(value = "/getActDate")
	public Output getActDate(@RequestParam("machine_code") String machine_code) {
		/*
		 * RedisPiBoxActData model = new RedisPiBoxActData(); model.machine_code
		 * = "1001"; model.time = "2017-07-26 10:20:11";
		 * model.act_time_data_list = new ArrayList<PiBoxCom>(); PiBoxCom
		 * com=new PiBoxCom(); com.com="1"; com.addr_list=new
		 * ArrayList<PiBoxComAddr>(); PiBoxComAddr addr=new PiBoxComAddr();
		 * addr.addr="10"; addr.value="0.256"; com.addr_list.add(addr);
		 * model.act_time_data_list.add(com);
		 * redisPiBoxApi.saveRedisPiBoxActData(model);
		 */

		JSONObject data = new JSONObject();
		data.put("piBoxActDateMode", redisPiBoxApi.getRedisPiBoxActData(machine_code));
		return new Output(data);
	}

	/*
	 * 这边写mosquito的发送请求
	 *
	 */
	@Description("mosquito消息的推送测试")
	@RequestMapping(value = "/putMess")
	public Output putMQTTMess(@RequestParam("machine_code") String machine_code, @RequestParam("com") String com,
			@RequestParam("state") String state, @RequestParam("value") String value,
			@RequestParam("addr_id") String addr_id) throws MqttException {
		ServerMqtt server = new ServerMqtt();
		server.message = new MqttMessage();
		server.message.setQos(1);
		server.message.setRetained(true);

		PiBoxComAddr addr1 = new PiBoxComAddr();
		addr1.state = state;
		addr1.addr_id = addr_id;
		addr1.value = value;
		List<PiBoxCom> operate_data_list = new ArrayList<PiBoxCom>();
		PiBoxCom piBoxCom = new PiBoxCom();
		List<PiBoxComAddr> piBoxComAddrs = new ArrayList<PiBoxComAddr>();
		piBoxCom.com = com;
		piBoxComAddrs.add(addr1);
		piBoxCom.addr_list = piBoxComAddrs;
		operate_data_list.add(piBoxCom);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("act", 2000);
		jsonObject.put("machine_code", machine_code);
		JSONObject oplistObject = new JSONObject();
		oplistObject.put("operate_data_list", operate_data_list);
		jsonObject.put("data", oplistObject);
		jsonObject.put("feedback", 0);
		String message = jsonObject.toJSONString();
		System.out.println(message);
		server.message.setPayload((message).getBytes());
		server.topic11 = server.client.getTopic("pibox/stc/" + machine_code);

		server.publish(server.topic11, server.message);
		System.out.println(server.message.isRetained() + "------ratained状态");
		server.client.disconnect();
		return new Output();
	}
}
