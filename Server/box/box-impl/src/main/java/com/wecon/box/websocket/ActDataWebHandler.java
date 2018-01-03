package com.wecon.box.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.*;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.box.util.ClientMQTT;
import com.wecon.box.util.DbLogUtil;
import com.wecon.box.util.SendValue;
import com.wecon.box.util.SendvalueCallback;
import com.wecon.common.redis.RedisManager;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Client;
import redis.clients.jedis.JedisPubSub;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lanph on 2017/9/13.
 */
public class ActDataWebHandler extends AbstractWebSocketHandler {
	@Autowired
	private RedisPiBoxApi redisPiBoxApi;
	@Autowired
	private RealHisCfgApi realHisCfgApi;
	@Autowired
	private DeviceApi deviceApi;
	private Map<String, Client> clients = new HashMap<String, Client>();
	private Map<String, SubscribeListener> subscribeListeners = new HashMap<String, SubscribeListener>();
	private Map<String, Map<String, Object>> paramMaps = new HashMap<String, Map<String, Object>>();
	private Map<String, ClientMQTT> clientMQTTs = new HashMap<String, ClientMQTT>();
	private Map<String, List<String>> machineCodeCache = new HashMap<>();
	@Autowired
	protected DbLogUtil dbLogUtil;
	@Autowired
	private SendValue sendValue;

	private static final Logger logger = LogManager.getLogger(ActDataHandler.class.getName());

	/**
	 * 收到消息
	 *
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {

		try {
			String params = message.getPayload();
			logger.debug("Server received message: " + params);
			if (CommonUtils.isNullOrEmpty(params)) {
				return;
			}
			Map<String, Object> bParams = JSON.parseObject(params, new TypeReference<Map<String, Object>>() {
			});

			if ("0".equals(bParams.get("markid").toString())) {

				logger.debug("WebSocket push begin");
				if (paramMaps.containsKey(session.getId())) {
					paramMaps.remove(session.getId());
				}
				paramMaps.put(session.getId(), bParams);
				session.sendMessage(new TextMessage(getStringRealData(session)));

				logger.debug("WebSocket push end");

			} else if ("1".equals(bParams.get("markid").toString())) {
				String value = bParams.get("value").toString();
				String addr_id = bParams.get("addr_id").toString();
				// 订阅消息
				if (!CommonUtils.isNullOrEmpty(addr_id)) {
					RealHisCfg realHisCfg = realHisCfgApi.getRealHisCfg(Long.parseLong(addr_id));
					Device device = deviceApi.getDevice(realHisCfg.device_id);
					String subscribeTopic = "pibox/cts/" + device.machine_code;
					SendvalueCallback sendvalueCallback = new SendvalueCallback(session, addr_id);
					ClientMQTT reclient = new ClientMQTT(subscribeTopic, "send" + session.getId(), sendvalueCallback);
					reclient.start();
					clientMQTTs.put(session.getId(), reclient);
					sendValue.putMQTTMess(value, session, addr_id, OpTypeOption.WriteAct, reclient);
				}
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
	}

	/**
	 * 订阅redis消息
	 */
	private void subscribeRealData(WebSocketSession session, final Set<String> machineCodeSet) {
		final SubscribeListener subscribeListener = new SubscribeListener(session);
		subscribeListeners.put(session.getId(), subscribeListener);
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
		cachedThreadPool.execute(new Runnable() {
			public void run() {
				logger.debug("Redis begin subscribe realData");
				String[] machineCodeArray = new String[machineCodeSet.size()];
				int i = 0;
				for (String machineCode : machineCodeSet) {
					machineCodeArray[i++] = machineCode;
				}
				RedisManager.subscribe(ConstKey.REDIS_GROUP_NAME, subscribeListener, machineCodeArray);

			}
		});
	}

	class SubscribeListener extends JedisPubSub {
		/**
		 *
		 */
		private WebSocketSession session;

		public SubscribeListener(WebSocketSession session) {
			this.session = session;

		}

		@Override
		public void onMessage(String channel, String message) {
			logger.debug("Subscribe callback，channel：" + channel + "message:" + message);
			if (!CommonUtils.isNullOrEmpty(message)
					&& "0".equals(paramMaps.get(session.getId()).get("markid").toString())) {
				try {
					session.sendMessage(new TextMessage(getStringRealData(session)));
					if (null != clientMQTTs.get(session.getId())) {
						clientMQTTs.get(session.getId()).close();
						clientMQTTs.remove(session.getId());
					}

				} catch (Exception e) {
					e.printStackTrace();
					logger.debug("Subscribe callback error，" + e.getMessage());
				}
			}
		}
	}

	private String getStringRealData(WebSocketSession session) {
		try {

			JSONObject json = new JSONObject();
			/** 获取实时数据配置信息 **/
			RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
			/** 通过视图获取配置信息 **/
			ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();
			Client client = clients.get(session.getId());
			Map<String, Object> bParams = paramMaps.get(session.getId());
			logger.debug("显示client==" + client.userInfo.toString());
			logger.debug("显示session.getId()==" + session.getId());
			if (client != null) {
				Page<RealHisCfgDevice> realHisCfgDeviceList = null;
				if (client.userInfo.getUserType() == 1) {
					/** 管理 **/
					realHisCfgFilter.addr_type = -1;
					realHisCfgFilter.data_type = 0;
					realHisCfgFilter.his_cycle = -1;
					realHisCfgFilter.state = 3;
					realHisCfgFilter.bind_state = 1;
					realHisCfgFilter.account_id = client.userId;

					if (!CommonUtils.isNullOrEmpty(bParams.get("acc_dir_id"))) {
						realHisCfgFilter.dirId = Long.parseLong(bParams.get("acc_dir_id").toString());
					}
					if (!CommonUtils.isNullOrEmpty(bParams.get("device_id"))) {
						realHisCfgFilter.device_id = Long.parseLong(bParams.get("device_id").toString());
					}
					realHisCfgDeviceList = realHisCfgApi.getRealHisCfgList(realHisCfgFilter,
							Integer.parseInt(bParams.get("pageIndex").toString()),
							Integer.parseInt(bParams.get("pageSize").toString()));

				} else if (client.userInfo.getUserType() == 2) {
					/** 视图 **/

					viewAccountRoleFilter.view_id = client.userId;
					viewAccountRoleFilter.cfg_type = 1;
					viewAccountRoleFilter.data_type = 0;
					viewAccountRoleFilter.role_type = -1;
					viewAccountRoleFilter.state = 3;
					if (!CommonUtils.isNullOrEmpty(bParams.get("acc_dir_id"))) {
						viewAccountRoleFilter.dirId = Long.parseLong(bParams.get("acc_dir_id").toString());
					}
					realHisCfgDeviceList = realHisCfgApi.getRealHisCfgList(viewAccountRoleFilter,
							Integer.parseInt(bParams.get("pageIndex").toString()),
							Integer.parseInt(bParams.get("pageSize").toString()));
				}
				Set<String> machineCodeSet = null;
				// 如果该账号下无实时数据配置文件直接返回空
				if (realHisCfgDeviceList != null && realHisCfgDeviceList.getList().size() > 0) {
					machineCodeSet = new HashSet<>();
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
						String device_machine = realHisCfgDevice.machine_code;
						List<String> machineCodeList = machineCodeCache.get(session.getId());
						if(null == machineCodeList){
							machineCodeList = new ArrayList<>();
							machineCodeCache.put(session.getId(), machineCodeList);
						}
						if(!CommonUtils.isNullOrEmpty(device_machine) && !machineCodeList.contains(device_machine)){
							machineCodeSet.add(device_machine);
							machineCodeList.add(device_machine);
						}
						Device device = deviceApi.getDevice(device_machine);
						realHisCfgDevice.box_state = device.state;
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
											realHisCfgDevice.re_value =piBoxComAddr.value;
											

										}

									}

								}

							}
						}
					}
				}
				if (machineCodeSet != null && machineCodeSet.size() > 0) {

					subscribeRealData(session, machineCodeSet);
				}
				json.put("piBoxActDateMode", realHisCfgDeviceList);

			}
			logger.debug("Websocket push msg: " + json.toJSONString());
			return json.toJSONString();
		} catch (Exception e) {
			logger.debug("Server error，" + e.getMessage());
			e.printStackTrace();
			return "服务器错误";
		}
	}

	/**
	 * 连接成功后
	 *
	 * @param session
	 * @throws Exception
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		try {
			logger.debug("连接成功");
			Client client = AppContext.getSession().client;
			clients.put(session.getId(), client);

		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("afterConnectionEstablished，" + e.getMessage());
		}

	}

	/**
	 * 关闭连接后
	 *
	 * @param session
	 * @param status
	 * @throws Exception
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		try {
			logger.debug("关闭连接");
			if (clients.containsKey(session.getId())) {
				clients.remove(session.getId());

			}
			if (subscribeListeners.containsKey(session.getId())) {
				subscribeListeners.get(session.getId()).unsubscribe();
				subscribeListeners.remove(session.getId());

			}
			if (paramMaps.containsKey(session.getId())) {
				paramMaps.remove(session.getId());

			}
			if (clientMQTTs.containsKey(session.getId())) {
				clientMQTTs.get(session.getId()).close();
				clientMQTTs.remove(session.getId());

			}
			machineCodeCache.get(session.getId()).clear();
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("afterConnectionClosed，" + e.getMessage());
		}
	}
}
