package com.wecon.box.action.data;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AccountApi;
import com.wecon.box.api.AccountRelationApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.entity.PiBoxCom;
import com.wecon.box.entity.PiBoxComAddr;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.entity.RedisPiBoxActData;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Client;
import com.wecon.restful.core.Output;

/**
 * @author lanpenghui 2017年8月9日下午1:43:32
 */
@RestController
@RequestMapping("actDataAction")
public class ActDataAction {

	@Autowired
	private RedisPiBoxApi redisPiBoxApi;
	@Autowired
	private RealHisCfgApi realHisCfgApi;
	@Autowired
	AccountApi accountApi;
	@Autowired
	AccountRelationApi accountRelationApi;

	/**
	 * 获取实时数据
	 * 
	 * @return
	 */
	@WebApi(forceAuth = true, master = true)
	@Description("根据获取的机器码向redis服务器请求实时数据")
	@RequestMapping(value = "/getActData")
	public Output getActData() {
		Client client = AppContext.getSession().client;
		JSONObject json = new JSONObject();
		JSONArray arr = new JSONArray();
		JSONObject data = null;
		// 获取实时数据配置信息
		RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();

		List<RealHisCfgDevice> realHisCfgDeviceList = null;
		/** 超级管理 **/
		if (client.userInfo.getUserType() == 0) {

			realHisCfgFilter.addr_type = -1;
			realHisCfgFilter.data_type = 0;
			realHisCfgFilter.his_cycle = -1;
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter);
		} else if (client.userInfo.getUserType() == 1) {
			/** 管理 **/
			realHisCfgFilter.addr_type = -1;
			realHisCfgFilter.data_type = 0;
			realHisCfgFilter.his_cycle = -1;
			realHisCfgFilter.account_id = client.userId;
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter);
		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/
			// 通过视图获取配置信息
			ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();
			viewAccountRoleFilter.view_id = client.userId;
			viewAccountRoleFilter.cfg_type = 1;
			viewAccountRoleFilter.data_type = 0;
			viewAccountRoleFilter.role_type = -1;
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(viewAccountRoleFilter);

		}

		// 如果该账号下无实时数据配置文件直接返回空
		if (realHisCfgDeviceList == null || realHisCfgDeviceList.size() < 1) {
			return new Output(json);
		}
		for (int i = 0; i < realHisCfgDeviceList.size(); i++) {
			RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.get(i);
			String device_machine = realHisCfgDevice.machine_code;
			// 通过机器码去redis中获取数据
			RedisPiBoxActData redisPiBoxActData = redisPiBoxApi.getRedisPiBoxActData(device_machine);
			List<PiBoxCom> act_time_data_list = redisPiBoxActData.act_time_data_list;

			data = new JSONObject();

			data.put("id", realHisCfgDevice.id);
			data.put("state", realHisCfgDevice.state);
			data.put("addr_type", realHisCfgDevice.addr_type);
			data.put("addr", realHisCfgDevice.addr);
			data.put("name", realHisCfgDevice.name);
			data.put("describe", realHisCfgDevice.describe);
			for (int j = 0; j < act_time_data_list.size(); j++) {
				PiBoxCom piBoxCom = act_time_data_list.get(j);

				if (realHisCfgDevice.plc_id == Long.parseLong(piBoxCom.com)) {

					List<PiBoxComAddr> addr_list = piBoxCom.addr_list;
					for (int x = 0; x < addr_list.size(); x++) {
						PiBoxComAddr piBoxComAddr = addr_list.get(x);

						if (realHisCfgDevice.id == Long.parseLong(piBoxComAddr.addr_id)) {

							data.put("state", piBoxComAddr.state);
							data.put("value", piBoxComAddr.value);
						}

					}

				}

			}

			arr.add(data);

		}

		json.put("piBoxActDateMode", arr);
		return new Output(json);

	}

}
