package com.wecon.box.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AccountApi;
import com.wecon.box.api.AccountDirApi;
import com.wecon.box.api.AccountDirRelApi;
import com.wecon.box.api.DevBindUserApi;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.entity.AccountDir;
import com.wecon.box.entity.AccountDirRel;
import com.wecon.box.entity.DevBindUser;
import com.wecon.box.entity.Device;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Client;
import com.wecon.restful.core.Output;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by caijinw on 2017/8/8.
 */
@RestController
@RequestMapping(value = "baseInfoAction")
public class DeviceAction {
	@Autowired
	DeviceApi deviceApi;

	@Autowired
	AccountApi accountApi;

	@Autowired
	DevBindUserApi devBindUserApi;

	@Autowired
	AccountDirRelApi accountDirRelApi;
	@Autowired
	private AccountDirApi accountDirApi;

	/**
	 * 盒子基本信息的展示
	 *
	 * @param device_id
	 * @return
	 */
	@RequestMapping(value = "showBaseInfo")
	@WebApi(forceAuth = true, master = true)
	public Output showBaseInfo(@RequestParam("device_id") Integer device_id) {
		/*
		 * 验证信息(不知道怎么写)
		 */
		Device device = deviceApi.getDevice(device_id);
		JSONObject data = new JSONObject();
		data.put("device", device);

		return new Output(data);
	}

	/*
	 * 测试 没有用户登入等验证 直接先传入user_id
	 */
	@WebApi(forceAuth = true, master = true, authority = { "1" })
	@RequestMapping(value = "/boundBox")
	public Output boundBox(@RequestParam("machine_code") String machine_code, @RequestParam("password") String password,
			@RequestParam("name") String name, @RequestParam("acc_dir_id") Integer acc_dir_id) {

		/*
		 * 验证是否该盒子是否存在
		 */
		Device device = deviceApi.getDevice(machine_code);
		if (device == null) {
			throw new BusinessException(ErrorCodeOption.Device_NotFound.key, ErrorCodeOption.Device_NotFound.value);
		} else if (!device.password.equals(password) || !device.name.equals(name)) {
			throw new BusinessException(ErrorCodeOption.Device_NotFound.key, ErrorCodeOption.Device_NotFound.value);
		}
		long device_id = device.device_id;

		/*
		 * 该设备没有被别的用户绑定过
		 */
		if (devBindUserApi.findByDevId(device_id) != 0) {
			throw new BusinessException(ErrorCodeOption.Device_AlreadyBind.key,
					ErrorCodeOption.Device_AlreadyBind.value);
		}

		DevBindUser model = new DevBindUser();
		model.account_id = AppContext.getSession().client.userId;
		model.device_id = device_id;
		System.out.println("执行到这边  设备号ID是" + device_id);
		devBindUserApi.saveDevBindUser(model);
		/*
		 * 有选择分组操作 默认分组 ref_id=0
		 *
		 */
		if (acc_dir_id != 0) {
			AccountDirRel accountDirRel = new AccountDirRel();
			accountDirRel.ref_id = device_id;
			accountDirRel.acc_dir_id = acc_dir_id;
			accountDirRelApi.saveAccountDirRel(accountDirRel);
		}
		return new Output();
	}

	@WebApi(forceAuth = true, master = true)
	@Description("根据用户获取分组下的盒子")
	@RequestMapping(value = "/getBoxGroup")
	public Output getBoxGroup() {
		Client client = AppContext.getSession().client;
		JSONObject json = new JSONObject();
		JSONArray devicearr = null;
		JSONArray allarr = new JSONArray();
		JSONObject data = null;
		JSONObject devicedata = null;

		List<Device> deviceList = null;

		List<AccountDir> accountDirList = accountDirApi.getAccountDirList(client.userId, 0);

		if (accountDirList == null || accountDirList.size() < 1) {
			// 没有盒子分组默认创建
			AccountDir accountDir = new AccountDir();
			accountDir.account_id = client.userId;
			accountDir.type = 0;
			accountDir.name = "默认组";
			long id = accountDirApi.addAccountDir(accountDir);
			if (id > 0) {
				accountDirList = accountDirApi.getAccountDirList(client.userId, 0);
				
			} else {
				throw new BusinessException(ErrorCodeOption.Get_DeviceList_Error.key,
						ErrorCodeOption.Get_DeviceList_Error.value);
			}
		}

		for (int i = 0; i < accountDirList.size(); i++) {
			AccountDir accountDir = accountDirList.get(i);
			data = new JSONObject();

			data.put("accountdirId", accountDir.id);
			data.put("accountdirName", accountDir.name);

			deviceList = deviceApi.getDeviceList(client.userId, accountDir.id);
			if (deviceList != null) {
				for (int j = 0; j < deviceList.size(); j++) {

					devicedata = new JSONObject();
					devicearr = new JSONArray();
					Device device = deviceList.get(j);
					devicedata.put("deviceId", device.device_id);
					devicedata.put("deviceName", device.name);
					devicearr.add(devicedata);

				}
				data.put("deviceList", devicearr);
			}

			allarr.add(data);

		}

		json.put("allData", allarr);

		return new Output(json);

	}

}
