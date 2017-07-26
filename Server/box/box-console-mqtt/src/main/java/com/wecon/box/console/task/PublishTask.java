package com.wecon.box.console.task;

import com.wecon.box.console.util.MqttConfigContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * 服务端发布消息
 * Created by zengzhipeng on 2017/7/25.
 */
public class PublishTask extends Thread{
    private static final Logger logger = LogManager.getLogger(PublishTask.class);
    private static ApplicationContext applicationContext;

    public void run() {

    }
}
