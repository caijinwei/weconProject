package com.wecon.box.action.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AccountDirApi;
import com.wecon.box.api.AlarmCfgDataApi;
import com.wecon.box.entity.AccountDir;
import com.wecon.box.entity.AlarmCfgDataAlarmCfg;
import com.wecon.box.entity.Page;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.AlarmCfgDataFilter;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
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
	@Autowired
	private AccountDirApi accountDirApi;

	@WebApi(forceAuth = true, master = true)
	@Description("获取当前报警")
	@RequestMapping(value = "/getNowAlarmData")
	public Output getNowAlarmData(@RequestParam("device_id") String device_id,
			@RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
		Client client = AppContext.getSession().client;

		Page<AlarmCfgDataAlarmCfg> alarmCfgDataAlarmCfgList = null;
		AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
		filter.account_id = client.userId;
		if (!CommonUtils.isNullOrEmpty(device_id)) {
			filter.device_id = Long.parseLong(device_id);
		}

		filter.state = 1;

		if (client.userInfo.getUserType() == 1) {
			/** 管理 **/

			alarmCfgDataAlarmCfgList = alarmCfgDataApi.getRealHisCfgDataList(filter, pageIndex, pageSize);

		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/
			alarmCfgDataAlarmCfgList = alarmCfgDataApi.getViewRealHisCfgDataList(filter, pageIndex, pageSize);

		}
		JSONObject json = new JSONObject();
		json.put("type", client.userInfo.getUserType());
		json.put("alarmData", alarmCfgDataAlarmCfgList);
		return new Output(json);

	}

	@WebApi(forceAuth = true, master = true)
	@Description("获取历史报警")
	@RequestMapping(value = "/getHisAlarmData")
	public Output getHisAlarmData(@RequestParam("device_id") String device_id,
			@RequestParam("alarm_cfg_id") String alarm_cfg_id, @RequestParam("name") String name,
			@RequestParam("start_date") String start_date, @RequestParam("end_date") String end_date,
			@RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
		Client client = AppContext.getSession().client;

		Page<AlarmCfgDataAlarmCfg> alarmCfgDataAlarmCfgList = null;
		AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
		if (!CommonUtils.isNullOrEmpty(alarm_cfg_id)) {
			filter.alarm_cfg_id = Long.parseLong(alarm_cfg_id);
		}
		filter.name = name;
		filter.state = 1;
		filter.start_date = start_date;
		filter.end_date = end_date;
		filter.account_id = client.userId;
		if (!CommonUtils.isNullOrEmpty(device_id)) {
			filter.device_id = Long.parseLong(device_id);
		}

		if (client.userInfo.getUserType() == 1) {
			/** 管理 **/

			alarmCfgDataAlarmCfgList = alarmCfgDataApi.getRealHisCfgDataList(filter, pageIndex, pageSize);

		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/
			alarmCfgDataAlarmCfgList = alarmCfgDataApi.getViewRealHisCfgDataList(filter, pageIndex, pageSize);

		}

		JSONObject json = new JSONObject();
		json.put("alarmHisData", alarmCfgDataAlarmCfgList);
		return new Output(json);

	}
	/**
	 * 通过机器码获取对应的实时数据组
	 * 
	 * @param device_id
	 * @return
	 */
	@WebApi(forceAuth = true, master = true)
	@Description("通过盒子ID获取报警配置分组")
	@RequestMapping(value = "/getAlarmGroup")
	public Output getAlarmGroup(@RequestParam("device_id") String device_id) {

		Client client = AppContext.getSession().client;
		JSONObject json = new JSONObject();
		long deviceid = 0;
		if (!CommonUtils.isNullOrEmpty(device_id)) {
			deviceid = Long.parseLong(device_id);
		}
		/** 管理 **/
		List<AccountDir> accountDirList = accountDirApi.getAccountDirList(client.userId, 3, deviceid);

		if (accountDirList == null || accountDirList.size() < 1) {
			AccountDir model = new AccountDir();
			model.account_id = client.userId;
			model.name = "默认组";
			model.type = 3;
			model.device_id = deviceid;
			long id = accountDirApi.addAccountDir(model);
			if (id > 0) {
				accountDirList = accountDirApi.getAccountDirList(client.userId, 3, deviceid);

			} else {
				throw new BusinessException(ErrorCodeOption.Get_AlarmList_Error.key,
						ErrorCodeOption.Get_AlarmList_Error.value);
			}

		}

		json.put("alarmGroup", accountDirList);
		json.put("type", client.userInfo.getUserType());
		return new Output(json);

	}

}
