package com.wecon.box.action.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.log.Log;
import com.wecon.box.api.*;
import com.wecon.box.constant.Constant;
import com.wecon.box.entity.*;
import com.wecon.box.entity.plcdom.AddrDom;
import com.wecon.box.entity.plcdom.Plc;
import com.wecon.box.entity.plcdom.Res;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.box.filter.DevBindUserFilter;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.box.param.RealHisCfgParam;
import com.wecon.box.util.DbLogUtil;
import com.wecon.box.util.OptionUtil;
import com.wecon.box.util.PlcTypeParser;
import com.wecon.box.util.PlcTypeQuerier;
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

import static org.hamcrest.CoreMatchers.endsWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lanpenghui 2017年8月9日下午1:43:32
 */
@RestController
@RequestMapping("actDataAction")
public class ActDataAction {

	@Autowired
	private RealHisCfgApi realHisCfgApi;
	@Autowired
	private AccountDirApi accountDirApi;
	@Autowired
	private AccountDirRelApi accountDirRelApi;
	@Autowired
	private OptionUtil optionService;
	@Autowired
	private PlcInfoApi plcInfoApi;
	@Autowired
	private ViewAccountRoleApi viewAccountRoleApi;
	@Autowired
	private DevBindUserApi devBindUserApi;
	@Autowired
	protected DbLogUtil dbLogUtil;

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

	@WebApi(forceAuth = true, master = true, authority = { "1" })
	@Description("删除实时数据分组")
	@RequestMapping(value = "/delActGroup")
	public Output delActGroup(@RequestParam("id") Integer id) {
		AccountDir dir = accountDirApi.getAccountDir(id);
		if (dir.device_id > 0) {
			List<AccountDirRel> dirrel = accountDirRelApi.getAccountDirRel(dir.id);
			if (dirrel != null && dirrel.size() > 0) {
				for (AccountDirRel acc : dirrel) {
					RealHisCfg realHisCfg = realHisCfgApi.getRealHisCfg(acc.ref_id);
					if (realHisCfg != null) {
						realHisCfg.state = 3;
						realHisCfgApi.updateRealHisCfg(realHisCfg);// 删除分组下的监控点
					}

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
	 * 更新实时监控点名称
	 * 
	 * @return
	 */
	@WebApi(forceAuth = true, master = true)
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

	/**
	 * 复制监控点到其他组
	 * 
	 * @return
	 */
	@WebApi(forceAuth = true, master = true)
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
	@WebApi(forceAuth = true, master = true)
	@Description("移动监控点到其他组")
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
	 * 移除监控点
	 * 
	 * @return
	 */
	@WebApi(forceAuth = true, master = true)
	@Description("移除监控点")
	@RequestMapping(value = "/delMonitor")
	public Output delMonitor(@RequestParam("monitorid") String monitorid, @RequestParam("acc_dir_id") String acc_dir_id,
			@RequestParam("isdel") String isdel) {
		Client client = AppContext.getSession().client;
		if (CommonUtils.isNullOrEmpty(monitorid) || CommonUtils.isNullOrEmpty(acc_dir_id)
				|| CommonUtils.isNullOrEmpty(isdel)) {
			throw new BusinessException(ErrorCodeOption.Get_Data_Error.key, ErrorCodeOption.Get_Data_Error.value);
		}
		if (1 == Integer.parseInt(isdel)) {
			// 移除该分组下的监控点
			accountDirRelApi.delAccountDir(Long.parseLong(acc_dir_id), Long.parseLong(monitorid));
		} else {
			RealHisCfg realHisCfg = realHisCfgApi.getRealHisCfg(Long.parseLong(monitorid));
			DevBindUserFilter devBindUser = new DevBindUserFilter();
			devBindUser.account_id = client.userId;
			devBindUser.device_id = realHisCfg.device_id;
			List<DevBindUser> listDeviceBindUser = devBindUserApi.getDevBindUser(devBindUser);
			if (listDeviceBindUser.size() != 1) {
				throw new BusinessException(ErrorCodeOption.Device_AlreadyBind.key,
						ErrorCodeOption.Device_AlreadyBind.value);
			}

			// 1.删除分配给视图账号的配置
			viewAccountRoleApi.deletePoint(1, Long.parseLong(monitorid));
			// 2.更改配置状态，等待盒子发送数据把配置物理删除

			realHisCfg.state = 3;// 删除配置状态
			realHisCfgApi.updateRealHisCfg(realHisCfg);
			accountDirRelApi.delAccountDir(Long.parseLong(acc_dir_id), Long.parseLong(monitorid));
			if (realHisCfg.data_type == 0) {// 删除实时数据配置日志
				dbLogUtil.addOperateLog(OpTypeOption.DelAct, ResTypeOption.Act, realHisCfg.id, realHisCfg);

			} else {
				// 删除历史数据配置日志
				dbLogUtil.addOperateLog(OpTypeOption.DelHis, ResTypeOption.His, realHisCfg.id, realHisCfg);
			}

		}

		return new Output();

	}

	@Description("获取分配监控点")
	@WebApi(forceAuth = true, master = true)
	@RequestMapping(value = "/getAllotMonitor")
	public Output getAllotMonitor(@RequestParam("device_id") String device_id,
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
			realHisCfgFilter.state = 3;
			realHisCfgFilter.bind_state = 1;

			realHisCfgFilter.account_id = client.userId;
			if (!CommonUtils.isNullOrEmpty(device_id)) {
				realHisCfgFilter.device_id = Long.parseLong(device_id);
			}

			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter, pageIndex, pageSize);

		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/

			viewAccountRoleFilter.view_id = client.userId;
			viewAccountRoleFilter.cfg_type = 1;
			viewAccountRoleFilter.data_type = 0;
			viewAccountRoleFilter.role_type = -1;
			viewAccountRoleFilter.state = 3;
			realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(viewAccountRoleFilter, pageIndex, pageSize);
		}
		json.put("actAllotData", realHisCfgDeviceList);
		return new Output(json);
	}

	@Description("分配监控点")
	@WebApi(forceAuth = true, master = true)
	@RequestMapping(value = "/allotMonitor")
	public Output allotMonitor(@RequestParam("acc_dir_id") String acc_dir_id,
			@RequestParam("selectedId") String selectedId) {
		List<AccountDirRel> listAccountDirRel = new ArrayList<AccountDirRel>();
		AccountDirRel accountDirRel = null;
		if (!CommonUtils.isNullOrEmpty(acc_dir_id) && !CommonUtils.isNullOrEmpty(selectedId)) {
			String[] ids = selectedId.split(",");

			for (int i = 0; i < ids.length; i++) {
				accountDirRel = accountDirRelApi.getAccountDirRel(Long.parseLong(acc_dir_id), Long.parseLong(ids[i]));

				if (accountDirRel == null) {
					RealHisCfg realHisCfg = realHisCfgApi.getRealHisCfg(Long.parseLong(ids[i]));
					if (realHisCfg != null) {
						accountDirRel = new AccountDirRel();
						accountDirRel.acc_dir_id = Long.parseLong(acc_dir_id);
						accountDirRel.ref_id = realHisCfg.id;
						accountDirRel.ref_alais = realHisCfg.name;
						listAccountDirRel.add(accountDirRel);

					}

				}
			}
			if (listAccountDirRel.size() > 0) {
				accountDirRelApi.saveAccountDirRel(listAccountDirRel);
			}

		}

		return new Output();
	}

	/**
	 * 获取数据类型
	 * 
	 * @return
	 */
	@Description("获取数据类型")
	@WebApi(forceAuth = true, master = true)
	@RequestMapping(value = "/getDataType")
	public Output getDataType() {
		JSONObject json = new JSONObject();
		json.put("DataTypeOption", optionService.getDataTypeOptionOptions());
		System.out.println("显示数据类型=="+json.toString());

		return new Output(json);

	}

	/**
	 * 获取地址类型和对应的寄存器类型
	 * 
	 * @return
	 */
	@Description("获取地址类型和对应的寄存器类型")
	@WebApi(forceAuth = true, master = true)
	@RequestMapping(value = "/getAddrType")
	public Output getAddrType(@RequestParam("plc_id") String plc_id) {
		JSONObject json = new JSONObject();
		if (!CommonUtils.isNullOrEmpty(plc_id)) {
			JSONObject data = null;
			JSONObject attr = null;
			JSONArray arr = null;
			JSONArray allarr = new JSONArray();
			PlcInfo plcInfo = plcInfoApi.getPlcInfo(Long.parseLong(plc_id));
			PlcTypeParser.doParse();// 解析plcType文件中的数据填充到po
			if (plcInfo != null) {
				List<Plc> result = PlcTypeQuerier.getInstance().query("plctype", plcInfo.type);
				Map<String, AddrDom> plcs = result.get(0).getAddrs();
				AddrDom addrDom = null;
				if (null != plcs.get("bitaddr")) {
					data = new JSONObject();
					arr = new JSONArray();
					addrDom = plcs.get("bitaddr");
					data.put("addrkey", 0);

					List<Res> ResList = addrDom.getResList();
					for (int i = 0; i < ResList.size(); i++) {
						attr = new JSONObject();
						Map<String, String> attris = ResList.get(i).getAttributes();
						String value = attris.get("Rid");
						String range = attris.get("MRange");
						String mJinzhi = attris.get("MJinzhi");// 整数位进制 d-十进制//
																// o-八进制 h-十六进制
						if ("d".equals(mJinzhi.toLowerCase())) {
							mJinzhi = "十进制";

						} else if ("o".equals(mJinzhi.toLowerCase())) {
							mJinzhi = "八进制";
							String[] ranges = range.split(" ");
							StringBuffer sb = new StringBuffer();
							String.valueOf(Long.toOctalString(Long.parseLong(ranges[0])));
							String.valueOf(Long.toOctalString(Long.parseLong(ranges[1])));
							sb.append(String.valueOf(Long.toOctalString(Long.parseLong(ranges[0]))));
							sb.append(" ");
							sb.append(String.valueOf(Long.toOctalString(Long.parseLong(ranges[1]))));
							range = sb.toString();

						} else if ("h".equals(mJinzhi.toLowerCase())) {
							mJinzhi = "十六进制";
							String[] ranges = range.split(" ");
							StringBuffer sb = new StringBuffer();
							sb.append(String.valueOf(Long.toHexString(Long.parseLong(ranges[0]))));
							sb.append(" ");
							sb.append(String.valueOf(Long.toHexString(Long.parseLong(ranges[1]))));
							range = sb.toString();
						}
						String bitCount = attris.get("BitCount");
						attr.put("range", range);
						attr.put("addrvalue", value);
						attr.put("mJinzhi", mJinzhi);
						if (!CommonUtils.isNullOrEmpty(bitCount)) {
							if (0 != Integer.parseInt(bitCount)) {// 有小数位数
								String bRange = attris.get("BRange");
								String bJinzhi = attris.get(
										"BJinzhi");/**
													 * 整数位进制 d-十进制 o-八进制 h-十六进制
													 **/
								if ("d".equals(bJinzhi.toLowerCase())) {
									bJinzhi = "十进制";

								} else if ("o".equals(bJinzhi.toLowerCase())) {
									bJinzhi = "八进制";
									String[] ranges = bRange.split(" ");
									StringBuffer sb = new StringBuffer();
									sb.append(String.valueOf(Long.toOctalString(Long.parseLong(ranges[0]))));
									sb.append(" ");
									sb.append(String.valueOf(Long.toOctalString(Long.parseLong(ranges[1]))));
									bRange = sb.toString();
								} else if ("h".equals(bJinzhi.toLowerCase())) {
									bJinzhi = "十六进制";
									String[] bRanges = bRange.split(" ");
									StringBuffer sb = new StringBuffer();
									sb.append(String.valueOf(Long.toHexString(Long.parseLong(bRanges[0]))));
									sb.append(" ");
									sb.append(String.valueOf(Long.toHexString(Long.parseLong(bRanges[1]))));
									bRange = sb.toString();
								}
								attr.put("bRange", bRange);
								attr.put("bitCount", bitCount);
								attr.put("bJinzhi", bJinzhi);

							}
						}

						arr.add(attr);
					}
					if (arr.size() > 0) {
						data.put("addrRid", arr);
					}

					allarr.add(data);
				}
				if (null != plcs.get("byteaddr")) {
					addrDom = plcs.get("byteaddr");
					data = new JSONObject();
					arr = new JSONArray();
					data.put("addrkey", 1);
					List<Res> ResList = addrDom.getResList();
					for (int i = 0; i < ResList.size(); i++) {
						attr = new JSONObject();
						Map<String, String> attris = ResList.get(i).getAttributes();
						String value = attris.get("Rid");
						String range = attris.get("Range");
						String Jinzhi = attris.get("Jinzhi");// 整数位进制 d-十进制//
																// o-八进制 h-十六进制
						if ("d".equals(Jinzhi.toLowerCase())) {
							Jinzhi = "十进制";
						} else if ("o".equals(Jinzhi.toLowerCase())) {
							Jinzhi = "八进制";
							String[] ranges = range.split(" ");
							StringBuffer sb = new StringBuffer();
							String.valueOf(Long.toOctalString(Long.parseLong(ranges[0])));
							String.valueOf(Long.toOctalString(Long.parseLong(ranges[1])));
							sb.append(String.valueOf(Long.toOctalString(Long.parseLong(ranges[0]))));
							sb.append(" ");
							sb.append(String.valueOf(Long.toOctalString(Long.parseLong(ranges[1]))));
							range = sb.toString();
						} else if ("h".equals(Jinzhi.toLowerCase())) {
							Jinzhi = "十六进制";
							String[] ranges = range.split(" ");
							StringBuffer sb = new StringBuffer();
							sb.append(String.valueOf(Long.toHexString(Long.parseLong(ranges[0]))));
							sb.append(" ");
							sb.append(String.valueOf(Long.toHexString(Long.parseLong(ranges[1]))));
							range = sb.toString();
						}
						attr.put("addrvalue", value);
						attr.put("range", range);
						attr.put("mJinzhi", Jinzhi);
						arr.add(attr);
					}
					if (arr.size() > 0) {
						data.put("addrRid", arr);
					}
					allarr.add(data);

				}
				if (null != plcs.get("wordaddr")) {
					addrDom = plcs.get("wordaddr");
					data = new JSONObject();
					arr = new JSONArray();
					data.put("addrkey", 2);
					List<Res> ResList = addrDom.getResList();
					for (int i = 0; i < ResList.size(); i++) {
						attr = new JSONObject();
						Map<String, String> attris = ResList.get(i).getAttributes();
						String value = attris.get("Rid");
						String range = attris.get("Range");
						String Jinzhi = attris.get("Jinzhi");// 整数位进制 d-十进制//
																// o-八进制 h-十六进制
						if ("d".equals(Jinzhi.toLowerCase())) {
							Jinzhi = "十进制";
						} else if ("o".equals(Jinzhi.toLowerCase())) {
							Jinzhi = "八进制";
							String[] ranges = range.split(" ");
							StringBuffer sb = new StringBuffer();
							String.valueOf(Long.toOctalString(Long.parseLong(ranges[0])));
							String.valueOf(Long.toOctalString(Long.parseLong(ranges[1])));
							sb.append(String.valueOf(Long.toOctalString(Long.parseLong(ranges[0]))));
							sb.append(" ");
							sb.append(String.valueOf(Long.toOctalString(Long.parseLong(ranges[1]))));
							range = sb.toString();
						} else if ("h".equals(Jinzhi.toLowerCase())) {
							Jinzhi = "十六进制";
							String[] ranges = range.split(" ");
							StringBuffer sb = new StringBuffer();
							sb.append(String.valueOf(Long.toHexString(Long.parseLong(ranges[0]))));
							sb.append(" ");
							sb.append(String.valueOf(Long.toHexString(Long.parseLong(ranges[1]))));
							range = sb.toString();
						}
						attr.put("addrvalue", value);
						attr.put("range", range);
						attr.put("mJinzhi", Jinzhi);
						arr.add(attr);
					}
					if (arr.size() > 0) {
						data.put("addrRid", arr);
					}
					allarr.add(data);

				}
				if (null != plcs.get("dwordaddr")) {
					addrDom = plcs.get("dwordaddr");
					data = new JSONObject();
					arr = new JSONArray();
					data.put("addrkey", 3);
					List<Res> ResList = addrDom.getResList();
					for (int i = 0; i < ResList.size(); i++) {
						attr = new JSONObject();
						Map<String, String> attris = ResList.get(i).getAttributes();
						String value = attris.get("Rid");
						String range = attris.get("Range");
						String Jinzhi = attris.get("Jinzhi");// 整数位进制 d-十进制//
																// o-八进制 h-十六进制
						if ("d".equals(Jinzhi.toLowerCase())) {
							Jinzhi = "十进制";
						} else if ("o".equals(Jinzhi.toLowerCase())) {
							Jinzhi = "八进制";
							String[] ranges = range.split(" ");
							StringBuffer sb = new StringBuffer();
							String.valueOf(Long.toOctalString(Long.parseLong(ranges[0])));
							String.valueOf(Long.toOctalString(Long.parseLong(ranges[1])));
							sb.append(String.valueOf(Long.toOctalString(Long.parseLong(ranges[0]))));
							sb.append(" ");
							sb.append(String.valueOf(Long.toOctalString(Long.parseLong(ranges[1]))));
							range = sb.toString();
						} else if ("h".equals(Jinzhi.toLowerCase())) {
							Jinzhi = "十六进制";
							String[] ranges = range.split(" ");
							StringBuffer sb = new StringBuffer();
							sb.append(String.valueOf(Long.toHexString(Long.parseLong(ranges[0]))));
							sb.append(" ");
							sb.append(String.valueOf(Long.toHexString(Long.parseLong(ranges[1]))));
							range = sb.toString();
						}
						attr.put("addrvalue", value);
						attr.put("range", range);
						attr.put("mJinzhi", Jinzhi);
						arr.add(attr);
					}
					if (arr.size() > 0) {
						data.put("addrRid", arr);
					}
					allarr.add(data);

				}

			}
			json.put("allAddr", allarr);

		}

		return new Output(json);

	}

	@Description("添加或者修改监控点")
	@WebApi(forceAuth = true, master = true, authority = { "1" })
	@RequestMapping(value = "/addUpdataMonitor")
	public Output addUpdataMonitor(@Valid RealHisCfgParam realHisCfgParam) {
		JSONObject json = new JSONObject();
		long account_id = AppContext.getSession().client.userId;
		DevBindUserFilter devBindUser = new DevBindUserFilter();
		devBindUser.account_id = account_id;
		devBindUser.device_id = realHisCfgParam.device_id;
		List<DevBindUser> listDeviceBindUser = devBindUserApi.getDevBindUser(devBindUser);
		if (listDeviceBindUser.size() != 1) {
			throw new BusinessException(ErrorCodeOption.Device_AlreadyBind.key,
					ErrorCodeOption.Device_AlreadyBind.value);
		}
		if (realHisCfgParam != null) {
			RealHisCfg oldrealHisCfg = null;
			RealHisCfg realHisCfg = null;
			if (realHisCfgParam.id > 0) {
				RealHisCfg oldre = realHisCfgApi.getRealHisCfg(realHisCfgParam.device_id, realHisCfgParam.name,realHisCfgParam.data_type);
				if (oldre != null && oldre.id != realHisCfgParam.id) {
					throw new BusinessException(ErrorCodeOption.Name_Repetition.key,
							ErrorCodeOption.Name_Repetition.value);
				}
				oldrealHisCfg = realHisCfgApi.getRealHisCfg(realHisCfgParam.id);
				realHisCfg = realHisCfgApi.getRealHisCfg(realHisCfgParam.id);
				realHisCfg.account_id = account_id;
				realHisCfg.addr = realHisCfgParam.addr;
				realHisCfg.addr_type = realHisCfgParam.addr_type;
				realHisCfg.data_id = realHisCfgParam.data_id;
				realHisCfg.ext_unit=realHisCfgParam.unit;
				if (realHisCfgParam.name.length() > 64) {
					realHisCfg.name = realHisCfgParam.name.substring(0, 64);
				} else {
					realHisCfg.name = realHisCfgParam.name;
				}

				realHisCfg.plc_id = realHisCfgParam.plc_id;
				realHisCfg.device_id = realHisCfgParam.device_id;
				realHisCfg.rid = realHisCfgParam.rid;
				realHisCfg.data_limit = realHisCfgParam.rang;
				realHisCfg.digit_count = realHisCfgParam.digit_count;
				realHisCfg.digit_binary = realHisCfgParam.digit_binary;
				if (!CommonUtils.isNullOrEmpty(realHisCfgParam.describe)) {
					if (realHisCfgParam.describe.length() > 64) {
						realHisCfg.describe = realHisCfgParam.describe.substring(0, 64);
					} else {
						realHisCfg.describe = realHisCfgParam.describe;
					}

				}
				realHisCfg.plc_id = realHisCfgParam.plc_id;
				realHisCfg.data_type = realHisCfgParam.data_type;
				if (realHisCfgParam.data_type == 0) {
					if (realHisCfgParam.group_id < 1) {

						throw new BusinessException(ErrorCodeOption.Get_Groupid_Error.key,
								ErrorCodeOption.Get_Groupid_Error.value);
					}
				} else {
					if (realHisCfgParam.his_cycle > 0) {
						realHisCfg.his_cycle = realHisCfgParam.his_cycle;
					}

				}
				realHisCfg.state = 2;// 更新配置

				boolean issuccess = realHisCfgApi.updateRealHisCfg(realHisCfg);

				if (issuccess) {
					if (realHisCfg.data_type == 0) {// 实时数据配置
						dbLogUtil.updOperateLog(OpTypeOption.UpdAct, ResTypeOption.Act, realHisCfg.id, oldrealHisCfg,
								realHisCfg);
					} else {
						dbLogUtil.updOperateLog(OpTypeOption.UpdHis, ResTypeOption.His, realHisCfg.id, oldrealHisCfg,
								realHisCfg);
					}

					if (realHisCfgParam.data_type == 0) {
						AccountDirRel accountDirRel = accountDirRelApi.getAccountDirRel(realHisCfgParam.group_id,
								realHisCfg.id);
						if (accountDirRel == null) {
							accountDirRel = new AccountDirRel();
							accountDirRel.acc_dir_id = realHisCfgParam.group_id;
							accountDirRel.ref_id = realHisCfg.id;
							accountDirRel.ref_alais = realHisCfg.name;
							accountDirRelApi.saveAccountDirRel(accountDirRel);
						} else {
							accountDirRel.acc_dir_id = realHisCfgParam.group_id;
							accountDirRel.ref_id = realHisCfg.id;
							accountDirRel.ref_alais = realHisCfg.name;
							accountDirRelApi.upAccountDirRel(accountDirRel);
						}
					}
				}
				json.put("success", 0);

			} else {
				RealHisCfg newre = realHisCfgApi.getRealHisCfg(realHisCfgParam.device_id, realHisCfgParam.name,realHisCfgParam.data_type);
				if (newre != null) {
					throw new BusinessException(ErrorCodeOption.Name_Repetition.key,
							ErrorCodeOption.Name_Repetition.value);
				}
				realHisCfg = new RealHisCfg();
				realHisCfg.account_id = account_id;
				realHisCfg.addr_type = realHisCfgParam.addr_type;
				realHisCfg.data_id = realHisCfgParam.data_id;
				realHisCfg.plc_id = realHisCfgParam.plc_id;
				realHisCfg.device_id = realHisCfgParam.device_id;
				realHisCfg.rid = realHisCfgParam.rid;
				realHisCfg.data_limit = realHisCfgParam.rang;
				realHisCfg.digit_count = realHisCfgParam.digit_count;
				realHisCfg.digit_binary = realHisCfgParam.digit_binary;
				realHisCfg.ext_unit=realHisCfgParam.unit;
				realHisCfg.state = 1;// 0：已同步给盒子1：新增配置2：更新配置3：删除配置，如果同步成功再做物理删除，同时需要删除监控点绑定和权限的分配，和其他相关数据
				if (!CommonUtils.isNullOrEmpty(realHisCfgParam.describe)) {
					if (realHisCfgParam.describe.length() > 64) {
						realHisCfg.describe = realHisCfgParam.describe.substring(0, 64);
					} else {
						realHisCfg.describe = realHisCfgParam.describe;
					}

				}
				realHisCfg.plc_id = realHisCfgParam.plc_id;
				realHisCfg.data_type = realHisCfgParam.data_type;
				List<RealHisCfgDevice> realHisCfgDeviceList = new ArrayList<RealHisCfgDevice>();
				RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
				if (realHisCfgParam.data_type == 0) {
					if (realHisCfgParam.group_id < 1) {

						throw new BusinessException(ErrorCodeOption.Get_Groupid_Error.key,
								ErrorCodeOption.Get_Groupid_Error.value);
					}
					/** 管理 **/
					realHisCfgFilter.addr_type = -1;
					realHisCfgFilter.data_type = 0;
					realHisCfgFilter.his_cycle = -1;
					realHisCfgFilter.state = 3;
					realHisCfgFilter.bind_state = 1;
					realHisCfgFilter.account_id = account_id;
					realHisCfgFilter.device_id = realHisCfgParam.device_id;

					realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter);
					if (realHisCfgDeviceList.size() >= Constant.AddNum.ACT_SET_NUM) {
						throw new BusinessException(ErrorCodeOption.Act_add_Beyond.key,
								ErrorCodeOption.Act_add_Beyond.value);
					}
				} else {
					if (realHisCfgParam.his_cycle > 0) {
						realHisCfg.his_cycle = realHisCfgParam.his_cycle;
					}
					/** 管理 **/
					realHisCfgFilter.addr_type = -1;
					realHisCfgFilter.data_type = 1;
					realHisCfgFilter.his_cycle = -1;
					realHisCfgFilter.state = 3;
					realHisCfgFilter.bind_state = 1;
					realHisCfgFilter.account_id = account_id;
					realHisCfgFilter.device_id = realHisCfgParam.device_id;

					realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter);
					if (realHisCfgDeviceList.size() >= Constant.AddNum.HIS_SET_NUM) {
						throw new BusinessException(ErrorCodeOption.His_add_Beyond.key,
								ErrorCodeOption.His_add_Beyond.value);
					}

				}

				if (realHisCfgParam.batch > 0) {

					if (!CommonUtils.isNullOrEmpty(realHisCfgParam.digit_binary)
							&& !CommonUtils.isNullOrEmpty(realHisCfgParam.rang)
							&& !CommonUtils.isNullOrEmpty(realHisCfgParam.addr)) {
						for (int i = 0; i < realHisCfgParam.batch; i++) {
							if (realHisCfgParam.data_type == 0) {
								realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter);
								if (realHisCfgDeviceList.size() >= Constant.AddNum.ACT_SET_NUM) {
									break;
								}
							}
							String binarys[] = realHisCfgParam.digit_binary.split(",");
							String rangs[] = realHisCfgParam.rang.split(",");
							String addrs[] = realHisCfgParam.addr.split(",");
							if (binarys.length == 1) {
								String limit[] = rangs[0].split(" ");
								if ("八进制".equals(binarys[0])) {
									// 前端设置的主编号地址
									long setaddr = Long.parseLong(addrs[0], 8);
									int inc = Integer.parseInt(realHisCfgParam.increase) * i;
									long increase = Long.parseLong(String.valueOf(inc));
									// 添加后地址
									long afteraddr = setaddr + increase;
									if (Long.parseLong(limit[0], 8) > afteraddr
											|| afteraddr > Long.parseLong(limit[1], 8)) {
										continue;
									}
									realHisCfg.addr = String.valueOf(Long.toOctalString(afteraddr));

								} else if ("十进制".equals(binarys[0])) {
									// 前端设置的主编号地址
									long setaddr = Long.parseLong(addrs[0]);

									int inc = Integer.parseInt(realHisCfgParam.increase) * i;
									long increase = Long.parseLong(String.valueOf(inc));
									// 添加后地址
									long afteraddr = setaddr + increase;
									if (Long.parseLong(limit[0]) > afteraddr || afteraddr > Long.parseLong(limit[1])) {
										continue;
									}
									realHisCfg.addr = String.valueOf(afteraddr);

								} else if ("十六进制".equals(binarys[0])) {
									// 前端设置的主编号地址
									long setaddr = Long.parseLong(addrs[0], 16);
									int inc = Integer.parseInt(realHisCfgParam.increase) * i;
									long increase = Long.parseLong(String.valueOf(inc));
									// 添加后地址
									long afteraddr = setaddr + increase;

									if (Long.parseLong(limit[0], 16) > afteraddr
											|| afteraddr > Long.parseLong(limit[1], 16)) {
										continue;
									}
									realHisCfg.addr = String.valueOf(Long.toHexString(afteraddr));

								}

							} else if (binarys.length == 2) {

								String dig_limit[] = rangs[1].split(" ");// 子编号范围
								long remainder = 0;// 余数
								long jinwei = 0;
								if ("八进制".equals(binarys[1])) {

									// 前端设置的子编号地址
									long childaddr = Long.parseLong(addrs[1], 8);

									int inc = Integer.parseInt(realHisCfgParam.increase) * i;
									long increase = Long.parseLong(String.valueOf(inc));
									// 添加后地址
									long afteraddr = childaddr + increase;
									if (Long.parseLong(dig_limit[0], 8) > afteraddr) {
										continue;
									}
									if (Long.parseLong(dig_limit[1]) <= afteraddr) {
										if (afteraddr % Long.parseLong(dig_limit[1], 8) == 0) {
											jinwei = afteraddr / Long.parseLong(dig_limit[1], 8);
											addrs[1] = String.valueOf(Long.toOctalString(0));
										} else {
											jinwei = afteraddr / Long.parseLong(dig_limit[1], 8);
											remainder = afteraddr % Long.parseLong(dig_limit[1], 8);
											addrs[1] = String.valueOf(Long.toOctalString(remainder));
										}

									} else {

										addrs[1] = String.valueOf(Long.toOctalString(afteraddr));// 没进位
									}

								} else if ("十进制".equals(binarys[1])) {
									// 前端设置的子编号地址
									long childaddr = Long.parseLong(addrs[1]);

									int inc = Integer.parseInt(realHisCfgParam.increase) * i;
									long increase = Long.parseLong(String.valueOf(inc));
									// 添加后地址
									long afteraddr = childaddr + increase;
									if (Long.parseLong(dig_limit[0]) > afteraddr) {
										continue;
									}
									if (Long.parseLong(dig_limit[1]) <= afteraddr) {
										if (afteraddr % Long.parseLong(dig_limit[1]) == 0) {
											jinwei = afteraddr / Long.parseLong(dig_limit[1]);
											addrs[1] = "0";
										} else {
											jinwei = afteraddr / Long.parseLong(dig_limit[1]);
											remainder = afteraddr % Long.parseLong(dig_limit[1]);
											addrs[1] = String.valueOf(remainder);
										}

									} else {

										addrs[1] = String.valueOf(afteraddr);// 没进位
									}

								} else if ("十六进制".equals(binarys[1])) {

									// 前端设置的子编号地址
									long childaddr = Long.parseLong(addrs[1], 16);

									int inc = Integer.parseInt(realHisCfgParam.increase) * i;
									long increase = Long.parseLong(String.valueOf(inc));
									// 添加后地址
									long afteraddr = childaddr + increase;
									if (Long.parseLong(dig_limit[0], 16) > afteraddr) {
										continue;
									}
									if (Long.parseLong(dig_limit[1], 16) <= afteraddr) {
										if (afteraddr % Long.parseLong(dig_limit[1], 16) == 0) {
											jinwei = afteraddr / Long.parseLong(dig_limit[1], 16);
											addrs[1] = String.valueOf(Long.toHexString(0));
										} else {
											jinwei = afteraddr / Long.parseLong(dig_limit[1], 16);
											remainder = afteraddr % Long.parseLong(dig_limit[1], 16);
											addrs[1] = String.valueOf(Long.toHexString(remainder));
										}

									} else {

										addrs[1] = String.valueOf(String.valueOf(Long.toHexString(afteraddr)));// 没进位
									}
								}
								String limit[] = rangs[0].split(" ");// 主编号范围
								if ("八进制".equals(binarys[0])) {
									long mainaddr = Long.parseLong(addrs[0], 8);
									long afteraddr = mainaddr + jinwei;
									if (Long.parseLong(limit[0], 8) > afteraddr
											|| afteraddr > Long.parseLong(limit[1], 8)) {
										continue;
									}
									addrs[0] = String.valueOf(Long.toOctalString(afteraddr));// 主编号地址
									realHisCfg.addr = addrs[0] + "," + addrs[1];

								} else if ("十进制".equals(binarys[0])) {
									long mainaddr = Long.parseLong(addrs[0]);
									long afteraddr = mainaddr + jinwei;
									if (Long.parseLong(limit[0]) > afteraddr || afteraddr > Long.parseLong(limit[1])) {
										continue;
									}
									addrs[0] = String.valueOf(afteraddr);// 主编号地址

									realHisCfg.addr = addrs[0] + "," + addrs[1];

								} else if ("十六进制".equals(binarys[0])) {
									long mainaddr = Long.parseLong(addrs[0], 16);
									long afteraddr = mainaddr + jinwei;
									if (Long.parseLong(limit[0], 16) > afteraddr
											|| afteraddr > Long.parseLong(limit[1], 16)) {
										continue;
									}
									addrs[0] = String.valueOf(String.valueOf(Long.toHexString(afteraddr)));// 主编号地址
									realHisCfg.addr = addrs[0] + "," + addrs[1];

								}

							}
							if (i > 0) {
								if (realHisCfgParam.name.length() > 50) {
									realHisCfg.name = realHisCfgParam.name.substring(0, 46) + "_" + i;
								} else {
									realHisCfg.name = realHisCfgParam.name + "_" + i;
								}
							} else {
								if (realHisCfgParam.name.length() > 50) {
									realHisCfg.name = realHisCfgParam.name.substring(0, 50);
								} else {
									realHisCfg.name = realHisCfgParam.name;
								}
							}

							RealHisCfg rename = realHisCfgApi.getRealHisCfg(realHisCfgParam.device_id, realHisCfg.name);
							if (rename != null) {
								realHisCfg.name = realHisCfgParam.name + "_" + new Date().getTime();
							}

							long id = realHisCfgApi.saveRealHisCfg(realHisCfg);

							if (id > 0) {
								if (realHisCfg.data_type == 0) {// 实时数据配置
									dbLogUtil.addOperateLog(OpTypeOption.AddAct, ResTypeOption.Act, realHisCfg.id,
											realHisCfg);
								} else {
									// 历史数据配置
									dbLogUtil.addOperateLog(OpTypeOption.AddHis, ResTypeOption.His, realHisCfg.id,
											realHisCfg);
								}

								if (realHisCfgParam.data_type == 0) {
									realHisCfg = realHisCfgApi.getRealHisCfg(id);
									AccountDirRel accountDirRel = accountDirRelApi
											.getAccountDirRel(realHisCfgParam.group_id, realHisCfg.id);
									if (accountDirRel == null) {
										accountDirRel = new AccountDirRel();
										accountDirRel.acc_dir_id = realHisCfgParam.group_id;
										accountDirRel.ref_id = realHisCfg.id;
										accountDirRel.ref_alais = realHisCfg.name;
										accountDirRelApi.saveAccountDirRel(accountDirRel);
									}
								}
							}
						}

					}

				} else {

					if (realHisCfgParam.name.length() > 50) {
						realHisCfg.name = realHisCfgParam.name.substring(0, 50);
					} else {
						realHisCfg.name = realHisCfgParam.name;
					}
					realHisCfg.addr = realHisCfgParam.addr;
					long reid = realHisCfgApi.saveRealHisCfg(realHisCfg);
					if (reid > 0) {
						if (realHisCfg.data_type == 0) {// 实时数据配置
							dbLogUtil.addOperateLog(OpTypeOption.AddAct, ResTypeOption.Act, realHisCfg.id, realHisCfg);
						} else {
							// 历史数据配置
							dbLogUtil.addOperateLog(OpTypeOption.AddHis, ResTypeOption.His, realHisCfg.id, realHisCfg);
						}

						if (realHisCfgParam.data_type == 0) {
							realHisCfg = realHisCfgApi.getRealHisCfg(reid);
							AccountDirRel accountDirRel = accountDirRelApi.getAccountDirRel(realHisCfgParam.group_id,
									realHisCfg.id);
							if (accountDirRel == null) {
								accountDirRel = new AccountDirRel();
								accountDirRel.acc_dir_id = realHisCfgParam.group_id;
								accountDirRel.ref_id = realHisCfg.id;
								accountDirRel.ref_alais = realHisCfg.name;
								accountDirRelApi.saveAccountDirRel(accountDirRel);
							}
						}
					}
				}

				json.put("success", 1);
			}

		}

		return new Output(json);

	}

}
