/**
 * @功能说明:  监控任务
 * @创建人  : lph
 * @创建时间:  2017年07月26日 
 * @修改人  : 
 * @修改时间: 
 * @修改描述: 
 * @Copyright (c)2017 福州富昌维控电子科技有限公司-版权所有
 *
 */
package com.wecon.box.console.task;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.wecon.box.console.config.ConnectOptions;
import com.wecon.box.console.util.MqttConfigContext;

public class MonitorTaskJob implements Job {
	private static MqttClient client;

	/**
	 * 实时监控是否连上代理服务器
	 * 
	 * @param job
	 */
	public void execute(JobExecutionContext job) throws JobExecutionException {
		try {
			MqttConnectOptions options = ConnectOptions.getConnectOptions(MqttConfigContext.mqttConfig.getUsername(),
					MqttConfigContext.mqttConfig.getPassword());
			if (client != null && client.isConnected()) {
				System.out.println("连接MQTT代理服务器正常");
				return;
			}
			client = new MqttClient(MqttConfigContext.mqttConfig.getHost(), "server", new MemoryPersistence());
			client.connect(options);
			// 订阅盒子的所有发送主题
			client.subscribe("po/cts/#");

			client.setCallback(new MqttCallback() {

				public void messageArrived(String topic, MqttMessage message) throws Exception {

					System.out.println("topic=" + topic + " " + "message=" + new String(message.getPayload()).trim());
				}

				public void deliveryComplete(IMqttDeliveryToken token) {

				}

				public void connectionLost(Throwable cause) {
				}
			});

		} catch (MqttException e) {
			e.printStackTrace();
		}

	}

}
