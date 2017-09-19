package com.wecon.box.util;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by caijinw on 2017/9/13.
 */
public class ClientMQTT {


    public static final String HOST = "tcp://192.168.29.186:1883";
    public String topicPort = "topic11";
    private  String clientid ;
    private MqttClient client;
    private MqttConnectOptions options;
    private String userName = "admin";
    private String passWord = "password";
    private ScheduledExecutorService scheduler;
    //回调函数  获取到监听的信息
    private MqttCallback callback;

    public ClientMQTT(String topic,String clientid, MqttCallback callback) {
        this.topicPort = topic;
        this.clientid = clientid;
        this.callback = callback;
    }

    public void start() {
        try {
            // host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(HOST, clientid, new MemoryPersistence());
            // MQTT的连接设置
            options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName(userName);
            // 设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10000);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // 设置回调
            client.setCallback(callback);
            MqttTopic topic = client.getTopic(topicPort);
            //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
            options.setWill(topic, "close".getBytes(), 2, true);

            client.connect(options);
            //订阅消息
            int[] Qos = {1};
            String[] topic1 = {topicPort};
            client.subscribe(topic1, Qos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws MqttException {
        if (client != null)
            client.close();
    }

    public static void main(String[] args) throws MqttException {
//        ClientMQTT client = new ClientMQTT("pibox/stc/2222/", new PushCallback());
//        client.start();
    }
}