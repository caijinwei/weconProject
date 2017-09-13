package com.wecon.box.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.entity.*;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zengzhipeng on 2017/8/22.
 */
public class ActDataHandler extends AbstractWebSocketHandler {
    @Autowired
    private RedisPiBoxApi redisPiBoxApi;
    @Autowired
    private RealHisCfgApi realHisCfgApi;
    private static final Logger logger = LogManager.getLogger(ActDataHandler.class.getName());
    private Timer timer;

    /**
     * 收到消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String msg = message.getPayload();
        logger.debug("Server received message: " + msg);
        if(CommonUtils.isNullOrEmpty(msg)){
            return;
        }
        try {
            Map<String, Object> bParams = JSON.parseObject(msg, new TypeReference<Map<String, Object>>(){});
            Client client = AppContext.getSession().client;
            RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
            Page<RealHisCfgDevice> realHisCfgDevicePage = null;
            int pageIndex = 1;
            int pageSize = Integer.MAX_VALUE;
            if(null != bParams.get("pageIndex")){
                pageIndex = Integer.parseInt(bParams.get("pageIndex").toString());
            }
            if(null != bParams.get("pageSize")){
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
                session.sendMessage(new TextMessage(json.toJSONString()));
                return;
            }
            for (int i = 0; i < realHisCfgDeviceList.size(); i++) {
                RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.get(i);
                String device_machine = realHisCfgDevice.machine_code;
                // 通过机器码去redis中获取数据
                RedisPiBoxActData redisPiBoxActData = redisPiBoxApi.getRedisPiBoxActData(device_machine);
                List<PiBoxCom> actTimeDataList = null == redisPiBoxActData ? null : redisPiBoxActData.act_time_data_list;

                JSONObject data = new JSONObject();
                data.put("id", realHisCfgDevice.id);
                data.put("state", realHisCfgDevice.state);
                data.put("monitorName", realHisCfgDevice.name);
                data.put("number", 0);
                if(null != actTimeDataList){
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
            session.sendMessage(new TextMessage(json.toJSONString()));
        }catch (Exception e){
            session.sendMessage(new TextMessage("服务器错误"));
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
        //session.sendMessage(new TextMessage("连接成功"));
    }

    class OrderTimeTask extends TimerTask {
        private WebSocketSession session;
        private String msg;
        private Timer timer;

        public OrderTimeTask(WebSocketSession session, String msg, Timer timer) {
            this.session = session;
            this.msg = msg;
            this.timer = timer;
        }

        public void run() {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String msgtime = "3秒报时：" + df.format(new Date());
            try {
                if (session == null || !session.isOpen()) {
                    this.timer.cancel();
                }
                System.out.println(msgtime);
                session.sendMessage(new TextMessage(msgtime));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        if (this.timer != null) {
            this.timer.cancel();
            logger.debug("timer cancel");
        }
        logger.debug("关闭连接");
    }
}
