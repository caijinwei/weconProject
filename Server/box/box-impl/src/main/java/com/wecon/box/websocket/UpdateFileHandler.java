package com.wecon.box.websocket;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.util.ClientMQTT;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private  int count=0;
    /*
     * @param session
     * @param message
     * @throws Exception
     */

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

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
        count=Integer.parseInt(data.get("msgNum").toString());
        if(count<=0){
            throw new BusinessException(ErrorCodeOption.WebSocket_Update_ClientError.key,ErrorCodeOption.WebSocket_Update_ClientError.value);
        }
        //获取到需要验证的固件驱动信息
        List<JSONObject> verifyObjects=JSONObject.parseArray(String.valueOf(data.get("data")),JSONObject.class);

        /*
        * mqtt监听主题
        * */
        UpdateFileCallback updateFileCallback = new UpdateFileCallback(session,count,verifyObjects);
        client = new ClientMQTT("pibox/cts/" + machine_code , "upb" + session.getId(), updateFileCallback);
        client.start();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.debug("websocket连接成功");
        session.sendMessage(new TextMessage("websocket连接成功"));
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
    WebSocketSession session;
    List<JSONObject> msgs=new ArrayList<JSONObject>();
    List<JSONObject> verifyObjects;
    int count=0;
    public UpdateFileCallback(WebSocketSession session,int count,List<JSONObject> verifyObjects) {
        this.session = session;
        this.count=count;
        this.verifyObjects=verifyObjects;
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

        System.out.println("收到的mqtt信息是    "+JSONObject.parseObject(new String(mqttMessage.getPayload())));
        HashMap<String,String> result=new HashMap<String,String>();
        JSONObject feedBack=JSONObject.parseObject(new String(mqttMessage.getPayload()));
            //如果不是反馈消息  直接忽略
            if(!String.valueOf(feedBack.get("act")).equals("1")){
                return;
            }
            //固件信息升级的反馈
            if(String.valueOf(feedBack.get("feedback")).equals("2007")){
                JSONObject data=JSONObject.parseObject(String.valueOf(feedBack.get("data")));
                String upd_state=data.get("upd_state").toString();
                result.put("firm",upd_state);
                count--;
            }
            //驱动信息升级反馈
            else if(String.valueOf(feedBack.get("feedback")).equals("2008")){
                JSONObject data=JSONObject.parseObject(String.valueOf(feedBack.get("data")));
                String upd_state=data.get("upd_state").toString();
                String com=data.get("com").toString();
                String upd_time=data.get("upd_time").toString();
                //验证更新时间是否一致
                for(JSONObject verify:verifyObjects){
//                    JSONObject innerData=JSONObject.parseObject(String.valueOf(verify.get("data")));
//                    if(String.valueOf(innerData.get("file_type")).equals("2")&&String.valueOf(innerData.get("com")).equals(com)){
//                        if(!String.valueOf(innerData.get("upd_time")).equals(upd_time)){
//                            result.put(com,"-1");
//                        }else{
//                            count--;
//                            break;
//                        }
//                    }
                    if(String.valueOf(verify.get("file_type")).equals("2")&&String.valueOf(verify.get("com")).equals(com)){
                        if(!String.valueOf(verify.get("upd_time")).equals(upd_time)){
                            result.put(com,"-1");
                        }else{
                            count--;
                            break;
                        }
                    }
                }
            }else{
                return;
            }
        //已经收到全部的反馈
            if(count<=0) {
                JSONObject data=new JSONObject();
                data.put("data",result);
                sendWSMassage(session, data.toString());
                msgs.add(JSONObject.parseObject(new String(mqttMessage.getPayload())));
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

