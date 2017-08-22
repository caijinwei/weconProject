package com.wecon.box.action.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AccountDirApi;
import com.wecon.box.api.AccountDirRelApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.entity.AccountDir;
import com.wecon.box.entity.AccountDirRel;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.PiBoxCom;
import com.wecon.box.entity.PiBoxComAddr;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.entity.RedisPiBoxActData;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.box.util.ServerMqtt;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
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
	private AccountDirApi accountDirApi;
	@Autowired
	private AccountDirRelApi accountDirRelApi;

	/**
	 * 通过机器码获取对应的实时数据组
	 * 
	 * @param device_id
	 * @return
	 */
	@WebApi(forceAuth = true, master = true)
	@Description("通过盒子ID获取实时数据组")
	@RequestMapping(value = "/getActGroup")
	public Output getActGroup(@RequestParam("device_id") String device_id) {

		Client client = AppContext.getSession().client;
		JSONObject json = new JSONObject();
		long deviceid = 0;
		if (!CommonUtils.isNullOrEmpty(device_id)) {
			deviceid = Long.parseLong(device_id);
		}
		/** 管理 **/
		List<AccountDir> accountDirList = accountDirApi.getAccountDirList(client.userId, 1, deviceid);

		if (accountDirList == null || accountDirList.size() < 1) {
			AccountDir model = new AccountDir();
			model.account_id = client.userId;
			model.name = "默认组";
			model.type = 1;
			model.device_id = deviceid;
			long id = accountDirApi.addAccountDir(model);
			if (id > 0) {
				accountDirList = accountDirApi.getAccountDirList(client.userId, 1, deviceid);

			} else {
				throw new BusinessException(ErrorCodeOption.Get_ActList_Error.key,
						ErrorCodeOption.Get_ActList_Error.value);
			}

		}

		json.put("ActGroup", accountDirList);
		json.put("type", client.userInfo.getUserType());
		return new Output(json);

	}

	/**
	 * 获取实时数据
	 * 
	 * @return
	 */

	@WebApi(forceAuth = true, master = true)

	@Description("根据盒子id和实时分组id获取实时数据")

	@RequestMapping(value = "/getActData")

	public Output getActData(@RequestParam("device_id") String device_id, @RequestParam("acc_dir_id") String acc_dir_id,
			@RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
		Client client = AppContext.getSession().client;
		JSONObject json = new JSONObject();
		/** 获取实时数据配置信息 **/
		RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
		/** 通过视图获取配置信息 **/
		ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();

		Page<RealHisCfgDevice> realHisCfgDeviceList = null;
		if (client.userInfo.getUserType() == 1) {
			/** 管理 **/
			realHisCfgFilter.addr_type = -1;
			realHisCfgFilter.data_type = 0;
			realHisCfgFilter.his_cycle = -1;
			realHisCfgFilter.state = 1;

			realHisCfgFilter.account_id = client.userId;

			if (!CommonUtils.isNullOrEmpty(acc_dir_id)) {
				realHisCfgFilter.dirId = Long.parseLong(acc_dir_id);
			}
			if (!CommonUtils.isNullOrEmpty(device_id)) {
				realHisCfgFilter.device_id = Long.parseLong(device_id);
			}
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfgList(realHisCfgFilter, pageIndex, pageSize);

		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/

			viewAccountRoleFilter.view_id = client.userId;
			viewAccountRoleFilter.cfg_type = 1;
			viewAccountRoleFilter.data_type = 0;
			viewAccountRoleFilter.role_type = -1;
			viewAccountRoleFilter.state = 1;
			if (!CommonUtils.isNullOrEmpty(acc_dir_id)) {
				viewAccountRoleFilter.dirId = Long.parseLong(acc_dir_id);
			}
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfgList(viewAccountRoleFilter, pageIndex, pageSize);
		}

		// 如果该账号下无实时数据配置文件直接返回空
		if (realHisCfgDeviceList != null && realHisCfgDeviceList.getList().size() > 0) {
			for (int i = 0; i < realHisCfgDeviceList.getList().size(); i++) {
				RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.getList().get(i);

				String device_machine = realHisCfgDevice.machine_code;
				// 通过机器码去redis中获取数据
				RedisPiBoxActData redisPiBoxActData = redisPiBoxApi.getRedisPiBoxActData(device_machine);
				if (redisPiBoxActData != null) {
					List<PiBoxCom> act_time_data_list = redisPiBoxActData.act_time_data_list;
					for (int j = 0; j < act_time_data_list.size(); j++) {
						PiBoxCom piBoxCom = act_time_data_list.get(j);

						if (realHisCfgDevice.plc_id == Long.parseLong(piBoxCom.com)) {

							List<PiBoxComAddr> addr_list = piBoxCom.addr_list;
							for (int x = 0; x < addr_list.size(); x++) {
								PiBoxComAddr piBoxComAddr = addr_list.get(x);

								if (realHisCfgDevice.id == Long.parseLong(piBoxComAddr.addr_id)) {
									realHisCfgDevice.re_state = piBoxComAddr.state;
									realHisCfgDevice.re_value = piBoxComAddr.value;

								}

							}

						}

					}
				}
			}
		}

		json.put("piBoxActDateMode", realHisCfgDeviceList);
		return new Output(json);

	}

	/**
	 * 更新实时监控点名称
	 * 
	 * @return
	 */
	@Description("更新实时监控点名称")
	@RequestMapping(value = "/upActcfgName")
	public Output upActcfgName(@RequestParam("id") String id, @RequestParam("name") String name,
			@RequestParam("actgroupId") String actgroupId) {

		if (!CommonUtils.isNullOrEmpty(id) && !CommonUtils.isNullOrEmpty(actgroupId)) {
			AccountDirRel accountDirRel = accountDirRelApi.getAccountDirRel(Long.parseLong(actgroupId),
					Long.parseLong(id));

			if (accountDirRel != null) {
				accountDirRel.ref_alais = name;

				accountDirRelApi.upAccountDirRel(accountDirRel);
			}

		}

		return new Output();

	}

	@Description("mosquito消息的推送测试")
	@RequestMapping(value = "/putMess")
	public Output putMQTTMess(@RequestParam("machine_code") String machine_code, @RequestParam("com") String com,
			@RequestParam("value") String value, @RequestParam("addr_id") String addr_id) throws MqttException {
		ServerMqtt server = new ServerMqtt();
		server.message = new MqttMessage();
		server.message.setQos(1);
		server.message.setRetained(true);

		PiBoxComAddr addr1 = new PiBoxComAddr();
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

	/**
	 * 复制监控点到其他组
	 * 
	 * @return
	 */
	@Description("复制监控点到其他组")
	@RequestMapping(value = "/copyMonitor")
	public Output copyMonitor(@RequestParam("monitorid") String monitorid,
			@RequestParam("acc_dir_id") String acc_dir_id, @RequestParam("alais") String alais) {

		if (CommonUtils.isNullOrEmpty(monitorid) || CommonUtils.isNullOrEmpty(acc_dir_id)
				|| CommonUtils.isNullOrEmpty(alais)) {
			throw new BusinessException(ErrorCodeOption.Get_Data_Error.key, ErrorCodeOption.Get_Data_Error.value);
		}
		AccountDirRel accountDirRel = accountDirRelApi.getAccountDirRel(Long.parseLong(acc_dir_id),
				Long.parseLong(monitorid));

		if (accountDirRel != null) {
			throw new BusinessException(ErrorCodeOption.Monitor_Existed.key, ErrorCodeOption.Monitor_Existed.value);
		} else {

			accountDirRel = new AccountDirRel();
			accountDirRel.acc_dir_id = Long.parseLong(acc_dir_id);
			accountDirRel.ref_id = Long.parseLong(monitorid);
			accountDirRel.ref_alais = alais;
			accountDirRelApi.saveAccountDirRel(accountDirRel);

		}

		return new Output();

	}

	/**
	 * 移动监控点到其他组
	 * 
	 * @return
	 */
	@Description("复制监控点到其他组")
	@RequestMapping(value = "/moveMonitor")
	public Output moveMonitor(@RequestParam("monitorid") String monitorid,
			@RequestParam("to_acc_dir_id") String to_acc_dir_id,
			@RequestParam("from_acc_dir_id") String from_acc_dir_id, @RequestParam("alais") String alais) {

		if (CommonUtils.isNullOrEmpty(monitorid) || CommonUtils.isNullOrEmpty(to_acc_dir_id)
				|| CommonUtils.isNullOrEmpty(from_acc_dir_id) || CommonUtils.isNullOrEmpty(alais)) {
			throw new BusinessException(ErrorCodeOption.Get_Data_Error.key, ErrorCodeOption.Get_Data_Error.value);
		}
		AccountDirRel accountDirRel = accountDirRelApi.getAccountDirRel(Long.parseLong(to_acc_dir_id),
				Long.parseLong(monitorid));

		if (accountDirRel != null) {
			throw new BusinessException(ErrorCodeOption.Monitor_Existed.key, ErrorCodeOption.Monitor_Existed.value);
		} else {

			accountDirRel = new AccountDirRel();
			accountDirRel.acc_dir_id = Long.parseLong(to_acc_dir_id);
			accountDirRel.ref_id = Long.parseLong(monitorid);
			accountDirRel.ref_alais = alais;
			accountDirRelApi.saveAccountDirRel(accountDirRel);
			// 删除该分组下的监控点
			accountDirRelApi.delAccountDir(Long.parseLong(from_acc_dir_id), Long.parseLong(monitorid));

		}

		return new Output();

	}

	/**
	 * 复制监控点到其他组
	 * 
	 * @return
	 */
	@Description("移除监控点")
	@RequestMapping(value = "/delMonitor")
	public Output delMonitor(@RequestParam("monitorid") String monitorid,
			@RequestParam("acc_dir_id") String acc_dir_id) {

		if (CommonUtils.isNullOrEmpty(monitorid) || CommonUtils.isNullOrEmpty(acc_dir_id)) {
			throw new BusinessException(ErrorCodeOption.Get_Data_Error.key, ErrorCodeOption.Get_Data_Error.value);
		}
		// 删除该分组下的监控点
		accountDirRelApi.delAccountDir(Long.parseLong(acc_dir_id), Long.parseLong(monitorid));

		return new Output();

	}

}
