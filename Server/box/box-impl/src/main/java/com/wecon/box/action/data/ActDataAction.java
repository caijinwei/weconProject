package com.wecon.box.action.data;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AccountDirApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.entity.AccountDir;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.PiBoxCom;
import com.wecon.box.entity.PiBoxComAddr;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.entity.RedisPiBoxActData;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
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

	/**
	 * 通过机器码获取对应的实时数据组
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
			long id = accountDirApi.addAccountDir(model);
			if (id > 0) {
				accountDirList = accountDirApi.getAccountDirList(client.userId, 1, deviceid);

			} else {
				throw new BusinessException(ErrorCodeOption.Get_ActList_Error.key,
						ErrorCodeOption.Get_ActList_Error.value);
			}

		}

		json.put("ActGroup", accountDirList);
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
		/**获取实时数据配置信息**/
		RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
		/** 通过视图获取配置信息**/
		ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();

		Page<RealHisCfgDevice> realHisCfgDeviceList = null;
		if (client.userInfo.getUserType() == 1) {
			/** 管理 **/
			realHisCfgFilter.addr_type = -1;
			realHisCfgFilter.data_type = 0;
			realHisCfgFilter.his_cycle = -1;
			realHisCfgFilter.account_id = client.userId;

			if (!CommonUtils.isNullOrEmpty(acc_dir_id)) {
				realHisCfgFilter.dirId = Long.parseLong(acc_dir_id);
			}
			if (!CommonUtils.isNullOrEmpty(device_id)) {
				realHisCfgFilter.device_id = Long.parseLong(device_id);
			}
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfgList(realHisCfgFilter, 1, 5);

		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/

			viewAccountRoleFilter.view_id = client.userId;
			viewAccountRoleFilter.cfg_type = 1;
			viewAccountRoleFilter.data_type = 0;
			viewAccountRoleFilter.role_type = -1;
			if (!CommonUtils.isNullOrEmpty(acc_dir_id)) {
				viewAccountRoleFilter.dirId = Long.parseLong(acc_dir_id);
			}
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfgList(viewAccountRoleFilter, 1, 5);
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

}
