/**
 * @功能说明: 监控任务
 * @创建人 : lph
 * @创建时间: 2017年07月26日
 * @修改人 :
 * @修改时间:
 * @修改描述:
 * @Copyright (c)2017 福州富昌维控电子科技有限公司-版权所有
 */
package com.wecon.box.console.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.entity.PiBoxComAddr;
import com.wecon.box.entity.PiBoxHisCom;
import com.wecon.box.entity.PiBoxHisComAddr;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.RealHisCfgData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.wecon.box.api.AlarmCfgDataApi;
import com.wecon.box.api.DevFirmApi;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RealHisCfgDataApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.console.config.ConnectOptions;
import com.wecon.box.console.util.MqttConfigContext;
import com.wecon.box.console.util.SpringContextHolder;
import com.wecon.box.entity.AlarmCfgData;
import com.wecon.box.entity.DevFirm;
import com.wecon.box.entity.Device;
import com.wecon.box.entity.PiBoxCom;
import com.wecon.box.entity.RedisPiBoxActData;
import com.wecon.common.util.CommonUtils;

public class MonitorTaskJob implements Job {
	public static MqttClient client;
	private final int BASE_DATA = 1000;
	private final int REAL_DATA = 1001;
	private final int HISTORY_DATA = 1002;
	private final int ALARM_DATA = 1003;

	/**
	 * 实时监控是否连上代理服务器
	 *
	 * @param job
	 */
	public void execute(JobExecutionContext job) throws JobExecutionException {

		try {
			if (client != null && client.isConnected()) {
				System.out.println("The connection server is normal !");
				return;
			}

			MqttConnectOptions options = ConnectOptions.getConnectOptions(MqttConfigContext.mqttConfig.getUsername(),
					MqttConfigContext.mqttConfig.getPassword());
			System.out.println("to connect mqtt......");
			client = new MqttClient(MqttConfigContext.mqttConfig.getHost(), "WECON_REVEIVE", new MemoryPersistence());
			client.connect(options);
			// 订阅盒子的所有发送主题
			client.subscribe("pibox/cts/#");
			System.out.println("MQTT connection is successful !");
			client.setCallback(new MqttCallback() {

				public void messageArrived(String topic, MqttMessage message) throws Exception {
					manageData(topic, message);
				}

				public void deliveryComplete(IMqttDeliveryToken token) {

				}

				public void connectionLost(Throwable cause) {
				}

			});

		} catch (MqttException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 主题和消息处理
	 */
	public void manageData(String topic, MqttMessage message) {
		String[] idexs = topic.split("/");
		// 如果未获取到主题直接返回
		if (null == idexs || idexs.length < 1) {
			System.out.println("主题为空！");
			return;
		}
		if (message.getPayload().length < 1) {
			System.out.println("消息为空！");
			return;
		}
		String boxMsg = "";
		try {
			boxMsg = new String(message.getPayload(), "UTF-8").trim();
			JSONObject jsonObject = JSONObject.parseObject(boxMsg);
			System.out.println("jsonObject=" + jsonObject);
			String machineCode = jsonObject.getString("machine_code");
			// 机器码为空消息直接忽略
			if (CommonUtils.isNullOrEmpty(jsonObject.getString("machine_code"))) {
				return;
			}
			// 如果消息的机器码和主题中的机器码不匹配直接忽略消息
			if (!idexs[2].equals(machineCode)) {
				System.out.println("主题中的机器码和消息的机器码不匹配！");
				return;
			}
			// 数据为空
			if (CommonUtils.isNullOrEmpty(jsonObject.getString("data"))) {
				System.out.println("data为空！");
				return;
			}
			// act为空
			if (CommonUtils.isNullOrEmpty(jsonObject.getInteger("act"))) {
				System.out.println("act为空！");
				return;
			}
			Integer act = jsonObject.getInteger("act");
			switch (act) {
			// 基础数据
			case BASE_DATA:
				System.out.println("基础数据接收");
				DeviceApi deviceApi = SpringContextHolder.getBean(DeviceApi.class);
				// 获取基础上报数据
				JSONObject jsonBase = jsonObject.getJSONObject("data");
				Device olddevice = deviceApi.getDevice(machineCode);
				// 设备信息
				if (!CommonUtils.isNullOrEmpty(jsonBase.getString("device_info"))) {

					Device newdevice = JSON.parseObject(jsonBase.getString("device_info"), new TypeReference<Device>() {
					});
					if (newdevice == null) {
						return;
					}
					if (olddevice != null) {
						// 已经存在，直接更新device
						newdevice.device_id=olddevice.device_id;
						deviceApi.updateDevice(newdevice);
						System.out.println("device modify success");
					} else {
						// 保存device
						deviceApi.saveDevice(newdevice);
						System.out.println("device add success");
					}
				}
				// 估计版本信息
				if (!CommonUtils.isNullOrEmpty(jsonBase.getString("dev_firm"))) {
					if (olddevice != null) {
						DevFirm newdevFirm = JSON.parseObject(jsonBase.getString("dev_firm"),
								new TypeReference<DevFirm>() {
								});
						if (newdevFirm == null) {
							return;
						}
						newdevFirm.device_id = olddevice.device_id;
						DevFirmApi devFirmApi = SpringContextHolder.getBean(DevFirmApi.class);
						DevFirm olddevFirm = devFirmApi.getDevFirm_device_id(olddevice.device_id);
						if (null != olddevFirm) {
							// 固件更新
							newdevFirm.f_id=olddevFirm.f_id;
							devFirmApi.updateDevFirm(newdevFirm);
						} else {
							// 固件保存
							devFirmApi.SaveDevFirm(newdevFirm);
						}
					}
				}
				break;
			// 实时数据
			case REAL_DATA:
				System.out.println("实时数据接收");
				RedisPiBoxApi redisPiBoxApi = SpringContextHolder.getBean(RedisPiBoxApi.class);
				// 获取redis实时数据
				RedisPiBoxActData redisModel = redisPiBoxApi.getRedisPiBoxActData(machineCode);
				RedisPiBoxActData newModel = JSON.parseObject(jsonObject.getString("data"),
						new TypeReference<RedisPiBoxActData>() {
						});
				if (redisModel != null && redisModel.act_time_data_list != null && newModel != null
						&& newModel.act_time_data_list != null) {
					redisModel.time = newModel.time;
					for (PiBoxCom newIt : newModel.act_time_data_list) {
						boolean comIsExit = false;
						for (int i = 0; i < redisModel.act_time_data_list.size(); i++) {
							PiBoxCom redisIt = redisModel.act_time_data_list.get(i);

							if (newIt.com.equals(redisIt.com)) {
								comIsExit = true;
								// <editor-fold desc="检查地址列表">
								for (PiBoxComAddr newAddrIt : newIt.addr_list) {
									boolean addrIsExit = false;
									for (int j = 0; j < redisIt.addr_list.size(); j++) {
										PiBoxComAddr redisAddrIt = redisIt.addr_list.get(j);
										if (newAddrIt.addr_id.equals(redisAddrIt.addr_id)) {
											addrIsExit = true;
											redisAddrIt.state = newAddrIt.state;
											redisAddrIt.value = newAddrIt.value;
											break;
										}
									}
									if (!addrIsExit) {
										redisIt.addr_list.add(newAddrIt);
									}
								}
								break;
							}
						}
						if (!comIsExit) {
							redisModel.act_time_data_list.add(newIt);
						}
					}
					redisPiBoxApi.saveRedisPiBoxActData(redisModel);
					System.out.println("redis modify success");
				} else if (newModel != null && newModel.act_time_data_list != null) {
					newModel.machine_code = jsonObject.getString("machine_code");
					redisPiBoxApi.saveRedisPiBoxActData(newModel);
					System.out.println("redis add success");
				}

				break;
			// 历史数据
			case HISTORY_DATA:
				System.out.println("历史数据接收");
				RealHisCfgDataApi realHisCfgDataApi = SpringContextHolder.getBean(RealHisCfgDataApi.class);
				// 获取新的历史数据
				JSONObject jsonHis = jsonObject.getJSONObject("data");
				List<PiBoxHisCom> listpiBoxHisCom = JSON.parseObject(jsonHis.getString("history_data_com_list"),
						new TypeReference<List<PiBoxHisCom>>() {
						});
				if (listpiBoxHisCom == null) {
					return;
				}
				if (listpiBoxHisCom.get(0).dat_list == null || listpiBoxHisCom.get(0).dat_list.size() < 1) {
					return;
				}
				// 得到上报的串口ID
				String com = listpiBoxHisCom.get(0).com;
				if (!CommonUtils.isNullOrEmpty(com)) {
					// 数据配置接口
					RealHisCfgApi RealHisCfgApi = SpringContextHolder.getBean(RealHisCfgApi.class);
					// 通过串口ID和配置信息启用状态0-未启用 1-启用 得到配置信息列表
					List<RealHisCfg> listrealHisCfg = RealHisCfgApi.getRealHisCfg(Long.parseLong(com), 1);
					if (listrealHisCfg == null || listrealHisCfg.size() < 1) {
						return;
					}
					List<PiBoxHisComAddr> listPiBoxHisComAddr = listpiBoxHisCom.get(0).dat_list;
					// 保存历史数据列表
					List<RealHisCfgData> listInsertRealHisCfgData = new ArrayList<RealHisCfgData>();

					RealHisCfgData realHisCfgData;
					for (PiBoxHisComAddr piBoxHisComAddr : listPiBoxHisComAddr) {
						RealHisCfgData historyData = realHisCfgDataApi.getRealHisCfgData(
								Long.parseLong(piBoxHisComAddr.addr_id), Timestamp.valueOf(piBoxHisComAddr.time));
						if (historyData != null) {
							System.out.println("mqtt代理服务器发的缓存数据，直接忽略！");
							return;
						}
						realHisCfgData = new RealHisCfgData();
						realHisCfgData.monitor_time = Timestamp.valueOf(piBoxHisComAddr.time);
						realHisCfgData.real_his_cfg_id = Long.parseLong(piBoxHisComAddr.addr_id);
						realHisCfgData.value = piBoxHisComAddr.value;
						if (!CommonUtils.isNullOrEmpty(piBoxHisComAddr.state)) {
							realHisCfgData.state = Integer.parseInt(piBoxHisComAddr.state);
						}
						listInsertRealHisCfgData.add(realHisCfgData);
					}
					if (listInsertRealHisCfgData.size() > 0) {
						try {
							// 批量保存数据
							realHisCfgDataApi.saveRealHisCfgData(listInsertRealHisCfgData);
							System.out.println("realHisCfgData add success !");
						} catch (Exception e) {
							System.out.println("历史数据批量保存失败");
							e.printStackTrace();
						}
					}
				}
				break;
			// 报警数据
			case ALARM_DATA:
				System.out.println("报警数据接收");
				String feedback = jsonObject.getString("feedback");
				if (feedback.isEmpty()) {
					return;
				}
				if (!"1".equals(feedback)) {
					return;
				}
				// 获取报警数据
				JSONObject jsonAlarm = jsonObject.getJSONObject("data");
				List<PiBoxCom> listPiBoxCom = JSON.parseObject(jsonAlarm.getString("alarm_data_list"),
						new TypeReference<List<PiBoxCom>>() {
						});

				if (null == listPiBoxCom || listPiBoxCom.size() < 1) {
					return;
				}

				List<PiBoxComAddr> listPiBoxComAddr = listPiBoxCom.get(0).addr_list;
				if (null == listPiBoxComAddr || listPiBoxComAddr.size() < 1) {
					return;
				}
				// 获取AlarmCfgDataApi对象
				AlarmCfgDataApi alarmCfgDataApi = SpringContextHolder.getBean(AlarmCfgDataApi.class);
				// 保存报警数据列表
				List<AlarmCfgData> listInsertAlarmCfgData = new ArrayList<AlarmCfgData>();
				AlarmCfgData alarmCfgData = null;

				for (PiBoxComAddr piBoxComAddr : listPiBoxComAddr) {
					AlarmCfgData alarmData = alarmCfgDataApi.getAlarmCfgData(Long.parseLong(piBoxComAddr.addr_id),
							Timestamp.valueOf(jsonAlarm.getString("time")));
					if (alarmData != null) {
						System.out.println("mqtt代理服务器发的缓存数据，直接忽略！");
						return;

					}
					alarmCfgData = new AlarmCfgData();
					alarmCfgData.alarm_cfg_id = Long.parseLong(piBoxComAddr.addr_id);
					alarmCfgData.value = piBoxComAddr.value;
					alarmCfgData.monitor_time = Timestamp.valueOf(jsonAlarm.getString("time"));
					alarmCfgData.state = Integer.valueOf(piBoxComAddr.state);
					listInsertAlarmCfgData.add(alarmCfgData);
				}
				if (listInsertAlarmCfgData.size() > 0) {

					try {
						// 批量保存报警数据成功
						alarmCfgDataApi.SaveAlarmCfgData(listInsertAlarmCfgData);
						System.out.println("alarmCfgData add success !");
						// 反馈成功消息给盒子
						MqttMessage boxmessage = new MqttMessage();
						JSONObject json = new JSONObject();
						json.put("act", "1");
						json.put("feedback_act", ALARM_DATA);
						json.put("machine_code", jsonObject.getString("machine_code"));
						json.put("code", "1");
						json.put("msg", "success");
						System.out.println("报警反馈==" + json.toJSONString());
						boxmessage.setPayload(json.toString().getBytes());
						boxmessage.setQos(2);
						boxmessage.setRetained(true);
						// 需要反馈盒子的主题
						String boxtopic = "pibox/stc/" + jsonObject.getString("machine_code");
						MqttTopic mqtttopic = client.getTopic(boxtopic);
						PublishTask publish = new PublishTask(mqtttopic, boxmessage);
						publish.start();
					} catch (Exception e) {
						System.out.println("报警数据批量保存失败");
						e.printStackTrace();
					}
				}
				break;

			default:
				break;
			}
		} catch (Exception e) {
			String simplename = e.getClass().getSimpleName();
			if (!"JSONException".equals(simplename)) {
				e.printStackTrace();
			}

		}

	}

}
