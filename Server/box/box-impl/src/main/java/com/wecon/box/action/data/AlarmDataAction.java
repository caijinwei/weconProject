package com.wecon.box.action.data;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.*;
import com.wecon.box.constant.Constant;
import com.wecon.box.entity.*;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.box.filter.AlarmCfgDataFilter;
import com.wecon.box.filter.AlarmTriggerFilter;
import com.wecon.box.filter.DevBindUserFilter;
import com.wecon.box.param.AlarmCfgParam;
import com.wecon.box.param.AlarmDataParam;
import com.wecon.box.redis.RedisUpdDeviceCfg;
import com.wecon.box.util.DbLogUtil;
import com.wecon.box.util.OptionUtil;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Client;
import com.wecon.restful.core.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	@Autowired
	private DeviceApi deviceApi;
	@Autowired
	private DevBindUserApi devBindUserApi;
	@Autowired
	protected DbLogUtil dbLogUtil;
	@Autowired
	private OptionUtil optionService;
	@Autowired
	private RedisUpdDeviceCfg redisUpdDeviceCfg;

	@WebApi(forceAuth = true, master = true)
	@Description("获取当前/历史报警")
	@RequestMapping(value = "/getNowHisAlarmData")
	public Output getNowHisAlarmData(@Valid AlarmDataParam alarmDataParam) {
		Client client = AppContext.getSession().client;

		Page<AlarmCfgDataAlarmCfg> alarmCfgDataAlarmCfgList = null;
		AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.alarm_cfg_id)) {
			filter.alarm_cfg_id = Long.parseLong(alarmDataParam.alarm_cfg_id);
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.name)) {
			filter.name = alarmDataParam.name;
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.device_id)) {
			filter.device_id = Long.parseLong(alarmDataParam.device_id);
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.start_date)) {
			filter.start_date = alarmDataParam.start_date;
		}

		if (!CommonUtils.isNullOrEmpty(alarmDataParam.end_date)) {
			filter.end_date = alarmDataParam.end_date;
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.state)) {
			filter.state = Integer.parseInt(alarmDataParam.state);
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.grade_id)) {
			filter.grade_id = Integer.parseInt(alarmDataParam.grade_id);
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.event_id)) {
			filter.event_id = Integer.parseInt(alarmDataParam.event_id);
		}

		filter.account_id = client.userId;

		if (client.userInfo.getUserType() == 1) {
			/** 管理 **/

			alarmCfgDataAlarmCfgList = alarmCfgDataApi.getRealHisCfgDataList(filter, alarmDataParam.pageIndex,
					alarmDataParam.pageSize);

		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/
			alarmCfgDataAlarmCfgList = alarmCfgDataApi.getViewRealHisCfgDataList(filter, alarmDataParam.pageIndex,
					alarmDataParam.pageSize);

		}
		JSONObject json = new JSONObject();
		json.put("type", client.userInfo.getUserType());
		json.put("alarmHisData", alarmCfgDataAlarmCfgList);
		return new Output(json);

	}

	/**
	 * 通过机器码获取对应的报警数据组
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

	@WebApi(forceAuth = true, master = true)
	@Description("删除报警分组")
	@RequestMapping(value = "/delAlarmGroup")
	public Output delAlarmGroup(@RequestParam("id") Integer id) {
		AccountDir dir = accountDirApi.getAccountDir(id);

		List<AccountDirRel> dirrel = accountDirRelApi.getAccountDirRel(dir.id);
		if (dirrel != null) {
			for (AccountDirRel acc : dirrel) {
				AlarmCfg alarmCfg = alarmCfgApi.getAlarmcfg(acc.ref_id);
				if (null != alarmCfg) {
					alarmCfg.state = 3;
					alarmCfgApi.upAlarmCfg(alarmCfg);// 删除分组下的报警配置
					// 1.删除分配给视图账号的配置
					viewAccountRoleApi.deletePoint(2, alarmCfg.alarmcfg_id);
					accountDirRelApi.delAccountDir(id, alarmCfg.alarmcfg_id);
					dbLogUtil.addOperateLog(OpTypeOption.DelAlarm, ResTypeOption.Alarm, alarmCfg.alarmcfg_id, alarmCfg);
					// 消息推送到redis
					redisUpdDeviceCfg.pubDelAlarmCfg(alarmCfg.alarmcfg_id, 0);
				}

			}
		}
		if (dir != null && dir.account_id == AppContext.getSession().client.userId) {
			accountDirApi.delAccountDir(id);
			dbLogUtil.addOperateLog(OpTypeOption.DelDir, ResTypeOption.Dir, id, dir);
		} else {
			throw new BusinessException(ErrorCodeOption.OnlyOperateOneselfGroup.key,
					ErrorCodeOption.OnlyOperateOneselfGroup.value);
		}
		return new Output();

	}

	/**
	 * 获取报警级别
	 * 
	 * @return
	 */
	@Description("获取报警级别")
	@WebApi(forceAuth = true, master = true)
	@RequestMapping(value = "/getAlarmGrade")
	public Output getAlarmGrade() {
		JSONObject json = new JSONObject();
		json.put("alarmGradeOption", optionService.getAlarmGradeOptionOptions());
		System.out.println("显示报警级别==" + json.toString());

		return new Output(json);

	}

	@Description("获取组下的报警配置")
	@WebApi(forceAuth = true, master = true, authority = { "1" })
	@RequestMapping(value = "/getAlarmCfg")
	public Output getAlarmCfg(@RequestParam("group_id") String group_id, @RequestParam("device_id") String device_id,
			@RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
		long account_id = AppContext.getSession().client.userId;
		JSONObject json = new JSONObject();
		Page<AlarmCfgTrigger> listalrmCfgTrigger = new Page<AlarmCfgTrigger>(pageIndex, pageSize, 0);
		if (CommonUtils.isNullOrEmpty(group_id) || CommonUtils.isNullOrEmpty(device_id)) {
			json.put("listalrmCfgTrigger", listalrmCfgTrigger);
			return new Output(json);
		}
		listalrmCfgTrigger = alarmCfgApi.getAlarmCfgDataList(account_id, Long.parseLong(group_id),
				Long.parseLong(device_id), 1, pageIndex, pageSize);
		if (listalrmCfgTrigger.getList().size() > 0) {
			for (int i = 0; i < listalrmCfgTrigger.getList().size(); i++) {
				AlarmCfgTrigger alarmCfgTrigger = listalrmCfgTrigger.getList().get(i);
				// 整数位 小数位分割
				if (alarmCfgTrigger.digit_count != null) {
					String[] numdecs = alarmCfgTrigger.digit_count.split(",");
					if (numdecs != null) {
						if (numdecs.length == 1) {
							alarmCfgTrigger.num = numdecs[0];
						} else if (numdecs.length == 2) {
							alarmCfgTrigger.num = numdecs[0];
							alarmCfgTrigger.dec = numdecs[1];
						}
					}
				}
				// 主子编号范围分割
				if (alarmCfgTrigger.data_limit != null) {
					String[] numdecs = alarmCfgTrigger.data_limit.split(",");
					if (numdecs != null) {
						if (numdecs.length == 1) {
							alarmCfgTrigger.main_limit = numdecs[0];
						} else if (numdecs.length == 2) {
							alarmCfgTrigger.main_limit = numdecs[0];
							alarmCfgTrigger.child_limit = numdecs[1];
						}
					}
				}
				// 主子编号进制分割
				if (alarmCfgTrigger.digit_binary != null) {
					String[] numdecs = alarmCfgTrigger.digit_binary.split(",");
					if (numdecs != null) {
						if (numdecs.length == 1) {
							alarmCfgTrigger.main_binary = numdecs[0];
						} else if (numdecs.length == 2) {
							alarmCfgTrigger.main_binary = numdecs[0];
							alarmCfgTrigger.child_binary = numdecs[1];
						}
					}
				}
				// 主子地址分割
				String[] addrs = alarmCfgTrigger.addr.split(",");
				if (addrs != null) {
					if (addrs.length == 1) {
						alarmCfgTrigger.main_addr = addrs[0];
					} else if (addrs.length == 2) {
						alarmCfgTrigger.main_addr = addrs[0];
						alarmCfgTrigger.child_addr = addrs[1];
					}
				}
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
		DevBindUserFilter devBindUser = new DevBindUserFilter();
		devBindUser.account_id = account_id;
		devBindUser.device_id = alarmCfgParam.device_id;
		List<DevBindUser> listDeviceBindUser = devBindUserApi.getDevBindUser(devBindUser);
		if (listDeviceBindUser.size() != 1) {
			throw new BusinessException(ErrorCodeOption.Device_AlreadyBind.key,
					ErrorCodeOption.Device_AlreadyBind.value);
		}
		if (alarmCfgParam != null) {
			AlarmCfg oldalarmCfg = null;
			AlarmCfg alarmCfg = null;
			if (alarmCfgParam.alarmcfg_id > 0) {
				oldalarmCfg = alarmCfgApi.getAlarmcfg(alarmCfgParam.alarmcfg_id);
				alarmCfg = alarmCfgApi.getAlarmcfg(alarmCfgParam.alarmcfg_id);
				if (alarmCfg != null) {
					AlarmCfg oldarm = alarmCfgApi.getAlarmcfg(alarmCfgParam.device_id, alarmCfgParam.name);
					if (oldarm != null && oldarm.alarmcfg_id != alarmCfgParam.alarmcfg_id) {
						throw new BusinessException(ErrorCodeOption.Name_Repetition.key,
								ErrorCodeOption.Name_Repetition.value);
					}
					alarmCfg.account_id = account_id;
					alarmCfg.addr = alarmCfgParam.addr;
					alarmCfg.addr_type = alarmCfgParam.addr_type;
					alarmCfg.bind_state = 1;
					alarmCfg.data_id = alarmCfgParam.data_id;
					alarmCfg.plc_id = alarmCfgParam.plc_id;
					alarmCfg.alarm_level = alarmCfgParam.alarm_level;
					alarmCfg.rid = alarmCfgParam.rid;
					if (alarmCfgParam.name.length() > 50) {
						alarmCfg.name = alarmCfgParam.name.substring(0, 50);
					} else {
						alarmCfg.name = alarmCfgParam.name;
					}
					if (alarmCfgParam.text.length() > 249) {
						alarmCfg.text = alarmCfgParam.text.substring(0, 249);
					} else {
						alarmCfg.text = alarmCfgParam.text;
					}
					alarmCfg.condition_type = alarmCfgParam.condition_type;
					alarmCfg.state = 2;
					alarmCfg.device_id = alarmCfgParam.device_id;
					alarmCfg.data_limit = alarmCfgParam.rang;
					alarmCfg.digit_count = alarmCfgParam.digit_count;
					alarmCfg.digit_binary = alarmCfgParam.digit_binary;

					boolean issuccess = alarmCfgApi.upAlarmCfg(alarmCfg);
					if (issuccess) {
						dbLogUtil.updOperateLog(OpTypeOption.UpdAlarm, ResTypeOption.Alarm, alarmCfg.alarmcfg_id,
								oldalarmCfg, alarmCfg);
						// 获取分组信息
						if (alarmCfgParam.group_id > 0) {
							AccountDirRel accountDirRel = accountDirRelApi.getAccountDirRel(-1,
									alarmCfgParam.alarmcfg_id, 3);

							if (accountDirRel != null) {

								accountDirRelApi.delAccountDir(accountDirRel.acc_dir_id, accountDirRel.ref_id);
							}
							AccountDirRel dirRel = new AccountDirRel();
							dirRel.acc_dir_id = alarmCfgParam.group_id;
							dirRel.ref_id = alarmCfgParam.alarmcfg_id;
							if (alarmCfgParam.name.length() > 64) {
								accountDirRel.ref_alais = alarmCfgParam.name.substring(0, 64);
							} else {
								accountDirRel.ref_alais = alarmCfgParam.name;
							}
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
				//消息推送到redis
				redisUpdDeviceCfg.pubUpdAlarmCfg(alarmCfg.alarmcfg_id, 0);

			} else {
				AlarmCfg newarm = alarmCfgApi.getAlarmcfg(alarmCfgParam.device_id, alarmCfgParam.name);
				if (newarm != null) {
					throw new BusinessException(ErrorCodeOption.Name_Repetition.key,
							ErrorCodeOption.Name_Repetition.value);
				}
				// 判断报警配置是否达到上限
				List<AlarmCfg> listAlarmCfg = alarmCfgApi.getAlarmCfg(account_id, alarmCfgParam.device_id);
				if (listAlarmCfg != null && listAlarmCfg.size() >= Constant.AddNum.ALARM_SET_NUM) {
					throw new BusinessException(ErrorCodeOption.Alarm_add_Beyond.key,
							ErrorCodeOption.Alarm_add_Beyond.value);
				}
				alarmCfg = new AlarmCfg();
				alarmCfg.account_id = account_id;
				alarmCfg.addr = alarmCfgParam.addr;
				alarmCfg.addr_type = alarmCfgParam.addr_type;
				alarmCfg.bind_state = 1;
				alarmCfg.data_id = alarmCfgParam.data_id;
				alarmCfg.plc_id = alarmCfgParam.plc_id;
				alarmCfg.alarm_level = alarmCfgParam.alarm_level;
				alarmCfg.rid = alarmCfgParam.rid;
				if (alarmCfgParam.name.length() > 50) {
					alarmCfg.name = alarmCfgParam.name.substring(0, 50);
				} else {
					alarmCfg.name = alarmCfgParam.name;
				}
				if (alarmCfgParam.text.length() > 249) {
					alarmCfg.text = alarmCfgParam.text.substring(0, 249);
				} else {
					alarmCfg.text = alarmCfgParam.text;
				}
				alarmCfg.condition_type = alarmCfgParam.condition_type;
				alarmCfg.state = 1;
				alarmCfg.device_id = alarmCfgParam.device_id;
				alarmCfg.data_limit = alarmCfgParam.rang;
				alarmCfg.digit_count = alarmCfgParam.digit_count;
				alarmCfg.digit_binary = alarmCfgParam.digit_binary;
				long id = alarmCfgApi.saveAlarmCfg(alarmCfg);
				if (id > 0) {
					dbLogUtil.addOperateLog(OpTypeOption.AddAlarm, ResTypeOption.Alarm, alarmCfg.alarmcfg_id, alarmCfg);
					// 添加到分组下
					AlarmCfg alarm = alarmCfgApi.getAlarmcfg(id);
					AccountDirRel accountDirRel = new AccountDirRel();
					accountDirRel.acc_dir_id = alarmCfgParam.group_id;
					accountDirRel.ref_id = alarm.alarmcfg_id;
					if (alarmCfgParam.name.length() > 50) {
						accountDirRel.ref_alais = alarmCfgParam.name.substring(0, 50);
					} else {
						accountDirRel.ref_alais = alarmCfgParam.name;
					}
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
					//消息推送到redis
					redisUpdDeviceCfg.pubUpdAlarmCfg(id, 0);
				}

			}
		

		}

		return new Output();
	}

	@Description("删除报警配置")
	@WebApi(forceAuth = true, master = true, authority = { "1" })
	@RequestMapping(value = "/delAlrmCfg")
	public Output delAlrmCfg(@RequestParam("alarmcfg_id") String alarmcfg_id,
			@RequestParam("alarm_dir_id") String alarm_dir_id) {
		long account_id = AppContext.getSession().client.userId;
		if (!CommonUtils.isNullOrEmpty(alarmcfg_id)) {
			AlarmCfg alarmCfg = alarmCfgApi.getAlarmcfg(Long.parseLong(alarmcfg_id));
			DevBindUserFilter devBindUser = new DevBindUserFilter();
			devBindUser.account_id = account_id;
			devBindUser.device_id = alarmCfg.device_id;
			List<DevBindUser> listDeviceBindUser = devBindUserApi.getDevBindUser(devBindUser);
			if (listDeviceBindUser.size() != 1) {
				throw new BusinessException(ErrorCodeOption.Device_AlreadyBind.key,
						ErrorCodeOption.Device_AlreadyBind.value);
			}
			// 1.删除分配给视图账号的配置
			viewAccountRoleApi.deletePoint(2, Long.parseLong(alarmcfg_id));
			// 2.更改配置状态，等待盒子发送数据把配置物理删除
			alarmCfg.state = 3;// 删除配置状态
			alarmCfgApi.upAlarmCfg(alarmCfg);
			accountDirRelApi.delAccountDir(Long.parseLong(alarm_dir_id), Long.parseLong(alarmcfg_id));
			dbLogUtil.addOperateLog(OpTypeOption.DelAlarm, ResTypeOption.Alarm, alarmCfg.alarmcfg_id, alarmCfg);
			// 消息推送到redis
			redisUpdDeviceCfg.pubDelAlarmCfg(alarmCfg.alarmcfg_id, 0);
		}

		return new Output();

	}

	@Description("确认报警数据")
	@RequestMapping(value = "/confirmData")
	public Output confirmData(@RequestParam("alarm_cfg_id") String alarm_cfg_id,
			@RequestParam("monitor_time") String monitor_time) {
		if (!CommonUtils.isNullOrEmpty(alarm_cfg_id) && !CommonUtils.isNullOrEmpty(monitor_time)) {
			Date dt = new Date(Long.parseLong(monitor_time));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String monitortime = sdf.format(dt);
			AlarmCfgData alarmCfgData = alarmCfgDataApi.getAlarmCfgData(Long.parseLong(alarm_cfg_id),
					Timestamp.valueOf(monitortime));
			if (alarmCfgData != null) {
				alarmCfgData.state = 2;// 已确认状态
				alarmCfgDataApi.updateAlarmCfgData(alarmCfgData);
				dbLogUtil.addOperateLog(OpTypeOption.ConFirmAlarmData, ResTypeOption.Alarm, alarmCfgData.alarm_cfg_id,
						alarmCfgData);

			}

		}

		return new Output();

	}

	@Description("当前报警条目、当前报警盒子数、在线盒子数、总盒子数")
	@RequestMapping(value = "/boxInfo")
	public Output boxInfo() {
		Client client = AppContext.getSession().client;
		AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
		filter.account_id = client.userId;
		filter.state = 1;
		filter.grade_id = -1;
		filter.event_id = -1;
		Page<AlarmCfgDataAlarmCfg> alarmCfgDataAlarmCfgList = alarmCfgDataApi.getRealHisCfgDataList(filter, -1, -1);
		JSONObject json = new JSONObject();
		// 当前报警条目
		json.put("nowalarm", alarmCfgDataAlarmCfgList.getList().size());
		// 当前报警盒子数
		int alarmbox = alarmCfgApi.getAlamBxo(client.userId);
		json.put("alarmbox", alarmbox);
		// 盒子总数
		int allbox = devBindUserApi.getDevBindUserCount(client.userId);
		json.put("allbox", allbox);
		// 在线盒子数
		int onlinebox = deviceApi.getDeviceList(client.userId, 1);
		json.put("onlinebox", onlinebox);
		JSONObject alljson = new JSONObject();
		alljson.put("allBoxInfo", json);
		return new Output(alljson);

	}
}
