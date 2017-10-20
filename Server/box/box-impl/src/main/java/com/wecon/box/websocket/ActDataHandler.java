package com.wecon.box.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.constant.Constant;
import com.wecon.box.entity.*;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.box.util.ClientMQTT;
import com.wecon.box.util.SendValue;
import com.wecon.box.util.SendvalueCallback;
import com.wecon.common.redis.RedisManager;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zengzhipeng on 2017/8/22.
 */
public class ActDataHandler extends AbstractWebSocketHandler {
	@Autowired
	private RedisPiBoxApi redisPiBoxApi;
	@Autowired
	private RealHisCfgApi realHisCfgApi;
	@Autowired
	private DeviceApi deviceApi;
	@Autowired
	private SendValue sendValue;

	private Map<String, Client> clientMap = new HashMap<>();

	private Map<String, SubscribeListener> subListenerMap = new HashMap<>();

	private Map<String, ClientMQTT> clientMQTTs = new HashMap<String, ClientMQTT>();

	private Map<String, String> paramMap = new HashMap<>();

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
			logger.debug("WebSocketSession id: " + session.getId());
			if (CommonUtils.isNullOrEmpty(params)) {
				return;
			}
			Map<String, Object> bParams = JSON.parseObject(params, new TypeReference<Map<String, Object>>() {});
			paramMap.put(session.getId(), params);
			if (null != bParams.get("markid") && "1".equals(bParams.get("markid").toString())) {
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

					// 发送数据
					sendValue.putMQTTMess(value, session, addr_id, OpTypeOption.WriteActPhone, reclient);
				}

			} else {
				// 推送消息给移动端
				logger.debug("WebSocket push begin");
				Object[] oj = getRealData(session);
				session.sendMessage(new TextMessage(oj[0].toString()));
				logger.debug("WebSocket push end");

				// 订阅redis消息
				SubscribeListener subscribeListener = subListenerMap.get(session.getId());
				if (null != oj[1] && null == subscribeListener) {
					subscribeRealData(session, (Set<String>) oj[1]);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("handleTextMessage，" + e.getMessage());
		}

	}

	/**
	 * 订阅redis消息
	 */
	private void subscribeRealData(final WebSocketSession session, final Set<String> machineCodeSet) {
		final String[] machineCodeArray = new String[machineCodeSet.size()];
		int i = 0;
		for (String machineCode : machineCodeSet) {
			machineCodeArray[i++] = machineCode;
		}

		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
		cachedThreadPool.execute(new Runnable() {
			public void run() {
				logger.debug("Redis begin subscribe realData");
				SubscribeListener subscribeListener = subListenerMap.get(session.getId());
				if(null == subscribeListener){
					subscribeListener = new SubscribeListener(session, machineCodeArray);
					subListenerMap.put(session.getId(), subscribeListener);
				}
				RedisManager.subscribe(ConstKey.REDIS_GROUP_NAME, subscribeListener, machineCodeArray);
			}
		});
	}

	class SubscribeListener extends JedisPubSub {
		WebSocketSession session;
		String[] machineCodeArray;

		public SubscribeListener(WebSocketSession session, String[] machineCodeArray) {
			this.session = session;
			this.machineCodeArray = machineCodeArray;
		}

		@Override
		public void onMessage(String channel, String message) {
			logger.debug("Subscribe callback，channel：" + channel + "message:" + message);
			Map<String, Object> bParams = JSON.parseObject(paramMap.get(session.getId()), new TypeReference<Map<String, Object>>() {});
			if (!CommonUtils.isNullOrEmpty(message) && null == bParams.get("markid")) {
				try {
					Object[] oj = getRealData(session);
					if (null != oj[0]) {
						session.sendMessage(new TextMessage(oj[0].toString()));
					}
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

	private Object[] getRealData(WebSocketSession session) {
		try {
			String params = paramMap.get(session.getId());
			Map<String, Object> bParams = JSON.parseObject(params, new TypeReference<Map<String, Object>>() {
			});
			RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
			Page<RealHisCfgDevice> realHisCfgDevicePage = null;
			int pageIndex = 1;
			int pageSize = Integer.MAX_VALUE;
			if (null != bParams.get("pageIndex")) {
				pageIndex = Integer.parseInt(bParams.get("pageIndex").toString());
			}
			if (null != bParams.get("pageSize")) {
				pageSize = Integer.parseInt(bParams.get("pageSize").toString());
			}
			Client client = clientMap.get(session.getId());
			/** 管理者账号 **/
			if (client.userInfo.getUserType() == 1) {
				realHisCfgFilter.data_type = 0;
				realHisCfgFilter.account_id = client.userId;
				realHisCfgDevicePage = realHisCfgApi.getRealHisCfgDevicePage(realHisCfgFilter, bParams, pageIndex,
						pageSize);
			}
			/** 视图账号 **/
			else if (client.userInfo.getUserType() == 2) {
				ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();
				viewAccountRoleFilter.view_id = client.userId;
				viewAccountRoleFilter.data_type = 0;
				realHisCfgDevicePage = realHisCfgApi.getRealHisCfgDevicePage(viewAccountRoleFilter, bParams, pageIndex,
						pageSize);
			}
			List<RealHisCfgDevice> realHisCfgDeviceList = realHisCfgDevicePage.getList();
			JSONObject json = new JSONObject();
			JSONArray arr = new JSONArray();

			if (realHisCfgDeviceList == null || realHisCfgDeviceList.size() < 1) {
				return new Object[] { json.toJSONString(), null };
			}
			Set<String> machineCodeSet = new HashSet<>();
			for (int i = 0; i < realHisCfgDeviceList.size(); i++) {
				RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.get(i);
				String device_machine = realHisCfgDevice.machine_code;
				machineCodeSet.add(device_machine);
				// 通过机器码去redis中获取数据
				RedisPiBoxActData redisPiBoxActData = redisPiBoxApi.getRedisPiBoxActData(device_machine);
				List<PiBoxCom> actTimeDataList = null == redisPiBoxActData ? null
						: redisPiBoxActData.act_time_data_list;

				JSONObject data = new JSONObject();
				data.put("id", realHisCfgDevice.id);
				data.put("monitorName", CommonUtils.isNullOrEmpty(realHisCfgDevice.ref_alais) ? realHisCfgDevice.name
						: realHisCfgDevice.ref_alais);
				data.put("number", 0);
				String stateText = null;
				if (realHisCfgDevice.dstate == Constant.State.STATE_BOX_OFFLINE) {
					stateText = "0";
				} else {
					if (realHisCfgDevice.state < 0) {
						stateText = realHisCfgDevice.state + "";
					} else if (realHisCfgDevice.state != Constant.State.STATE_SYNCED_BOX) {
						stateText = "3";
					}
				}
				if (null != actTimeDataList) {
					outer: for (int j = 0; j < actTimeDataList.size(); j++) {
						PiBoxCom piBoxCom = actTimeDataList.get(j);
						if (Long.parseLong(piBoxCom.com) == realHisCfgDevice.plc_id) {
							List<PiBoxComAddr> addrList = piBoxCom.addr_list;
							for (int k = 0; k < addrList.size(); k++) {
								PiBoxComAddr piBoxComAddr = addrList.get(k);
								if (Long.parseLong(piBoxComAddr.addr_id) == realHisCfgDevice.id) {
									if (CommonUtils.isNullOrEmpty(stateText)) {
										try {
											if (Integer.parseInt(
													piBoxComAddr.state) == Constant.State.STATE_MONITOR_ONLINE) {
												stateText = "1";
											} else if (Integer.parseInt(
													piBoxComAddr.state) == Constant.State.STATE_MONITOR_OFFLINE) {
												stateText = "0";
											} else if (Integer.parseInt(
													piBoxComAddr.state) == Constant.State.STATE_MONITOR_TIMEOUT) {
												stateText = "2";
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									data.put("number", piBoxComAddr.value);
									break outer;
								}
							}
						}
					}
				}
				data.put("state", stateText);
				data.put("groupId", realHisCfgDevice.dir_id);
				arr.add(data);
			}
			json.put("list", arr);
			json.put("totalPage", realHisCfgDevicePage.getTotalPage());
			json.put("currentPage", realHisCfgDevicePage.getCurrentPage());
			logger.debug("Websocket push msg: " + json.toJSONString());
			return new Object[] { json.toJSONString(), machineCodeSet };
		} catch (Exception e) {
			logger.debug("Server error，" + e.getMessage());
			e.printStackTrace();
			return new Object[] { "服务器错误", null };
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
			clientMap.put(session.getId(), client);

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
			// 取消订阅
			SubscribeListener subscribeListener = subListenerMap.get(session.getId());
			if (null != subscribeListener) {
				subscribeListener.unsubscribe();
				subListenerMap.remove(session.getId());
			}
			if (null != clientMQTTs.get(session.getId())) {
				clientMQTTs.get(session.getId()).close();
				clientMQTTs.remove(session.getId());
			}
			paramMap.remove(session.getId());
			logger.debug("Redis取消订阅成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("afterConnectionClosed，" + e.getMessage());
		}
	}
}
