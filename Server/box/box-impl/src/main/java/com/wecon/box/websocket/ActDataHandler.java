package com.wecon.box.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
        //推送消息给移动端

        logger.debug("WebSocket push begin");
        session.sendMessage(new TextMessage(getStringRealData()));
        logger.debug("WebSocket push end");

        //订阅redis消息
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
            if (client == null) {
                client = AppContext.getSession().client;
            }
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

            /** 管理者账号 **/
            if (client.userInfo.getUserType() == 1) {
                realHisCfgFilter.data_type = 0;
                realHisCfgFilter.account_id = client.userId;
                realHisCfgDevicePage = realHisCfgApi.getRealHisCfgDevicePage(realHisCfgFilter, bParams, pageIndex, pageSize);
            }
            /** 视图账号 **/
            else if (client.userInfo.getUserType() == 2) {
                ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();
                viewAccountRoleFilter.view_id = client.userId;
                viewAccountRoleFilter.data_type = 0;
                realHisCfgDevicePage = realHisCfgApi.getRealHisCfgDevicePage(viewAccountRoleFilter, bParams, pageIndex, pageSize);
            }
            List<RealHisCfgDevice> realHisCfgDeviceList = realHisCfgDevicePage.getList();
            JSONObject json = new JSONObject();
            JSONArray arr = new JSONArray();

            if (realHisCfgDeviceList == null || realHisCfgDeviceList.size() < 1) {
                return null;
            }
            machineCodeSet = new HashSet<>();
            for (int i = 0; i < realHisCfgDeviceList.size(); i++) {
                RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.get(i);
                String device_machine = realHisCfgDevice.machine_code;
                machineCodeSet.add(device_machine);
                // 通过机器码去redis中获取数据
                RedisPiBoxActData redisPiBoxActData = redisPiBoxApi.getRedisPiBoxActData(device_machine);
                List<PiBoxCom> actTimeDataList = null == redisPiBoxActData ? null : redisPiBoxActData.act_time_data_list;

                JSONObject data = new JSONObject();
                data.put("id", realHisCfgDevice.id);
                data.put("state", realHisCfgDevice.state);
                data.put("monitorName", realHisCfgDevice.name);
                data.put("number", 0);
                if (null != actTimeDataList) {
                    for (int j = 0; j < actTimeDataList.size(); j++) {
                        PiBoxCom piBoxCom = actTimeDataList.get(j);
                        if (Long.parseLong(piBoxCom.com) == realHisCfgDevice.plc_id) {
                            List<PiBoxComAddr> addrList = piBoxCom.addr_list;
                            for (int k = 0; k < addrList.size(); k++) {
                                PiBoxComAddr piBoxComAddr = addrList.get(k);
                                if (Long.parseLong(piBoxComAddr.addr_id) == realHisCfgDevice.id) {
                                    data.put("state", piBoxComAddr.state);
                                    data.put("number", piBoxComAddr.value);
                                }
                            }
                        }
                    }
                }
                arr.add(data);
            }
            json.put("list", arr);
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
        //取消订阅
        subscribeListener.unsubscribe();
        logger.debug("Redis取消订阅成功");
    }
}
