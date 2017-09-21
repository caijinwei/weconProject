package com.wecon.box.websocket;

import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.util.ClientMQTT;
import com.wecon.box.util.DebugInfoCallback;
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
        machine_code = message.getPayload();

        String messageNum=message.getPayload();
        if(message==null){
            throw new BusinessException(ErrorCodeOption.WebSocket_Update_ClientError.key,ErrorCodeOption.WebSocket_Update_ClientError.value);
        }
        Integer msgNum=Integer.parseInt(messageNum);
        if(msgNum<=0){
            throw new BusinessException(ErrorCodeOption.WebSocket_Update_ClientError.key,ErrorCodeOption.WebSocket_Update_ClientError.value);
        }
        /*
        * mqtt监听主题
        * */
        DebugInfoCallback debugInfoCallback = new DebugInfoCallback(session);

        client = new ClientMQTT("pibox/cts/" + machine_code , "upb" + session.getId(), debugInfoCallback);
        client.start();
    }

    /**
     * 连接成功后
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.debug("连接成功");
        session.sendMessage(new TextMessage("连接成功"));
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
class UpdateFileCallback implements MqttCallback {
    WebSocketSession session;

    public UpdateFileCallback(WebSocketSession session){
        this.session=session;
    }
    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        new String(mqttMessage.getPayload());

        sendWSMassage(session,new String(mqttMessage.getPayload(),"UTF-8"));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
    //wbsock发送数据
    public void sendWSMassage(WebSocketSession session,String message) throws IOException {
        session.sendMessage(new TextMessage(message));
    }
}

