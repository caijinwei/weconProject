package com.wecon.box.action.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AlarmCfgDataApi;
import com.wecon.box.entity.AlarmCfgDataAlarmCfg;
import com.wecon.box.filter.AlarmCfgDataFilter;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Client;
import com.wecon.restful.core.Output;

/**
 * @author lanpenghui 2017年8月9日下午2:00:17
 */
@RestController
@RequestMapping("alarmDataAction")
public class AlarmDataAction {

	@Autowired
	private AlarmCfgDataApi alarmCfgDataApi;

	@WebApi(forceAuth = true, master = true)
	@Description("获取当前报警")
	@RequestMapping(value = "/getNowAlarmData")
	public Output getNowAlarmData() {
		Client client = AppContext.getSession().client;
		
		List<AlarmCfgDataAlarmCfg> alarmCfgDataAlarmCfgList=null;

		/** 超级管理 **/
		if (client.userInfo.getUserType() == 0) {
			AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
		
			alarmCfgDataAlarmCfgList = alarmCfgDataApi.getAlarmCfgData(filter);

		} else if (client.userInfo.getUserType() == 1) {
			/** 管理 **/
			AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
			filter.account_id=client.userId;
			
			alarmCfgDataAlarmCfgList = alarmCfgDataApi.getAlarmCfgData(filter);

		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/

		}
		JSONObject json = new JSONObject();
		JSONArray arr = new JSONArray();
		JSONObject data = null;

		for (int i = 0; i < alarmCfgDataAlarmCfgList.size(); i++) {
			AlarmCfgDataAlarmCfg alarmCfgDataAlarmCfg = alarmCfgDataAlarmCfgList.get(i);

			data = new JSONObject();
			data.put("name", alarmCfgDataAlarmCfg.name);// 名称
			data.put("text", alarmCfgDataAlarmCfg.text);// 信息记录
			data.put("state", alarmCfgDataAlarmCfg.state);// 报警值状态
			data.put("value", alarmCfgDataAlarmCfg.value);// 报警值
			data.put("alarm_cfg_id", alarmCfgDataAlarmCfg.alarm_cfg_id);// 报警值
			data.put("monitor_time", alarmCfgDataAlarmCfg.monitor_time);// 时间
			arr.add(data);

		}

		return new Output(json);
		// 获取当前用户的报警配置
		// List<AlarmCfg> alarmCfgList = alarmCfgApi.getAlarmCfg(1000002);
		// // 报警触发条件
		// AlarmTriggerFilter alarmTriggerFilter = new AlarmTriggerFilter();
		// // 报警数据条件
		// AlarmCfgDataFilter alarmCfgDatafilter = new AlarmCfgDataFilter();
		//
		// JSONObject json = new JSONObject();
		// JSONArray arr = new JSONArray();
		// JSONObject data = null;
		// for (int i = 0; i < alarmCfgList.size(); i++) {
		// AlarmCfg alarmCfg = alarmCfgList.get(i);
		// alarmTriggerFilter.alarmcfg_id = alarmCfg.alarmcfg_id;
		// alarmCfgDatafilter.alarm_cfg_id = alarmCfg.alarmcfg_id;
		// // 通过配置报警id得到对应的报警数据
		// List<AlarmCfgData> alarmCfgDataList =
		// alarmCfgDataApi.getAlarmCfgData(alarmCfgDatafilter);
		// if (alarmCfgDataList == null || alarmCfgDataList.size() < 1) {
		// continue;
		// }
		// // 通过配置报警id得到对应的报警触发条件
		// List<AlarmTrigger> alarmTriggerList =
		// alarmTriggerApi.getAlarmTrigger(alarmTriggerFilter);
		// if (alarmTriggerList == null || alarmTriggerList.size() < 1) {
		// continue;
		// }
		// for (int j = 0; j < alarmTriggerList.size(); j++) {
		// AlarmTrigger alarmTrigger = alarmTriggerList.get(j);
		// for (int k = 0; k < alarmCfgDataList.size(); k++) {
		// AlarmCfgData alarmCfgData = alarmCfgDataList.get(k);
		// if (alarmCfgData.alarm_cfg_id == alarmTrigger.alarmcfg_id) {
		// data = new JSONObject();
		// data.put("name", alarmCfg.name);// 名称
		// data.put("text", alarmCfg.text);// 信息记录
		// data.put("state", alarmCfgData.state);// 报警值状态
		// data.put("value", alarmCfgData.value);// 报警值
		// data.put("alarm_cfg_id", alarmCfgData.alarm_cfg_id);// 报警值
		// data.put("monitor_time", alarmCfgData.monitor_time);// 时间
		// arr.add(data);
		//
		// }
		// }
		// }
		//
		// }
		// json.put("alarmData", arr);
		// return new Output(json);

	}

	// @WebApi(forceAuth = true, master = true)
	@Description("获取历史报警")
	@RequestMapping(value = "/getHisAlarmData")
	public Output getHisAlarmData(@RequestParam("alarm_cfg_id") String alarm_cfg_id, @RequestParam("name") String name,
			@RequestParam("start_date") String start_date, @RequestParam("end_date") String end_date) {
		return null;
		// 获取当前用户的报警配置
		// List<AlarmCfg> alarmCfgList = alarmCfgApi.getAlarmCfg(1000002);
		// // 报警触发条件
		// AlarmTriggerFilter alarmTriggerFilter = new AlarmTriggerFilter();
		// // 报警数据条件
		// AlarmCfgDataFilter alarmCfgDatafilter = new AlarmCfgDataFilter();
		// alarmCfgDatafilter.alarm_cfg_id = Long.parseLong(alarm_cfg_id);
		// alarmCfgDatafilter.name = name;
		// alarmCfgDatafilter.start_date = start_date;
		// alarmCfgDatafilter.end_date = end_date;
		//
		// JSONObject json = new JSONObject();
		// JSONArray arr = new JSONArray();
		// JSONObject data = null;
		// for (int i = 0; i < alarmCfgList.size(); i++) {
		// AlarmCfg alarmCfg = alarmCfgList.get(i);
		// alarmTriggerFilter.alarmcfg_id = alarmCfg.alarmcfg_id;
		// alarmCfgDatafilter.alarm_cfg_id = alarmCfg.alarmcfg_id;
		// // 通过配置报警id得到对应的报警数据
		// List<AlarmCfgData> alarmCfgDataList =
		// alarmCfgDataApi.getAlarmCfgData(alarmCfgDatafilter);
		// if (alarmCfgDataList == null || alarmCfgDataList.size() < 1) {
		// continue;
		// }
		// // 通过配置报警id得到对应的报警触发条件
		// List<AlarmTrigger> alarmTriggerList =
		// alarmTriggerApi.getAlarmTrigger(alarmTriggerFilter);
		// if (alarmTriggerList == null || alarmTriggerList.size() < 1) {
		// continue;
		// }
		// for (int j = 0; j < alarmTriggerList.size(); j++) {
		// AlarmTrigger alarmTrigger = alarmTriggerList.get(j);
		// for (int k = 0; k < alarmCfgDataList.size(); k++) {
		// AlarmCfgData alarmCfgData = alarmCfgDataList.get(k);
		// if (alarmCfgData.alarm_cfg_id == alarmTrigger.alarmcfg_id) {
		// data = new JSONObject();
		// data.put("name", alarmCfg.name);// 名称
		// data.put("text", alarmCfg.text);// 信息记录
		// data.put("state", alarmCfgData.state);// 报警值状态
		// data.put("value", alarmCfgData.value);// 报警值
		// data.put("alarm_cfg_id", alarmCfgData.alarm_cfg_id);// 报警值
		// data.put("monitor_time", alarmCfgData.monitor_time);// 时间
		// arr.add(data);
		//
		// }
		// }
		// }
		// }
		// json.put("alarmData", arr);
		// return new Output(json);

	}

}
