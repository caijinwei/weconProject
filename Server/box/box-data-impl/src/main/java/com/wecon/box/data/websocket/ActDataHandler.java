package com.wecon.box.data.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.*;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.constant.Constant;
import com.wecon.box.data.util.SendvalueCallback;
import com.wecon.box.entity.*;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.filter.DevBindUserFilter;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.box.util.ClientMQTT;
import com.wecon.box.util.SendValue;
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
	private DevBindUserApi devBindUserApi;
	@Autowired
	private ViewAccountRoleApi viewAccountRoleApi;
	@Autowired
	private SendValue sendValue;

	private Map<String, Client> clientMap = new HashMap<>();

	private Map<String, SubscribeListener> subListenerMap = new HashMap<>();

	private Map<String, ClientMQTT> clientMQTTs = new HashMap<String, ClientMQTT>();

	private Map<String, String> paramMap = new HashMap<>();

	private Map<String, List<String>> machineCodeCache = new HashMap<>();

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
			Client client = clientMap.get(session.getId());
			Map<String, Object> bParams = JSON.parseObject(params, new TypeReference<Map<String, Object>>() {});
			String markId = null != bParams.get("markId") ? bParams.get("markId").toString() : null;
			if(markId != null){
				if("2".equals(markId)){
					paramMap.put(session.getId(), params);
					List<String> machineCodeList = null;
					/** 管理者账号 **/
					if (client.userInfo.getUserType() == 1) {
						machineCodeList = devBindUserApi.getMachineCodesByAccountId(client.userId);
					}
					/** 视图账号 **/
					else if (client.userInfo.getUserType() == 2) {
						machineCodeList = viewAccountRoleApi.getMachineCodesByViewId(client.userId);
					}
					if(null != machineCodeList && machineCodeList.size() > 0){
						Set<String> machineCodeSet = new HashSet<>();
						for(String machineCode : machineCodeList){
							machineCodeSet.add(machineCode+"realdata");
						}
						subscribeRealData(session, machineCodeSet);
					}
				}else if ("1".equals(markId)) {
					String value = bParams.get("value").toString();
					String addr_id = bParams.get("monitorId").toString();

					// 订阅消息
					if (!CommonUtils.isNullOrEmpty(addr_id)) {
						if (client.userInfo.getUserType() == 2) {
							int roleType = viewAccountRoleApi.getViewRealRoleTypeByCfgId(client.userId, Long.parseLong(addr_id));
							if(3 != roleType){
								return;
							}
						}
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
				}else if("0".equals(markId)){
					paramMap.put(session.getId(), params);
					// 推送消息给移动端
					logger.debug("WebSocket push begin");
					Object[] oj = getRealData(session);
					session.sendMessage(new TextMessage(oj[0].toString()));
					logger.debug("WebSocket push end");

					// 订阅redis消息，有新的机器码则进行订阅
					Set<String> machineCodeSet = (Set<String>) oj[1];
					if (machineCodeSet.size() > 0) {
						subscribeRealData(session, machineCodeSet);
					}
				} else if("-1".equals(markId)){
					session.sendMessage(new TextMessage("1"));
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
			if (!CommonUtils.isNullOrEmpty(message)) {
				if("0".equals(bParams.get("markId").toString())){
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
				}else if("2".equals(bParams.get("markId").toString())){
					try {
						RedisPiBoxActData redisModel = JSON.parseObject(message, RedisPiBoxActData.class);
						List<PiBoxCom> act_time_data_list = redisModel.act_time_data_list;
						JSONObject list = new JSONObject();
						JSONArray arr = new JSONArray();
						if(null != act_time_data_list){
							List<Long> cfgIds = new ArrayList<>();
							for(PiBoxCom piBoxCom : act_time_data_list){
								List<PiBoxComAddr> addr_list = piBoxCom.addr_list;
								if(null != addr_list){
									for(PiBoxComAddr piBoxComAddr : addr_list){
										cfgIds.add(Long.parseLong(piBoxComAddr.addr_id));
									}
								}
							}
							List<RealHisCfg> realCfgList = realHisCfgApi.getRealCfgByIds(cfgIds);
							int i = 0;
							for(PiBoxCom piBoxCom : act_time_data_list){
								List<PiBoxComAddr> addr_list = piBoxCom.addr_list;
								if(null != addr_list){
									for(PiBoxComAddr piBoxComAddr : addr_list){
										JSONObject data = new JSONObject();
										data.put("monitorId", piBoxComAddr.addr_id);
										data.put("state", piBoxComAddr.state);
										data.put("value", piBoxComAddr.value);
										data.put("addrType", realCfgList.get(i).addr_type);
										data.put("dataId", realCfgList.get(i).data_id);
										data.put("digitCount", realCfgList.get(i++).digit_count);
										arr.add(data);
									}
								}
							}
						}
						list.put("list", arr);
						JSONObject respone = new JSONObject();
						respone.put("msg", "实时数据");
						respone.put("markId", 2);
						respone.put("result", list);
						session.sendMessage(new TextMessage(respone.toJSONString()));
					}catch (Exception e){
						e.printStackTrace();
					}
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
				pageIndex = 0 == pageIndex ? 1 : pageIndex;
			}
			if (null != bParams.get("pageSize")) {
				pageSize = Integer.parseInt(bParams.get("pageSize").toString());
				pageSize = 0 == pageSize ? 10 : pageSize;
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
				return new Object[] {json.toJSONString(), null };
			}
			Set<String> machineCodeSet = new HashSet<>();
			List<String> machineCodeList = machineCodeCache.get(session.getId());
			if(null == machineCodeList){
				machineCodeList = new ArrayList<>();
				machineCodeCache.put(session.getId(), machineCodeList);
			}
			for (int i = 0; i < realHisCfgDeviceList.size(); i++) {
				RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.get(i);
				String device_machine = realHisCfgDevice.machine_code;
				if(!CommonUtils.isNullOrEmpty(device_machine) && !machineCodeList.contains(device_machine)){
					machineCodeSet.add(device_machine);
					machineCodeList.add(device_machine);
				}

				// 通过机器码去redis中获取数据
				RedisPiBoxActData redisPiBoxActData = redisPiBoxApi.getRedisPiBoxActData(device_machine);
				List<PiBoxCom> actTimeDataList = null == redisPiBoxActData ? null
						: redisPiBoxActData.act_time_data_list;

				JSONObject data = new JSONObject();
				data.put("monitorId", realHisCfgDevice.id);
				data.put("monitorName", CommonUtils.isNullOrEmpty(realHisCfgDevice.ref_alais) ? realHisCfgDevice.name
						: realHisCfgDevice.ref_alais);
				data.put("value", 0);
				data.put("dataId", realHisCfgDevice.data_id);
				data.put("digitCount", realHisCfgDevice.digit_count);
				int state = 0;
				String stateText = null;
				//盒子在线
				if (realHisCfgDevice.dstate == Constant.State.STATE_BOX_ONLINE) {
					//未同步给盒子
					if(realHisCfgDevice.state != Constant.State.STATE_SYNCED_BOX){
						if (realHisCfgDevice.state == Constant.State.STATE_NEW_CONFIG) {
							stateText = "条目未下发";
						} else if(realHisCfgDevice.state == Constant.State.STATE_UPDATE_CONFIG){
							stateText = "条目未下发";
						} else if(realHisCfgDevice.state == Constant.State.STATE_COMPILE_FAILED){
							stateText = "编译失败";
						} else if(realHisCfgDevice.state == Constant.State.STATE_ADDR_TRANS_FAILED){
							stateText = "地址转义失败";
						}  else if(realHisCfgDevice.state == Constant.State.STATE_ADDR_BIND_FAILED){
							stateText = "地址绑定失败";
						} else {
							stateText = "未知状态";
						}
						state = realHisCfgDevice.state;
					}
				} else {
					stateText = "离线";
					state = Constant.State.STATE_MONITOR_OFFLINE;
				}
				if (null != actTimeDataList) {
					outer: for (int j = 0; j < actTimeDataList.size(); j++) {
						PiBoxCom piBoxCom = actTimeDataList.get(j);
						if (Long.parseLong(piBoxCom.com) == realHisCfgDevice.plc_id) {
							List<PiBoxComAddr> addrList = piBoxCom.addr_list;
							for (int k = 0; k < addrList.size(); k++) {
								PiBoxComAddr piBoxComAddr = addrList.get(k);
								if (Long.parseLong(piBoxComAddr.addr_id) == realHisCfgDevice.id) {
									if (realHisCfgDevice.dstate == Constant.State.STATE_BOX_ONLINE
											&& realHisCfgDevice.state == Constant.State.STATE_SYNCED_BOX) {
										try {
											int rState = Integer.parseInt(piBoxComAddr.state);
											if (rState == Constant.State.STATE_MONITOR_ONLINE) {
												stateText = "在线";
											} else if (rState == Constant.State.STATE_MONITOR_OFFLINE) {
												stateText = "离线";
											} else if (rState == Constant.State.STATE_MONITOR_TIMEOUT) {
												stateText = "超时";
											} else {
												stateText = "超时";
											}
											state = rState;
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									data.put("value", piBoxComAddr.value);
									break outer;
								}
							}
						}
					}
				}else{
					if(null == stateText){
						stateText = "超时";
					}
				}
				data.put("state", state);
				data.put("stateText", stateText);
				data.put("com", realHisCfgDevice.plc_id);
				data.put("groupId", realHisCfgDevice.dir_id);
				arr.add(data);
			}
			json.put("list", arr);
			json.put("totalPage", realHisCfgDevicePage.getTotalPage());
			json.put("currentPage", realHisCfgDevicePage.getCurrentPage());
			JSONObject result = new JSONObject();
			result.put("msg", "实时数据");
			result.put("markId", 0);
			result.put("result", json);
			logger.debug("Websocket push msg: " + result.toJSONString());
			return new Object[] {result.toJSONString(), machineCodeSet};
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
			machineCodeCache.get(session.getId()).clear();
			logger.debug("Redis取消订阅成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("afterConnectionClosed，" + e.getMessage());
		}
	}
}
