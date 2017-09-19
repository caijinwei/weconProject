package com.wecon.box.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.*;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
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
import java.io.IOException;
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

	private String params;

	private Set<String> machineCodeSet;

	private WebSocketSession session;

	private SubscribeListener subscribeListener;

	private Client client;

	private static final Logger logger = LogManager.getLogger(ActDataHandler.class.getName());

	/**
	 * 收到消息
	 *
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		this.session = session;
		this.params = message.getPayload();
		logger.debug("Server received message: " + params);
		if (CommonUtils.isNullOrEmpty(params)) {
			return;
		}
		// 推送消息给网页端

		logger.debug("WebSocket push begin");
		session.sendMessage(new TextMessage(getStringRealData()));
		logger.debug("WebSocket push end");

		// 订阅redis消息
		if (null != machineCodeSet && null == subscribeListener) {
			subscribeRealData();
		}
	}

	/**
	 * 订阅redis消息
	 */
	private void subscribeRealData() {
		subscribeListener = new SubscribeListener();
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
		@Override
		public void onMessage(String channel, String message) {
			logger.debug("Subscribe callback，channel：" + channel + "message:" + message);
			if (!CommonUtils.isNullOrEmpty(message)) {
				try {
					session.sendMessage(new TextMessage(getStringRealData()));
				} catch (IOException e) {
					e.printStackTrace();
					logger.debug("Subscribe callback error，" + e.getMessage());
				}
			}
		}
	}

	private String getStringRealData() {
		try {
			Map<String, Object> bParams = JSON.parseObject(params, new TypeReference<Map<String, Object>>() {
			});
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
				realHisCfgFilter.state = -1;
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
				viewAccountRoleFilter.state = -1;
				if (!CommonUtils.isNullOrEmpty(bParams.get("acc_dir_id"))) {
					viewAccountRoleFilter.dirId = Long.parseLong(bParams.get("acc_dir_id").toString());
				}
				realHisCfgDeviceList = realHisCfgApi.getRealHisCfgList(viewAccountRoleFilter,
						Integer.parseInt(bParams.get("pageIndex").toString()),
						Integer.parseInt(bParams.get("pageSize").toString()));
			}

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
					machineCodeSet.add(device_machine);
					Device device = deviceApi.getDevice(device_machine);
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
										if (device.state == 0) {
											realHisCfgDevice.re_state = "0";
										} else {
											realHisCfgDevice.re_state = piBoxComAddr.state;
										}

										realHisCfgDevice.re_value = piBoxComAddr.value;

									}

								}

							}

						}
					}
				}
			}
			json.put("piBoxActDateMode", realHisCfgDeviceList);
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
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.debug("连接成功");
		if (client == null) {
			client = AppContext.getSession().client;
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
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		logger.debug("关闭连接");
		// 取消订阅
		subscribeListener.unsubscribe();
		subscribeListener = null;
		machineCodeSet = null;
		logger.debug("Redis取消订阅成功");
	}
}
