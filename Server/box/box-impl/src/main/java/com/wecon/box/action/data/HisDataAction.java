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
import com.wecon.box.api.RealHisCfgDataApi;
import com.wecon.box.entity.AccountDir;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfgData;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.RealHisCfgDataFilter;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
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
	@Autowired
	private AccountDirApi accountDirApi;

	@WebApi(forceAuth = true, master = true)
	@Description("通过盒子ID获取历史数据组")
	@RequestMapping(value = "/getHisGroup")
	public Output getHisGroup(@RequestParam("device_id") String device_id) {

		Client client = AppContext.getSession().client;
		JSONObject json = new JSONObject();
		long deviceid = 0;
		if (!CommonUtils.isNullOrEmpty(device_id)) {
			deviceid = Long.parseLong(device_id);
		}
		/** 管理 **/
		List<AccountDir> accountDirList = accountDirApi.getAccountDirList(client.userId, 2, deviceid);

		if (accountDirList == null || accountDirList.size() < 1) {
			AccountDir model = new AccountDir();
			model.account_id = client.userId;
			model.name = "默认组";
			model.type = 2;
			model.device_id=deviceid;
			long id = accountDirApi.addAccountDir(model);
			if (id > 0) {
				accountDirList = accountDirApi.getAccountDirList(client.userId, 2, deviceid);

			} else {
				throw new BusinessException(ErrorCodeOption.Get_HisList_Error.key,
						ErrorCodeOption.Get_HisList_Error.value);
			}

		}

		json.put("HisGroup", accountDirList);
		return new Output(json);

	}

	@WebApi(forceAuth = true, master = true)
	@Description("获取改账户下的plc和对应的监控点")
	@RequestMapping(value = "/getComMonitor")
	public Output getComMonitor(@RequestParam("device_id") String device_id,
			@RequestParam("acc_dir_id") String acc_dir_id) {

		Client client = AppContext.getSession().client;
		JSONObject json = new JSONObject();
		// 获取实时数据配置信息
		RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
		List<RealHisCfgDevice> realHisCfgDeviceList = null;
	  if (client.userInfo.getUserType() == 1) {
			/** 管理 **/
			realHisCfgFilter.addr_type = -1;
			realHisCfgFilter.data_type = 1;
			realHisCfgFilter.his_cycle = -1;
			realHisCfgFilter.state = 1;
			realHisCfgFilter.account_id = client.userId;
			if(!CommonUtils.isNullOrEmpty(device_id)){
				realHisCfgFilter.device_id=Long.parseLong(device_id);
			}
			if(!CommonUtils.isNullOrEmpty(acc_dir_id)){
				realHisCfgFilter.dirId=Long.parseLong(acc_dir_id);
			}
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter);
		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/
			// 通过视图获取配置信息
			ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();
			viewAccountRoleFilter.view_id = client.userId;
			viewAccountRoleFilter.cfg_type = 1;
			viewAccountRoleFilter.data_type = 1;
			viewAccountRoleFilter.role_type = -1;
			if(!CommonUtils.isNullOrEmpty(acc_dir_id)){
				viewAccountRoleFilter.dirId=Long.parseLong(acc_dir_id);
			}
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(viewAccountRoleFilter);

		}
		json.put("monitors", realHisCfgDeviceList);
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

		JSONObject data = new JSONObject();
		Page<RealHisCfgData> realHisCfgDataList = null;

		RealHisCfgDataFilter realHisCfgDataFilter = new RealHisCfgDataFilter();
		if (CommonUtils.isNullOrEmpty(real_his_cfg_id)) {
			data.put("realHisCfgDataList", "");
			return new Output(data);

		}
		realHisCfgDataFilter.real_his_cfg_id = Long.parseLong(real_his_cfg_id);
		realHisCfgDataFilter.start_date = start_date;
		realHisCfgDataFilter.end_date = end_date;
		realHisCfgDataFilter.state = 1;

		realHisCfgDataList = realHisCfgDataApi.getRealHisCfgDataList(realHisCfgDataFilter, pageIndex, pageSize);

		data.put("realHisCfgDataList", realHisCfgDataList);
		return new Output(data);

	}

}
