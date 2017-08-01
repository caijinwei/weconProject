package com.wecon.box.console.task;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.wecon.box.console.config.ConnectOptions;
import com.wecon.box.console.util.MqttConfigContext;

public class GetPublishClient {

	private MqttClient client;

	/**
	 * 服务器推送消息获取客户端连接
	 */
	public MqttClient conMqtt() {
		try {
			MqttConnectOptions options = ConnectOptions.getConnectOptions(MqttConfigContext.mqttConfig.getUsername(),
					MqttConfigContext.mqttConfig.getPassword());
			client = new MqttClient(MqttConfigContext.mqttConfig.getHost(), "WECON_PUBLISH", new MemoryPersistence());
			client.connect(options);

		} catch (MqttSecurityException e) {
			e.printStackTrace();
			return null;
		} catch (MqttException e) {
			e.printStackTrace();
			return null;
		}
		return client;

	}

	/**
	 * 关闭连接代理服务器
	 */
	public void disConMqtt() {
		if (client != null && client.isConnected()) {
			try {
				client.disconnect();
			} catch (MqttException e) {
				e.printStackTrace();
			}

		}

	}

}
