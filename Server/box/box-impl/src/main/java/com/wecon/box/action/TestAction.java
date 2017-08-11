package com.wecon.box.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DevBindUserApi;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RealHisCfgDataApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.entity.DevBindUser;
import com.wecon.box.entity.Device;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.PiBoxCom;
import com.wecon.box.entity.PiBoxComAddr;
import com.wecon.box.util.ServerMqtt;
import com.wecon.box.entity.PlcInfo;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.RealHisCfgData;
import com.wecon.box.entity.RedisPiBoxActData;
import com.wecon.box.filter.DevBindUserFilter;
import com.wecon.box.filter.RealHisCfgDataFilter;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.common.util.CommonUtils;
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
	@Autowired
	private DeviceApi deviceApi;
	@Autowired
	private DevBindUserApi devBindUserApi;
	@Autowired
	private RealHisCfgApi realHisCfgApi;
	@Autowired
	private PlcInfoApi plcInfoApi;
	@Autowired
	private RealHisCfgDataApi realHisCfgDataApi;

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

	/**
	 * 获取实时数据
	 * 
	 * @return
	 */
	@Description("根据获取的机器码向redis服务器请求实时数据")
	@RequestMapping(value = "/getActData")
	public Output getActData() {
		// 获取机器码
		// DevBindUserFilter devBindUserFilter = new DevBindUserFilter();
		// devBindUserFilter.account_id = 1000002;
		// devBindUserFilter.device_id = 1;
		//
		// // 获取实时数据配置信息
		// RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
		// realHisCfgFilter.account_id = 1000002;
		// realHisCfgFilter.addr_type = -1;
		// realHisCfgFilter.data_type = 0;
		// realHisCfgFilter.his_cycle = -1;
		//
		// // 获取绑定的盒子
		// List<DevBindUser> devBindUserList =
		// devBindUserApi.getDevBindUser(devBindUserFilter);
		//
		// JSONObject json = new JSONObject();
		// JSONArray arr = new JSONArray();
		// JSONObject data = null;
		// for (int y = 0; y < devBindUserList.size(); y++) {
		// DevBindUser devBindUser = devBindUserList.get(y);
		// Device device = deviceApi.getDevice(devBindUser.device_id);
		// String device_machine = device.machine_code;
		// // 通过机器码去redis中获取数据
		// RedisPiBoxActData redisPiBoxActData =
		// redisPiBoxApi.getRedisPiBoxActData(device_machine);
		// List<PiBoxCom> act_time_data_list =
		// redisPiBoxActData.act_time_data_list;
		//
		// // 获取device下的所有plc
		// List<PlcInfo> plcInfoList =
		// plcInfoApi.getListPlcInfo(device.device_id);
		//
		// for (int a = 0; a < plcInfoList.size(); a++) {
		// PlcInfo plcInfo = plcInfoList.get(a);
		// // 输出该plc下的所有实时数据配置
		// realHisCfgFilter.plc_id = plcInfo.plc_id;
		// List<RealHisCfg> realHisCfgList =
		// realHisCfgApi.getRealHisCfg(realHisCfgFilter);
		// // 如果该plc下没有实时数据配置，直接进行下个plc判断
		// if (realHisCfgList == null || realHisCfgList.size() < 1) {
		// continue;
		// }
		// for (int i = 0; i < realHisCfgList.size(); i++) {
		// RealHisCfg realHisCfg = realHisCfgList.get(i);
		// data = new JSONObject();
		// data.put("state", realHisCfg.state);
		// data.put("addr_type", realHisCfg.addr_type);
		// data.put("addr", realHisCfg.addr);
		// data.put("name", realHisCfg.name);
		// data.put("describe", realHisCfg.describe);
		// for (int j = 0; j < act_time_data_list.size(); j++) {
		// PiBoxCom piBoxCom = act_time_data_list.get(j);
		//
		// if (plcInfo.plc_id == Long.parseLong(piBoxCom.com)) {
		//
		// List<PiBoxComAddr> addr_list = piBoxCom.addr_list;
		// for (int x = 0; x < addr_list.size(); x++) {
		// PiBoxComAddr piBoxComAddr = addr_list.get(x);
		//
		// if (realHisCfg.id == Long.parseLong(piBoxComAddr.addr_id)) {
		//
		// data.put("state", piBoxComAddr.state);
		// data.put("value", piBoxComAddr.value);
		//
		// }
		//
		// }
		//
		// }
		//
		// }
		//
		// arr.add(data);
		//
		// }
		//
		// }
		//
		// }
		// json.put("piBoxActDateMode", arr);
		// return new Output(json);
		return null;
	}

	@Description("获取改账户下的plc和对应的监控点")
	@RequestMapping(value = "/getComMonitor")
	public Output getComMonitor() {
		// 获取绑定的盒子
		// DevBindUserFilter devBindUserFilter = new DevBindUserFilter();
		// devBindUserFilter.account_id = 1000002;
		// devBindUserFilter.device_id = 1;
		//
		// // 获取历史数据配置信息
		// RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
		// realHisCfgFilter.account_id = 1000002;
		// realHisCfgFilter.addr_type = -1;
		// realHisCfgFilter.data_type = 1;
		// realHisCfgFilter.his_cycle = -1;
		// realHisCfgFilter.state = 1;
		//
		// // 获取用户绑定的机器
		// List<DevBindUser> devBindUserList =
		// devBindUserApi.getDevBindUser(devBindUserFilter);
		//
		// JSONObject json = new JSONObject();
		// JSONArray arr = new JSONArray();
		// JSONArray arrmonitors = null;
		// JSONObject data = null;
		// JSONObject datamonitors = null;
		// for (int i = 0; i < devBindUserList.size(); i++) {
		// DevBindUser devBindUser = devBindUserList.get(0);
		// Device device = deviceApi.getDevice(devBindUser.device_id);
		// // 获取device下的所有plc
		// List<PlcInfo> plcInfoList =
		// plcInfoApi.getListPlcInfo(device.device_id);
		// for (int j = 0; j < plcInfoList.size(); j++) {
		// PlcInfo plcInfo = plcInfoList.get(j);
		//
		// realHisCfgFilter.plc_id = plcInfo.plc_id;
		// List<RealHisCfg> realHisCfgList =
		// realHisCfgApi.getRealHisCfg(realHisCfgFilter);
		// // 如果该plc下没有实时数据配置，直接进行下个plc判断
		// if (realHisCfgList == null || realHisCfgList.size() < 1) {
		// continue;
		// }
		// data = new JSONObject();
		// arrmonitors = new JSONArray();
		// data.put("com", plcInfo.port);
		// data.put("plc_id", plcInfo.plc_id);
		// for (int a = 0; a < realHisCfgList.size(); a++) {
		// RealHisCfg realHisCfg = realHisCfgList.get(a);
		// // 获取plc下的每个监控点
		// datamonitors = new JSONObject();
		// datamonitors.put("monitorid", realHisCfg.id);
		// datamonitors.put("monitordata", realHisCfg.name);
		// arrmonitors.add(datamonitors);
		// }
		// data.put("arrmonitors", arrmonitors);
		// arr.add(data);
		// }
		//
		// }
		// json.put("comMonitor", arr);
		// return new Output(json);
		return null;

	}

	/**
	 * 获取历史数据
	 * 
	 * @return
	 */
	@Description("获取历史数据")
	@RequestMapping(value = "/getHisData")
	public Output getHisData(@RequestParam("real_his_cfg_id") String real_his_cfg_id,
			@RequestParam("start_date") String start_date, @RequestParam("end_date") String end_date) {
				return null;

//		RealHisCfgDataFilter realHisCfgDataFilter = new RealHisCfgDataFilter();
//		if (!CommonUtils.isNullOrEmpty(real_his_cfg_id)) {
//			realHisCfgDataFilter.real_his_cfg_id = Long.parseLong(real_his_cfg_id);
//		}
//		realHisCfgDataFilter.start_date = start_date;
//		realHisCfgDataFilter.end_date = end_date;
//		realHisCfgDataFilter.state = 1;
//
//		List<RealHisCfgData> realHisCfgDataList = realHisCfgDataApi.getRealHisCfgData(realHisCfgDataFilter);
//		JSONObject data = new JSONObject();
//		data.put("realHisCfgDataList", realHisCfgDataList);
//		return new Output(data);

	}

	/*
	 * 这边写mosquito的发送请求
	 *
	 */
	@Description("mosquito消息的推送测试")
	@RequestMapping(value = "/putMess")
	public Output putMQTTMess(@RequestParam("machine_code") String machine_code, @RequestParam("com") String com,
			 @RequestParam("value") String value,
			@RequestParam("addr_id") String addr_id) throws MqttException {
		ServerMqtt server = new ServerMqtt();
		server.message = new MqttMessage();
		server.message.setQos(1);
		server.message.setRetained(true);

		PiBoxComAddr addr1 = new PiBoxComAddr();
//		addr1.state = state;
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
