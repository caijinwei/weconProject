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
import com.wecon.box.util.ClientMQTT;
//import com.wecon.box.util.ServerMqtt;
import com.wecon.common.redis.RedisManager;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Client;
import redis.clients.jedis.JedisPubSub;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
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
    private ClientMQTT reclient;
    private Map<String, Object> bParams;
    private String addr_id;

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
        this.params = message.getPayload();
        logger.debug("Server received message: " + params);
        if (CommonUtils.isNullOrEmpty(params)) {
            return;
        }
        try {
            bParams = JSON.parseObject(params, new TypeReference<Map<String, Object>>() {
            });
            if ("0".equals(bParams.get("markid").toString())) {
                logger.debug("WebSocket push begin");
                session.sendMessage(new TextMessage(getStringRealData()));
                logger.debug("WebSocket push end");

                // 订阅redis消息
                if (null != machineCodeSet && null == subscribeListener) {
                    subscribeRealData();
                }

            } else if ("1".equals(bParams.get("markid").toString())) {
                String value = bParams.get("value").toString();
                addr_id = bParams.get("addr_id").toString();
                // 订阅消息
                if (!CommonUtils.isNullOrEmpty(addr_id)) {
                    RealHisCfg realHisCfg = realHisCfgApi.getRealHisCfg(Long.parseLong(addr_id));
                    Device device = deviceApi.getDevice(realHisCfg.device_id);
                    String subscribeTopic = "pibox/cts/" + device.machine_code;
                    if (reclient == null || !reclient.isConnected()) {
                        SendvalueCallback sendvalueCallback = new SendvalueCallback();
                        reclient = new ClientMQTT(subscribeTopic, "send" + session.getId(),
                                sendvalueCallback);
                        reclient.start();
                    }
                    if (!reclient.topicPort.contains(subscribeTopic)) {
                        reclient.subscribe(subscribeTopic);
                    }
                }
                // 发送数据
                putMQTTMess(value);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    class SendvalueCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable arg0) {

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void messageArrived(String arg0, MqttMessage arg1) throws Exception {

            String[] idexs = arg0.split("/");
            String reMessage = new String(arg1.getPayload(), "UTF-8").trim();
            logger.debug("mqtt消息：" + reMessage);

            JSONObject json = new JSONObject();
            try {
                JSONObject jsonObject = JSONObject.parseObject(reMessage);
                String machineCode = jsonObject.getString("machine_code");
                // 机器码为空消息直接忽略
                if (CommonUtils.isNullOrEmpty(jsonObject.getString("machine_code"))) {
                    return;
                }
                // 如果消息的机器码和主题中的机器码不匹配直接忽略消息
                if (!idexs[2].equals(machineCode)) {
                    logger.info("主题中的机器码和消息的机器码不匹配！");
                    return;
                }
                // act为空
                if (CommonUtils.isNullOrEmpty(jsonObject.getInteger("act"))) {

                    logger.info("act为空！");
                    return;
                }
                if (1 != jsonObject.getInteger("act")) {
                    logger.info("act不为1！");
                    return;
                }
                if (CommonUtils.isNullOrEmpty(jsonObject.getInteger("feedback_act"))) {
                    logger.info("feedback_act为空！");
                    return;
                }
                if (2000 != jsonObject.getInteger("feedback_act")) {
                    logger.info("feedback_act不为2000！");
                    return;
                }
                // 数据为空
                if (CommonUtils.isNullOrEmpty(jsonObject.getString("data"))) {
                    logger.info("data为空！");
                    return;
                }
                System.out.println("选中的addrid==" + addr_id);
                System.out.println("接收的消息==" + reMessage);
                JSONObject jsonBase = jsonObject.getJSONObject("data");
                if (!CommonUtils.isNullOrEmpty(addr_id) && !CommonUtils.isNullOrEmpty(jsonBase.getInteger("addr_id"))) {
                    if (Integer.parseInt(addr_id) == jsonBase.getInteger("addr_id")) {

                        int upd_state = jsonBase.getInteger("upd_state");
                        if (1 == upd_state) {
                            json.put("resultData", 1);// 反馈成功信息

                        } else {
                            json.put("resultData", 0);
                            json.put("resultError", jsonBase.getString("upd_error"));
                        }
                        sendWSMassage(session, json.toJSONString());
                        addr_id = "-1";
                    }
                }

            } catch (Exception e) {
                String simplename = e.getClass().getSimpleName();
                if (!"JSONException".equals(simplename)) {
                    e.printStackTrace();
                }
            }

        }

        // wbsock发送数据
        public void sendWSMassage(WebSocketSession session, String message) throws IOException {
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            } else {
                logger.debug(session != null);
                logger.debug(session.isOpen());
                logger.debug("----websockect 断开连接----");
            }
        }

    }

    public void putMQTTMess(String value) throws MqttException {

        if (!CommonUtils.isNullOrEmpty(addr_id)) {
            RealHisCfg realHisCfg = realHisCfgApi.getRealHisCfg(Long.parseLong(addr_id));
            if (realHisCfg != null) {
                Device device = deviceApi.getDevice(realHisCfg.device_id);
                if (device != null) {
                    PiBoxComAddr addr1 = new PiBoxComAddr();
                    addr1.addr_id = addr_id;
                    addr1.value = value;
                    List<PiBoxCom> operate_data_list = new ArrayList<PiBoxCom>();
                    PiBoxCom piBoxCom = new PiBoxCom();
                    List<PiBoxComAddr> piBoxComAddrs = new ArrayList<PiBoxComAddr>();
                    piBoxCom.com = String.valueOf(realHisCfg.plc_id);
                    piBoxComAddrs.add(addr1);
                    piBoxCom.addr_list = piBoxComAddrs;
                    operate_data_list.add(piBoxCom);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("act", 2000);
                    jsonObject.put("machine_code", device.machine_code);
                    JSONObject oplistObject = new JSONObject();
                    oplistObject.put("operate_data_list", operate_data_list);
                    jsonObject.put("data", oplistObject);
                    jsonObject.put("feedback", 1);
                    String message = jsonObject.toJSONString();
                    logger.debug("put mqtt : " + message);
                    String topic = "pibox/stc/" + device.machine_code;
                    if (reclient == null || !reclient.isConnected()) {
                        SendvalueCallback sendvalueCallback = new SendvalueCallback();
                        reclient = new ClientMQTT(topic, "send" + session.getId(),
                                sendvalueCallback);
                        reclient.start();
                    }
                    reclient.publish(topic, message);
                }
            }

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
            if (!CommonUtils.isNullOrEmpty(message) && "0".equals(bParams.get("markid").toString())) {
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
        this.session = session;
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
