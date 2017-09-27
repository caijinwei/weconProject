package com.wecon.box.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.entity.PlcInfoDetail;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.util.ClientMQTT;
import com.wecon.box.util.SpringContextHolder;
import com.wecon.restful.core.BusinessException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * Created by caijinw on 2017/9/21.
 */
@Component
public class UpdateFileHandler extends AbstractWebSocketHandler {

    String machine_code;
    ClientMQTT client;
    private static final Logger logger = LogManager.getLogger(DebugInfoHandler.class.getName());
    private Timer timer;
    /*
     * @param session
     * @param message
     * @throws Exception
     */

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        int count=0;

        /*
        *    {
        *       “machine_code”:"机器码"，
        *        “msgNum”:“发送的条数”
        *        “data”:{
        *                     “file_type”:         1   固件文件
        *                       upd_time :                最后更新时间
        *                  }
        *    }
        *    {
        *       “machine_code”:"机器码"，
        *        “msgNum”:“发送的条数”
        *        “data”:{
        *                     “file_type”:            2  驱动文件
        *                      "com"                      comId
        *                      “upd_time”              最后更新时间
        *                 }
        *    }
        *
        * */
        String messages = message.getPayload();
        JSONObject data = JSONObject.parseObject(messages);
        String machine_code = data.get("machine_code").toString();
        count = Integer.parseInt(data.get("msgNum").toString());
        if (count <= 0) {
            throw new BusinessException(ErrorCodeOption.WebSocket_Update_ClientError.key, ErrorCodeOption.WebSocket_Update_ClientError.value);
        }
        //获取到需要验证的固件驱动信息
        List<JSONObject> verifyObjects = JSONObject.parseArray(String.valueOf(data.get("data")), JSONObject.class);

        /*
        * mqtt监听主题
        * */
        UpdateFileCallback updateFileCallback = new UpdateFileCallback(session, count, verifyObjects);
        client = new ClientMQTT("pibox/cts/" + machine_code, "upb" + session.getId(), updateFileCallback);
        client.start();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.debug("websocket连接成功");
//        session.sendMessage(new TextMessage("websocket连接成功"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.debug("服务的连接关闭");
        super.afterConnectionClosed(session, status);
    }
}

/*
*
* */

class UpdateFileCallback implements MqttCallback {
    private static final Logger logger = LogManager.getLogger(UpdateFileHandler.class.getName());
    WebSocketSession session;
    List<JSONObject> verifyObjects;
    HashMap<String, String> result=new HashMap<String,String>();
    int count = 0;

    public UpdateFileCallback(WebSocketSession session, int count, List<JSONObject> verifyObjects) {
        this.session = session;
        this.count = count;
        this.verifyObjects = verifyObjects;
    }

    @Override
    public void connectionLost(Throwable throwable) {
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {

        //处理不是JOSN格式的消息，直接抛弃
        try {
            JSONObject getPayMsg=JSONObject.parseObject(new String(mqttMessage.getPayload()));
            JSONObject.parseObject(String.valueOf(getPayMsg.get("data")));
        }catch (Exception e){
            return;
        }
        System.out.println("收到的mqtt信息是    " + JSONObject.parseObject(new String(mqttMessage.getPayload())));
        JSONObject feedBack = JSONObject.parseObject(new String(mqttMessage.getPayload()));
        //如果不是反馈消息  直接忽略
        if (!String.valueOf(feedBack.get("act")).equals("1")) {
            return;
        }
        //固件信息升级的反馈
        if (String.valueOf(feedBack.get("feedback_act")).equals("2007")) {
            for (JSONObject verify : verifyObjects) {
                //将后台验证信息删除   （计数）
                if(verify.get("file_type").equals("1")){
                    verifyObjects.remove(verify);
                    JSONObject data = JSONObject.parseObject(String.valueOf(feedBack.get("data")));
                    String upd_state = data.get("upd_state").toString();
                    result.put("firm", upd_state);
                    count--;
                    break;
                }
            }

        }
        //驱动信息升级反馈
        else if (String.valueOf(feedBack.get("feedback_act")).equals("2008")) {
            JSONObject data = JSONObject.parseObject(String.valueOf(feedBack.get("data")));
            String upd_state = data.get("upd_state").toString();
            String com = data.get("com").toString();
            String upd_time = data.get("upd_time").toString();
            //验证更新时间是否一致
            for (JSONObject verify : verifyObjects) {
                System.out.println(verify.get("com"));
                if (String.valueOf(verify.get("file_type")).equals("2") && String.valueOf(verify.get("com")).equals(com)) {
                    verifyObjects.remove(verify);
                    if (!String.valueOf(verify.get("upd_time")).equals(upd_time)) {
                        String upd_date=data.get("upd_state")+"";
                        result.put(com,upd_date);
                        //反馈状态  成功  写入数据库
                        if(upd_date.equals("1")) {
                            //数据库 更新 plc表  file_md5
                            PlcInfoApi plcInfoApi = SpringContextHolder.getBean(PlcInfoApi.class);
                            long plc_id = Long.parseLong(com);
                            PlcInfoDetail plcInfo = plcInfoApi.getPlcInfoDetail(plc_id);
                            plcInfo.file_md5 = String.valueOf(data.get("file_md5"));
                            plcInfoApi.savePlcInfoDetail(plcInfo);
                        }
                        count--;
                        break;
                    } else {
                        result.put(com,upd_state);
                        count--;
                        break;
                    }
                }
            }
        } else {
            return;
        }
        //已经收到全部的反馈
        if (count <= 0) {
            JSONObject data =  JSONObject.parseObject(JSON.toJSONString(result));
            try {
                sendWSMassage(session, data.toString());
            } catch (IOException e) {
                logger.debug(""+e);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    //wbsock发送数据
    public void sendWSMassage(WebSocketSession session, String message) throws IOException {
        session.sendMessage(new TextMessage(message));
    }

}

