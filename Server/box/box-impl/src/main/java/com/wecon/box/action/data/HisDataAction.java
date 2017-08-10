package com.wecon.box.action.data;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RealHisCfgDataApi;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.RealHisCfgData;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.filter.RealHisCfgDataFilter;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.core.Output;

/**
 * @author lanpenghui 2017年8月9日下午1:45:52
 */
@RestController
@RequestMapping("hisDataAction")
public class HisDataAction {
	@Autowired
	private RealHisCfgApi realHisCfgApi;
	@Autowired
	private RealHisCfgDataApi realHisCfgDataApi;

	// @WebApi(forceAuth = true, master = true)
	@Description("获取改账户下的plc和对应的监控点")
	@RequestMapping(value = "/getComMonitor")
	public Output getComMonitor() {

		// 获取绑定的盒子
		// DevBindUserFilter devBindUserFilter = new DevBindUserFilter();
		// devBindUserFilter.account_id = 1000002;
		// devBindUserFilter.device_id = 1;

		// 获取历史数据配置信息
		RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
		realHisCfgFilter.account_id = 1000002;
		realHisCfgFilter.addr_type = -1;
		realHisCfgFilter.data_type = 1;
		realHisCfgFilter.his_cycle = -1;
		realHisCfgFilter.state = 1;

		List<RealHisCfgDevice> realHisCfgList = realHisCfgApi.getRealHisCfg(realHisCfgFilter);
		JSONObject json = new JSONObject();
		JSONArray arr = new JSONArray();
		JSONObject data = null;

		for (int a = 0; a < realHisCfgList.size(); a++) {
			RealHisCfg realHisCfg = realHisCfgList.get(a);
			data = new JSONObject();
			data.put("monitorid", realHisCfg.id);
			data.put("monitordata", realHisCfg.name);
			arr.add(data);
		}

		// 获取用户绑定的机器
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
		json.put("monitors", arr);
		return new Output(json);

	}

	/**
	 * 获取历史数据
	 * 
	 * @return
	 */
	// @WebApi(forceAuth = true, master = true)
	@Description("获取历史数据")
	@RequestMapping(value = "/getHisData")
	public Output getHisData(@RequestParam("real_his_cfg_id") String real_his_cfg_id,
			@RequestParam("start_date") String start_date, @RequestParam("end_date") String end_date) {

		RealHisCfgDataFilter realHisCfgDataFilter = new RealHisCfgDataFilter();
		if (!CommonUtils.isNullOrEmpty(real_his_cfg_id)) {
			realHisCfgDataFilter.real_his_cfg_id = Long.parseLong(real_his_cfg_id);
		}
		realHisCfgDataFilter.start_date = start_date;
		realHisCfgDataFilter.end_date = end_date;
		realHisCfgDataFilter.state = 1;

		List<RealHisCfgData> realHisCfgDataList = realHisCfgDataApi.getRealHisCfgData(realHisCfgDataFilter);
		JSONObject data = new JSONObject();
		data.put("realHisCfgDataList", realHisCfgDataList);
		return new Output(data);

	}

}
