package com.wecon.box.console.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.context.ApplicationContext;

/**
 * Created by zengzhipeng on 2017/9/2.
 */
public class BoxNotifyTask extends Thread {
    public static MqttClient mqttClient;

    private static final Logger logger = LogManager.getLogger(BoxNotifyTask.class);
    private static int sleepTime = 1000 * 30;

    public void run() {
        logger.info("BoxNotifyTask run start");

        while (true) {
            try {
                if (mqttClient != null && mqttClient.isConnected()) {
                    logger.info("mqtt connection is normal !");
//                    notifyHandle();
                    sleep(sleepTime);
                    continue;
                }

//                connect();
//                subscribe();
//                notifyHandle();
                sleep(sleepTime);

            } catch (Exception e) {
                logger.error(e);
            }
        }

    }

    private void sleep(int sleepTime) throws InterruptedException {
        logger.info("sleep:" + sleepTime);
        int slept = 0;
        while (slept <= sleepTime) {
            Thread.sleep(200);
            slept += 200;
        }
    }
}
