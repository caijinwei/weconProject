package com.wecon.box.console.util;

import com.wecon.box.console.config.MqttConfig;

/**
 * Created by zengzhipeng on 2017/7/25.
 */
public class MqttConfigContext {
    public static MqttConfig mqttConfig;

    public void setMqttConfig(MqttConfig mqttConfig) {
        MqttConfigContext.mqttConfig = mqttConfig;
    }
}
