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
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.RealHisCfgData;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.filter.RealHisCfgDataFilter;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Client;
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

	@WebApi(forceAuth = true, master = true)
	@Description("获取改账户下的plc和对应的监控点")
	@RequestMapping(value = "/getComMonitor")
	public Output getComMonitor() {

		Client client = AppContext.getSession().client;
		JSONObject json = new JSONObject();
		JSONArray arr = new JSONArray();
		JSONObject data = null;
		List<RealHisCfgDevice> realHisCfgDeviceList = null;
		/** 超级管理 **/
		if (client.userInfo.getUserType() == 0) {
			// 获取实时数据配置信息
			RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
			realHisCfgFilter.addr_type = -1;
			realHisCfgFilter.data_type = 1;
			realHisCfgFilter.his_cycle = -1;
			realHisCfgFilter.state = 1;
		} else if (client.userInfo.getUserType() == 1) {
			/** 管理 **/
			// 获取实时数据配置信息
			RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
			realHisCfgFilter.addr_type = -1;
			realHisCfgFilter.data_type = 1;
			realHisCfgFilter.his_cycle = -1;
			realHisCfgFilter.state = 1;
			realHisCfgFilter.account_id = client.userId;
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter);
		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/
			// 通过视图获取配置信息
			ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();
			viewAccountRoleFilter.view_id = client.userId;
			viewAccountRoleFilter.cfg_type = 1;
			viewAccountRoleFilter.data_type = 1;
			viewAccountRoleFilter.role_type = -1;
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(viewAccountRoleFilter);

		}
		// 如果该账号下无历史数据配置文件直接返回空
		if (realHisCfgDeviceList == null || realHisCfgDeviceList.size() < 1) {
			return new Output(json);
		}

		for (int a = 0; a < realHisCfgDeviceList.size(); a++) {
			RealHisCfg realHisCfg = realHisCfgDeviceList.get(a);
			data = new JSONObject();
			data.put("monitorid", realHisCfg.id);
			data.put("monitordata", realHisCfg.name);
			arr.add(data);
		}

		json.put("monitors", arr);
		return new Output(json);

	}

	/**
	 * 获取历史数据
	 * 
	 * @return
	 */
	@WebApi(forceAuth = true, master = true)
	@Description("获取历史数据")
	@RequestMapping(value = "/getHisData")
	public Output getHisData(@RequestParam("real_his_cfg_id") String real_his_cfg_id,
			@RequestParam("start_date") String start_date, @RequestParam("end_date") String end_date,
			@RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {

		RealHisCfgDataFilter realHisCfgDataFilter = new RealHisCfgDataFilter();
		if (!CommonUtils.isNullOrEmpty(real_his_cfg_id)) {
			realHisCfgDataFilter.real_his_cfg_id = Long.parseLong(real_his_cfg_id);
		}
		realHisCfgDataFilter.start_date = start_date;
		realHisCfgDataFilter.end_date = end_date;
		realHisCfgDataFilter.state = 1;
		

		Page<RealHisCfgData> realHisCfgDataList = realHisCfgDataApi.getRealHisCfgDataList(realHisCfgDataFilter,pageIndex,pageSize);
		JSONObject data = new JSONObject();
		data.put("realHisCfgDataList", realHisCfgDataList);
		return new Output(data);

	}

}
