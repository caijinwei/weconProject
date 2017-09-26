package com.wecon.box.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zengzhipeng on 2017/9/25.
 */
public class WsTestHandler extends AbstractWebSocketHandler {

    private static final Logger logger = LogManager.getLogger(WsTestHandler.class.getName());

    private String params;
    private Map<String, Object> bParams;

    /**
     * 收到消息
     *
     * @param session
     * @param message
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        logger.debug("WsTestHandler handleTextMessage");
        params = message.getPayload();
        try {
            session.sendMessage(new TextMessage("server get msg:" + params));

            bParams = JSON.parseObject(params, new TypeReference<Map<String, Object>>() {
            });
//            Long l = Long.valueOf("a");


        } catch (Exception e) {
            logger.error(e);
//            e.printStackTrace();
        }
    }

    /**
     * 连接成功后
     *
     * @param session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.debug("WsTestHandler afterConnectionEstablished");
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
        logger.debug("WsTestHandler afterConnectionClosed");
    }


}
