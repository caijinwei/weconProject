package com.wecon.box.action.data;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DevBindUserApi;
import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RealHisCfgDataApi;
import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.DevBindUser;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.PlcInfo;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.RealHisCfgData;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.enums.DataTypeOption;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.DevBindUserFilter;
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
	private PlcInfoApi plcInfoApi;
	@Autowired
	private ViewAccountRoleApi viewAccountRoleApi;
	@Autowired
	private DevBindUserApi devBindUserApi;

	@WebApi(forceAuth = true, master = true)
	@Description("获取改账户下的plc和对应的监控点")
	@RequestMapping(value = "/getComMonitor")
	public Output getComMonitor(@RequestParam("device_id") String device_id) {

		Client client = AppContext.getSession().client;
		JSONObject json = new JSONObject();
		// 获取历史数据配置信息
		RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
		List<RealHisCfgDevice> realHisCfgDeviceList = null;
		if (client.userInfo.getUserType() == 1) {
			/** 管理 **/
			realHisCfgFilter.addr_type = -1;
			realHisCfgFilter.data_type = 1;
			realHisCfgFilter.his_cycle = -1;
			realHisCfgFilter.state = -1;
			realHisCfgFilter.account_id = client.userId;
			realHisCfgFilter.dirId = -1;
			if (!CommonUtils.isNullOrEmpty(device_id)) {
				realHisCfgFilter.device_id = Long.parseLong(device_id);
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
			viewAccountRoleFilter.state = -1;
			viewAccountRoleFilter.dirId = -1;
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(viewAccountRoleFilter);

		}
		json.put("type", client.userInfo.getUserType());
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
		Page<RealHisCfgData> realHisCfgDataList = new Page<RealHisCfgData>(pageIndex, pageSize, 0);

		RealHisCfgDataFilter realHisCfgDataFilter = new RealHisCfgDataFilter();
		if (CommonUtils.isNullOrEmpty(real_his_cfg_id)) {

			data.put("realHisCfgDataList", realHisCfgDataList);
			return new Output(data);

		}
		realHisCfgDataFilter.real_his_cfg_id = Long.parseLong(real_his_cfg_id);
		realHisCfgDataFilter.start_date = start_date;
		realHisCfgDataFilter.end_date = end_date;
		realHisCfgDataFilter.state = -1;

		realHisCfgDataList = realHisCfgDataApi.getRealHisCfgDataList(realHisCfgDataFilter, pageIndex, pageSize);

		data.put("realHisCfgDataList", realHisCfgDataList);
		return new Output(data);

	}

	@Description("获取数据登记信息")
	@WebApi(forceAuth = true, master = true)
	@RequestMapping(value = "/getHisConfig")
	public Output getHisConfig(@RequestParam("device_id") String device_id,
			@RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
		Client client = AppContext.getSession().client;
		JSONObject json = new JSONObject();
		/** 获取历史数据配置信息 **/
		RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
		/** 通过视图获取历史配置信息 **/
		ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();

		Page<RealHisCfgDevice> realHisCfgDeviceList = null;
		if (client.userInfo.getUserType() == 1) {
			/** 管理 **/
			realHisCfgFilter.addr_type = -1;
			realHisCfgFilter.data_type = 1;
			realHisCfgFilter.his_cycle = -1;
			realHisCfgFilter.state = -1;
			realHisCfgFilter.bind_state = 1;

			realHisCfgFilter.account_id = client.userId;
			if (!CommonUtils.isNullOrEmpty(device_id)) {
				realHisCfgFilter.device_id = Long.parseLong(device_id);
			}

			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter, pageIndex, pageSize);
			for (int i = 0; i < realHisCfgDeviceList.getList().size(); i++) {
				RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.getList().get(i);
				// 整数位 小数位分割
				if (realHisCfgDevice.digit_count != null) {
					String[] numdecs = realHisCfgDevice.digit_count.split(",");
					if (numdecs != null) {
						if (numdecs.length == 1) {
							realHisCfgDevice.num = numdecs[0];
						} else if (numdecs.length == 2) {
							realHisCfgDevice.num = numdecs[0];
							realHisCfgDevice.dec = numdecs[1];
						}
					}
				}
				// 主子编号范围分割
				if (realHisCfgDevice.data_limit != null) {
					String[] numdecs = realHisCfgDevice.data_limit.split(",");
					if (numdecs != null) {
						if (numdecs.length == 1) {
							realHisCfgDevice.main_limit = numdecs[0];
						} else if (numdecs.length == 2) {
							realHisCfgDevice.main_limit = numdecs[0];
							realHisCfgDevice.child_limit = numdecs[1];
						}
					}
				}
				// 主子编号进制分割
				if (realHisCfgDevice.digit_binary != null) {
					String[] numdecs = realHisCfgDevice.digit_binary.split(",");
					if (numdecs != null) {
						if (numdecs.length == 1) {
							realHisCfgDevice.main_binary = numdecs[0];
						} else if (numdecs.length == 2) {
							realHisCfgDevice.main_binary = numdecs[0];
							realHisCfgDevice.child_binary = numdecs[1];
						}
					}
				}
				// 主子地址分割
				String[] addrs = realHisCfgDevice.addr.split(",");
				if (addrs != null) {
					if (addrs.length == 1) {
						realHisCfgDevice.main_addr = addrs[0];
					} else if (addrs.length == 2) {
						realHisCfgDevice.main_addr = addrs[0];
						realHisCfgDevice.child_addr = addrs[1];
					}
				}
				PlcInfo plcInfo = plcInfoApi.getPlcInfo(realHisCfgDevice.plc_id);
				if (plcInfo != null) {
					realHisCfgDevice.condevice = plcInfo.port;
				}

				realHisCfgDevice.data_value = DataTypeOption.getDataTypeValue(realHisCfgDevice.data_id);

			}

		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/

			viewAccountRoleFilter.view_id = client.userId;
			viewAccountRoleFilter.cfg_type = 1;
			viewAccountRoleFilter.data_type = 1;
			viewAccountRoleFilter.role_type = -1;
			viewAccountRoleFilter.state = -1;
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(viewAccountRoleFilter, pageIndex, pageSize);
			for (int i = 0; i < realHisCfgDeviceList.getList().size(); i++) {
				RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.getList().get(i);
				PlcInfo plcInfo = plcInfoApi.getPlcInfo(realHisCfgDevice.plc_id);
				realHisCfgDevice.condevice = plcInfo.port;
				realHisCfgDevice.data_value = DataTypeOption.getDataTypeValue(realHisCfgDevice.data_id);

			}
		}
		json.put("HisAllotData", realHisCfgDeviceList);
		return new Output(json);
	}

	@Description("删除数据登记数据")
	@WebApi(forceAuth = true, master = true)
	@RequestMapping(value = "/delHisMonitor")
	public Output delHisMonitor(@RequestParam("monitorid") String monitorid) {
		if (!CommonUtils.isNullOrEmpty(monitorid)) {
			Client client = AppContext.getSession().client;
			RealHisCfg realHisCfg = realHisCfgApi.getRealHisCfg(Long.parseLong(monitorid));
			DevBindUserFilter devBindUser=new DevBindUserFilter();
			devBindUser.account_id=client.userId;
			devBindUser.device_id=realHisCfg.device_id;
			List<DevBindUser> listDeviceBindUser=devBindUserApi.getDevBindUser(devBindUser);
			if(listDeviceBindUser.size()!=1){
				throw new BusinessException(ErrorCodeOption.Device_AlreadyBind.key,
						ErrorCodeOption.Device_AlreadyBind.value);
			}
			viewAccountRoleApi.deletePoint(1, Long.parseLong(monitorid));

		
			realHisCfg.state = 3;// 删除配置状态
			realHisCfgApi.updateRealHisCfg(realHisCfg);
		}

		return new Output();

	}

}
