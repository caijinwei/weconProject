package com.wecon.box.websocket;

import com.wecon.restful.annotation.WebApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zengzhipeng on 2017/8/22.
 */
@RestController
public class ActDataHandler extends AbstractWebSocketHandler {
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
    @WebApi(forceAuth = false, master = true)
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String msg = message.getPayload();
        logger.debug("Server received message: " + msg);
        session.sendMessage(new TextMessage("Server received message: " + msg));
        if (msg.equals("time")) {
            timer = new Timer(true);
            long delay = 0;
            OrderTimeTask orderTimeTask = new OrderTimeTask(session, msg, timer);
            timer.schedule(orderTimeTask, delay, 10000);
        }
    }

    /**
     * 连接成功后
     *
     * @param session
     * @throws Exception
     */
    @Override
    @WebApi(forceAuth = false, master = true)
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.debug("连接成功");
        session.sendMessage(new TextMessage("连接成功"));
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

        @Override
        @WebApi(forceAuth = false, master = true)
        public void run() {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String msgtime = "10秒报时：" + df.format(new Date());
            try {
                if (session == null || !session.isOpen()) {
                    System.out.println("timer is closed");
                    this.timer.cancel();
                    System.out.println("you can not see me");
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
        System.out.println("关闭连接");
    }
}
