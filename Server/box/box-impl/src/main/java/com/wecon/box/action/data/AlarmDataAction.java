package com.wecon.box.action.data;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AccountDirApi;
import com.wecon.box.api.AccountDirRelApi;
import com.wecon.box.api.AlarmCfgApi;
import com.wecon.box.api.AlarmCfgDataApi;
import com.wecon.box.api.AlarmTriggerApi;
import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.AccountDir;
import com.wecon.box.entity.AccountDirRel;
import com.wecon.box.entity.AlarmCfg;
import com.wecon.box.entity.AlarmCfgDataAlarmCfg;
import com.wecon.box.entity.AlarmCfgTrigger;
import com.wecon.box.entity.AlarmTrigger;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.AlarmCfgDataFilter;
import com.wecon.box.filter.AlarmTriggerFilter;
import com.wecon.box.param.AlarmCfgParam;
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
	@Autowired
	private AlarmCfgApi alarmCfgApi;
	@Autowired
	private AccountDirRelApi accountDirRelApi;
	@Autowired
	private AlarmTriggerApi alarmTriggerApi;
	@Autowired
	private ViewAccountRoleApi viewAccountRoleApi;

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

	@Description("获取组下的报警配置")
	@WebApi(forceAuth = true, master = true, authority = { "1" })
	@RequestMapping(value = "/getAlarmCfg")
	public Output getAlarmCfg(@RequestParam("group_id") String group_id, @RequestParam("pageIndex") Integer pageIndex,
			@RequestParam("pageSize") Integer pageSize) {
		long account_id = AppContext.getSession().client.userId;
		JSONObject json = new JSONObject();
		Page<AlarmCfgTrigger> listalrmCfgTrigger = new Page<AlarmCfgTrigger>(pageIndex, pageSize, 0);
		if (CommonUtils.isNullOrEmpty(group_id)) {
			json.put("listalrmCfgTrigger", listalrmCfgTrigger);
			return new Output(json);
		}
		listalrmCfgTrigger = alarmCfgApi.getRealHisCfgDataList(account_id, Long.parseLong(group_id), pageIndex,
				pageSize);
		if (listalrmCfgTrigger.getList().size() > 0) {
			for (int i = 0; i < listalrmCfgTrigger.getList().size(); i++) {
				AlarmCfgTrigger alarmCfgTrigger = listalrmCfgTrigger.getList().get(i);
				AlarmTriggerFilter filter = new AlarmTriggerFilter();
				filter.alarmcfg_id = alarmCfgTrigger.alarmcfg_id;
				filter.type = -1;
				List<AlarmTrigger> listAlarmTrigger = alarmTriggerApi.getAlarmTrigger(filter);
				if (alarmCfgTrigger.addr_type == 0) {// 位地址
					alarmCfgTrigger.triggerValue = listAlarmTrigger.get(0).value;

				} else {
					if (alarmCfgTrigger.condition_type == 0) {// 一个条件
						AlarmTrigger alarmTrigger = listAlarmTrigger.get(0);
						String typevalue = "";
						switch (alarmTrigger.type) {
						case 0:
							typevalue = "等于";
							break;
						case 1:
							typevalue = "不等于";
							break;
						case 2:
							typevalue = "大于";
							break;
						case 3:
							typevalue = "大于等于";
							break;
						case 4:
							typevalue = "小于";
							break;
						case 5:
							typevalue = "小于等于";
							break;

						default:
							break;
						}
						alarmCfgTrigger.triggerValue = "值 " + " " + typevalue + listAlarmTrigger.get(0).value;
					} else {// 与 或
						StringBuffer buffer = new StringBuffer();
						buffer.append("值");
						String typevalue = "";
						switch (listAlarmTrigger.get(0).type) {
						case 0:
							typevalue = "等于";
							break;
						case 1:
							typevalue = "不等于";
							break;
						case 2:
							typevalue = "大于";
							break;
						case 3:
							typevalue = "大于等于";
							break;
						case 4:
							typevalue = "小于";
							break;
						case 5:
							typevalue = "小于等于";
							break;

						default:
							break;
						}
						buffer.append(" ");
						buffer.append(typevalue);
						buffer.append(listAlarmTrigger.get(0).value);
						buffer.append(" ");
						if (alarmCfgTrigger.condition_type == 1) {
							buffer.append("与");
						} else {
							buffer.append("或");
						}
						switch (listAlarmTrigger.get(1).type) {
						case 0:
							typevalue = "等于";
							break;
						case 1:
							typevalue = "不等于";
							break;
						case 2:
							typevalue = "大于";
							break;
						case 3:
							typevalue = "大于等于";
							break;
						case 4:
							typevalue = "小于";
							break;
						case 5:
							typevalue = "小于等于";
							break;

						default:
							break;
						}
						buffer.append(" ");
						buffer.append("值");
						buffer.append(" ");
						buffer.append(typevalue);
						buffer.append(listAlarmTrigger.get(1).value);
						alarmCfgTrigger.triggerValue = buffer.toString();
					}

				}

				alarmCfgTrigger.listAlarmTrigger = alarmTriggerApi.getAlarmTrigger(filter);
			}

		}
		json.put("listalrmCfgTrigger", listalrmCfgTrigger);
		return new Output(json);

	}

	@Description("添加或者修改报警配置")
	@WebApi(forceAuth = true, master = true, authority = { "1" })
	@RequestMapping(value = "/addUpdataAlarmMonitor")
	public Output addUpdataAlarmMonitor(@Valid AlarmCfgParam alarmCfgParam) {
		long account_id = AppContext.getSession().client.userId;
		if (alarmCfgParam != null) {
			AlarmCfg alarmCfg = null;
			if (alarmCfgParam.alarmcfg_id > 0) {
				alarmCfg = alarmCfgApi.getAlarmcfg(alarmCfgParam.alarmcfg_id);
				if (alarmCfg != null) {
					alarmCfg.account_id = account_id;
					alarmCfg.addr = alarmCfgParam.addr;
					alarmCfg.addr_type = alarmCfgParam.addr_type;
					alarmCfg.bind_state = 1;
					alarmCfg.data_id = alarmCfgParam.data_id;
					alarmCfg.plc_id = alarmCfgParam.plc_id;
					alarmCfg.rid = alarmCfgParam.rid;
					alarmCfg.name = alarmCfgParam.name;
					alarmCfg.text = alarmCfgParam.text;
					alarmCfg.condition_type = alarmCfgParam.condition_type;
					alarmCfg.state = 2;
					alarmCfg.device_id = alarmCfgParam.device_id;
					alarmCfg.data_limit = alarmCfgParam.rang;
					boolean issuccess = alarmCfgApi.upAlarmCfg(alarmCfg);
					if (issuccess) {
						// 获取分组信息
						if (alarmCfgParam.group_id > 0) {
							AccountDirRel accountDirRel = accountDirRelApi.getAccountDirRel(-1,
									alarmCfgParam.alarmcfg_id);

							if (accountDirRel != null) {

								accountDirRelApi.delAccountDir(accountDirRel.acc_dir_id, accountDirRel.ref_id);
							}
							AccountDirRel dirRel = new AccountDirRel();
							dirRel.acc_dir_id = alarmCfgParam.group_id;
							dirRel.ref_id = alarmCfgParam.alarmcfg_id;
							dirRel.ref_alais = alarmCfgParam.name;
							accountDirRelApi.saveAccountDirRel(dirRel);

						}
						if (!CommonUtils.isNullOrEmpty(alarmCfgParam.value)
								&& !CommonUtils.isNullOrEmpty(alarmCfgParam.type)) {
							// 先删除该配置下的所有触发配置
							alarmTriggerApi.delAlarmTrigger(alarmCfgParam.alarmcfg_id);
							String[] types = alarmCfgParam.type.split(",");
							String[] values = alarmCfgParam.value.split(",");
							List<AlarmTrigger> alarmTriggerList = new ArrayList<AlarmTrigger>();
							for (int i = 0; i < types.length; i++) {
								AlarmTrigger alarmTrigger = new AlarmTrigger();
								alarmTrigger.alarmcfg_id = alarmCfgParam.alarmcfg_id;
								alarmTrigger.type = Integer.parseInt(types[i]);
								alarmTrigger.value = values[i];
								alarmTriggerList.add(alarmTrigger);

							}
							// 批量添加报警触发配置
							alarmTriggerApi.saveAlarmTrigger(alarmTriggerList);

						}
					}

				}

			} else {
				alarmCfg = new AlarmCfg();
				alarmCfg.account_id = account_id;
				alarmCfg.addr = alarmCfgParam.addr;
				alarmCfg.addr_type = alarmCfgParam.addr_type;
				alarmCfg.bind_state = 1;
				alarmCfg.data_id = alarmCfgParam.data_id;
				alarmCfg.plc_id = alarmCfgParam.plc_id;
				alarmCfg.rid = alarmCfgParam.rid;
				alarmCfg.name = alarmCfgParam.name;
				alarmCfg.text = alarmCfgParam.text;
				alarmCfg.condition_type = alarmCfgParam.condition_type;
				alarmCfg.state = 1;
				alarmCfg.device_id = alarmCfgParam.device_id;
				alarmCfg.data_limit = alarmCfgParam.rang;
				long id = alarmCfgApi.saveAlarmCfg(alarmCfg);
				if (id > 0) {
					// 添加到分组下
					AlarmCfg alarm = alarmCfgApi.getAlarmcfg(id);
					AccountDirRel accountDirRel = new AccountDirRel();
					accountDirRel.acc_dir_id = alarmCfgParam.group_id;
					accountDirRel.ref_id = alarm.alarmcfg_id;
					accountDirRel.ref_alais = alarmCfgParam.name;
					accountDirRelApi.saveAccountDirRel(accountDirRel);

					if (!CommonUtils.isNullOrEmpty(alarmCfgParam.value)
							&& !CommonUtils.isNullOrEmpty(alarmCfgParam.type)) {
						String[] types = alarmCfgParam.type.split(",");
						String[] values = alarmCfgParam.value.split(",");
						List<AlarmTrigger> alarmTriggerList = new ArrayList<AlarmTrigger>();
						for (int i = 0; i < types.length; i++) {
							AlarmTrigger alarmTrigger = new AlarmTrigger();
							alarmTrigger.alarmcfg_id = alarm.alarmcfg_id;
							alarmTrigger.type = Integer.parseInt(types[i]);
							alarmTrigger.value = values[i];
							alarmTriggerList.add(alarmTrigger);

						}
						// 批量添加报警触发配置
						alarmTriggerApi.saveAlarmTrigger(alarmTriggerList);

					}

				}

			}

		}

		return new Output();
	}

	@Description("删除报警配置")
	@WebApi(forceAuth = true, master = true, authority = { "1" })
	@RequestMapping(value = "/delAlrmCfg")
	public Output delAlrmCfg(@RequestParam("alarmcfg_id") String alarmcfg_id) {

		// 1.删除分配给视图账号的配置
		viewAccountRoleApi.deletePoint(2, Long.parseLong(alarmcfg_id));
		// 2.更改配置状态，等待盒子发送数据把配置物理删除
		AlarmCfg alarmCfg = alarmCfgApi.getAlarmcfg(Long.parseLong(alarmcfg_id));
		alarmCfg.state = 3;// 删除配置状态
		alarmCfgApi.upAlarmCfg(alarmCfg);

		return new Output();

	}

}
