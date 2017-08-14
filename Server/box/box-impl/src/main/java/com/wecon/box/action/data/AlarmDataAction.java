package com.wecon.box.action.data;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AlarmCfgDataApi;
import com.wecon.box.entity.AlarmCfgDataAlarmCfg;
import com.wecon.box.entity.Page;
import com.wecon.box.filter.AlarmCfgDataFilter;
import com.wecon.common.util.CommonUtils;
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

	// @WebApi(forceAuth = true, master = true)
	@Description("获取当前报警")
	@RequestMapping(value = "/getNowAlarmData")
	public Output getNowAlarmData(@RequestParam("pageIndex") Integer pageIndex,
			@RequestParam("pageSize") Integer pageSize) {
		Client client = AppContext.getSession().client;

		Page<AlarmCfgDataAlarmCfg> alarmCfgDataAlarmCfgList = null;
		AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
		filter.state = -1;
		filter.account_id = 1000021;
		//管理
//		alarmCfgDataAlarmCfgList = alarmCfgDataApi.getRealHisCfgDataList(filter, pageIndex, pageSize);
		//视图
		alarmCfgDataAlarmCfgList = alarmCfgDataApi.getViewRealHisCfgDataList(filter, pageIndex, pageSize);

		/** 超级管理 **/
		// if (client.userInfo.getUserType() == 0) {
		// AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
		// filter.state = -1;
		// alarmCfgDataAlarmCfgList =
		// alarmCfgDataApi.getRealHisCfgDataList(filter,pageIndex,pageSize);
		//
		// } else if (client.userInfo.getUserType() == 1) {
		// /** 管理 **/
		// AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
		// filter.account_id = client.userId;
		// filter.state = -1;
		// alarmCfgDataAlarmCfgList =
		// alarmCfgDataApi.getRealHisCfgDataList(filter,pageIndex,pageSize);
		//
		// } else if (client.userInfo.getUserType() == 2) {
		// /** 视图 **/
		//
		// }
		JSONObject json = new JSONObject();
		json.put("alarmData", alarmCfgDataAlarmCfgList);
		return new Output(json);

	}

	// @WebApi(forceAuth = true, master = true)
	@Description("获取历史报警")
	@RequestMapping(value = "/getHisAlarmData")
	public Output getHisAlarmData(@RequestParam("alarm_cfg_id") String alarm_cfg_id, @RequestParam("name") String name,
			@RequestParam("start_date") String start_date, @RequestParam("end_date") String end_date,
			@RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
		Client client = AppContext.getSession().client;

		Page<AlarmCfgDataAlarmCfg> alarmCfgDataAlarmCfgList = null;
		AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
		if (!CommonUtils.isNullOrEmpty(alarm_cfg_id)) {
			filter.alarm_cfg_id = Long.parseLong(alarm_cfg_id);
		}
		filter.name=name;
		filter.state = -1;
		filter.start_date = start_date;
		filter.end_date = end_date;
		filter.account_id = 1000021;
		//视图
		alarmCfgDataAlarmCfgList=alarmCfgDataApi.getViewRealHisCfgDataList(filter, pageIndex, pageSize);
		
		
		
		
		
		//管理者
//		alarmCfgDataAlarmCfgList = alarmCfgDataApi.getRealHisCfgDataList(filter, pageIndex, pageSize);
		JSONObject json = new JSONObject();
		json.put("alarmHisData", alarmCfgDataAlarmCfgList);
		return new Output(json);

	}

}
