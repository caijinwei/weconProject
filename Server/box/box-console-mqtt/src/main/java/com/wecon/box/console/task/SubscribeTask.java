package com.wecon.box.console.task;

import com.wecon.box.console.util.MqttConfigContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 服务端监听消息
 * Created by zengzhipeng on 2017/7/25.
 */
public class SubscribeTask extends Thread{
    private static final Logger logger = LogManager.getLogger(SubscribeTask.class);
    private static ApplicationContext applicationContext;

    public void run() {
        applicationContext = new ClassPathXmlApplicationContext("spring.xml");

        System.out.println(MqttConfigContext.mqttConfig.getHost());
        System.out.println(MqttConfigContext.mqttConfig.getUsername());
    }
}
