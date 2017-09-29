package com.wecon.box.console.task;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.wecon.box.util.JPushServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.AlarmCfgDataApi;
import com.wecon.box.api.DevFirmApi;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RealHisCfgDataApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.console.config.ConnectOptions;
import com.wecon.box.console.util.MqttConfigContext;
import com.wecon.box.console.util.SpringContextHolder;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.AlarmCfgData;
import com.wecon.box.entity.DevFirm;
import com.wecon.box.entity.Device;
import com.wecon.box.entity.PiBoxCom;
import com.wecon.box.entity.PiBoxComAddr;
import com.wecon.box.entity.PiBoxHisCom;
import com.wecon.box.entity.PiBoxHisComAddr;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.RealHisCfgData;
import com.wecon.box.entity.RedisPiBoxActData;
import com.wecon.common.redis.RedisManager;
import com.wecon.common.util.CommonUtils;

/**
 * @author lanpenghui 2017年9月2日下午5:09:01
 */
public class MonitorTask extends Thread {
	public static MqttClient client;
	private final String clientId = "WECON_REVEIVE_DATA";
	private String serverTopic = "pibox/cts/#";
	private final int BASE_DATA = 1000;
	private final int REAL_DATA = 1001;
	private final int HISTORY_DATA = 1002;
	private final int ALARM_DATA = 1003;
	private final int WILL_DATA = 1004;

	private static final Logger logger = LogManager.getLogger(MonitorTask.class);
	private static int sleepTime = 1000 * 30;

	public void run() {
		logger.info("MonitorTask run start");
		while (true) {
			try {
				if (client == null || !client.isConnected()) {
					logger.info("mqtt connection is disconnection !");
					connect();
				}
				sleep(sleepTime);

			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	/**
	 *
	 */
	private void connect() {
		try {

			MqttConnectOptions options = ConnectOptions.getConnectOptions(MqttConfigContext.mqttConfig.getUsername(),
					MqttConfigContext.mqttConfig.getPassword());
			System.out.println("to connect mqtt......");
			logger.info("to connect mqtt......");

			client = new MqttClient(MqttConfigContext.mqttConfig.getHost(), clientId, new MemoryPersistence());
			client.connect(options);
			// 订阅盒子的所有发送主题
			client.subscribe(serverTopic);
			System.out.println("MQTT connection is successful !");
			logger.info("MQTT connection is successful !");
			client.setCallback(new MqttCallback() {

				public void messageArrived(String topic, MqttMessage message) throws Exception {
					manageData(topic, message);
				}

				public void deliveryComplete(IMqttDeliveryToken token) {
					logger.info("deliveryComplete执行");
				}

				public void connectionLost(Throwable cause) {
					if (!client.isConnected()) {
						logger.info("Connection is broken  !");
					}

				}

			});

		} catch (MqttException e) {
			logger.info("MqttException e==" + e.getMessage());
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
			logger.info("主题为空！");
			System.out.println("主题为空！");
			return;
		}
		if (message.getPayload().length < 1) {
			logger.info("消息为空！");
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
				logger.info("主题中的机器码和消息的机器码不匹配！");
				System.out.println("主题中的机器码和消息的机器码不匹配！");
				return;
			}
			// 数据为空
			if (CommonUtils.isNullOrEmpty(jsonObject.getString("data"))) {
				logger.info("data为空！");
				System.out.println("data为空！");
				return;
			}
			// act为空
			if (CommonUtils.isNullOrEmpty(jsonObject.getInteger("act"))) {
				logger.info("act为空！");
				System.out.println("act为空！");
				return;
			}
			if (CommonUtils.isNullOrEmpty(jsonObject.getInteger("feedback"))) {
				logger.info("feedback为空！");
				System.out.println("feedback为空！");
				return;
			}
			Integer act = jsonObject.getInteger("act");
			DeviceApi deviceApi = SpringContextHolder.getBean(DeviceApi.class);
			switch (act) {
			// 基础数据
			case BASE_DATA:
				System.out.println("基础数据接收");
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
					if (!newdevice.machine_code.equals(machineCode)) {
						logger.info("主题的机器码和数据中的机器码不一致");
						return;
					}
					if (olddevice != null) {
						// 已经存在，直接更新device
						olddevice.password = newdevice.password;
						olddevice.state = newdevice.state;
						olddevice.dev_model = newdevice.dev_model;
						logger.info("接收的基础数据=" + jsonBase.getString("device_info"));
						logger.info("基础数据的机器码=" + olddevice.machine_code);
						logger.info("基础数据从数据库中获取的name=" + olddevice.name);
						deviceApi.updateDevice(olddevice);
						System.out.println("device modify success");
					} else {

						// 保存device
						deviceApi.saveDevice(newdevice);
						olddevice = deviceApi.getDevice(machineCode);
						System.out.println("device add success");

					}
				}
				// 固件版本信息
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
							newdevFirm.f_id = olddevFirm.f_id;
							devFirmApi.updateDevFirm(newdevFirm);
						} else {
							// 固件保存
							devFirmApi.saveDevFirm(newdevFirm);
						}
					}
				}
				if (jsonObject.getInteger("feedback") == 1) {
					SendMessage(machineCode, BASE_DATA);
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
				//发布消息
				RedisManager.publish(ConstKey.REDIS_GROUP_NAME, machineCode, machineCode);
				if (jsonObject.getInteger("feedback") == 1) {
					SendMessage(machineCode, REAL_DATA);
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
				for (int i = 0; i < listpiBoxHisCom.size(); i++) {
					if (listpiBoxHisCom.get(i).addr_list == null || listpiBoxHisCom.get(i).addr_list.size() < 1) {
						continue;
					}
					// 得到上报的串口ID
					String com = listpiBoxHisCom.get(i).com;
					if (!CommonUtils.isNullOrEmpty(com)) {
						// 数据配置接口
						RealHisCfgApi RealHisCfgApi = SpringContextHolder.getBean(RealHisCfgApi.class);
						// 通过串口ID和配置信息状态获取
						List<RealHisCfg> listrealHisCfg = RealHisCfgApi.getRealHisCfg(Long.parseLong(com), -1);
						if (listrealHisCfg == null || listrealHisCfg.size() < 1) {
							return;
						}
						List<PiBoxHisComAddr> listPiBoxHisComAddr = listpiBoxHisCom.get(i).addr_list;
						// 保存历史数据列表
						List<RealHisCfgData> listInsertRealHisCfgData = new ArrayList<RealHisCfgData>();

						RealHisCfgData realHisCfgData;
						for (PiBoxHisComAddr piBoxHisComAddr : listPiBoxHisComAddr) {
							RealHisCfgData historyData = realHisCfgDataApi.getRealHisCfgData(
									Long.parseLong(piBoxHisComAddr.addr_id), Timestamp.valueOf(piBoxHisComAddr.time));
							if (historyData != null) {
								logger.info("mqtt代理服务器发的缓存数据，直接忽略！");
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
								logger.info("历史数据批量保存失败=="+e.getMessage());
								System.out.println("历史数据批量保存失败");
								e.printStackTrace();
							}
						}
					}

				}
				if (jsonObject.getInteger("feedback") == 1) {
					SendMessage(machineCode, HISTORY_DATA);
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
				for (int j = 0; j < listPiBoxCom.size(); j++) {
					List<PiBoxComAddr> listPiBoxComAddr = listPiBoxCom.get(j).addr_list;
					if (null == listPiBoxComAddr || listPiBoxComAddr.size() < 1) {
						continue;
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
							logger.info("mqtt代理服务器发的缓存数据，直接忽略！");
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
							alarmCfgDataApi.saveAlarmCfgData(listInsertAlarmCfgData);
							System.out.println("alarmCfgData add success !");
							//极光推送给客户端
							new JPushServer().push(JSON.toJSONString(listInsertAlarmCfgData));
							if (jsonObject.getInteger("feedback") == 1) {
								SendMessage(machineCode, ALARM_DATA);
							}

						} catch (Exception e) {
							logger.info("报警数据批量保存失败");
							System.out.println("报警数据批量保存失败");
							e.printStackTrace();
						}
					}

				}

				break;
			case WILL_DATA:
				JSONObject jsonData = jsonObject.getJSONObject("data");
				logger.info("盒子离线发的遗嘱消息,machine_code:" + jsonData.getString("machine_code"));
				if (!CommonUtils.isNullOrEmpty(jsonData.getString("machine_code"))) {
					if (machineCode.equals(jsonData.getString("machine_code"))) {
						Device device = deviceApi.getDevice(machineCode);
						if (device != null) {
							device.state = 0;// 设置为离线
							deviceApi.updateDevice(device);
						}

					}

				}

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

	public void SendMessage(String machineCode, int code) {
		// 反馈成功信息
		MqttMessage message = new MqttMessage();
		JSONObject json = new JSONObject();
		json.put("act", "1");
		json.put("feedback_act", code);
		json.put("machine_code", machineCode);
		json.put("code", "1");
		json.put("msg", "success");
		message.setPayload(json.toString().getBytes());
		message.setQos(2);
		message.setRetained(true);
		// 需要反馈盒子的主题
		String topic = "pibox/stc/" + machineCode;
		MqttTopic mqtttopic = client.getTopic(topic);
		PublishTask publish = new PublishTask(mqtttopic, message);
		publish.start();

	}
}
