/**
 * @功能说明: 监控任务
 * @创建人 : lph
 * @创建时间: 2017年07月26日
 * @修改人 :
 * @修改时间:
 * @修改描述:
 * @Copyright (c)2017 福州富昌维控电子科技有限公司-版权所有
 */
package com.wecon.box.console.task;

import java.util.List;
import java.util.ListIterator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.entity.PiBoxComAddr;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.console.config.ConnectOptions;
import com.wecon.box.console.util.MqttConfigContext;
import com.wecon.box.entity.PiBoxCom;
import com.wecon.box.entity.RedisPiBoxActData;

//import net.sf.json.JSONObject;

public class MonitorTaskJob implements Job {
    private static MqttClient client;
    private final int BASE_DATA = 1000;
    private final int REAL_DATA = 1001;
    private final int HISTORY_DATA = 1002;
    private final int ALARM_DATA = 1003;

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
//                System.out.println("连接MQTT代理服务器正常");
                return;
            }
            System.out.println("to connect mqtt");
            client = new MqttClient(MqttConfigContext.mqttConfig.getHost(), "Wecon", new MemoryPersistence());
            client.connect(options);


            // 订阅盒子的所有发送主题
            client.subscribe("pibox/cts/1870011603240285dd4dc845fb4");//("pibox/cts/#");
            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    manageData(topic, message);
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

    /**
     * 主题和消息处理
     */
    public void manageData(String topic, MqttMessage message) {
        String[] idexs = topic.split("/");
        // 如果未获取到主题直接返回
        if (null == idexs || idexs.length < 1) {
            return;
        }
        String boxMsg = new String(message.getPayload()).trim();
        try {
            JSONObject jsonObject = JSONObject.parseObject(boxMsg);
            System.out.println("jsonObject=" + jsonObject);
            Integer act = jsonObject.getInteger("act");
            switch (act) {
                // 基础数据
                case BASE_DATA:

                    break;
                // 实时数据
                case REAL_DATA:
                    System.out.println("实时数据接收");
                    ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");
                    RedisPiBoxApi redisPiBoxApi = applicationContext.getBean(RedisPiBoxApi.class);
                    if (!jsonObject.getString("machine_code").isEmpty() && !jsonObject.getString("data").isEmpty()) {
                        //获取redis实时数据
                        RedisPiBoxActData redisModel = redisPiBoxApi.getRedisPiBoxActData(jsonObject.getString("machine_code"));
                        RedisPiBoxActData newModel = JSON.parseObject(jsonObject.getString("data"), new TypeReference<RedisPiBoxActData>() {
                        });
                        if (redisModel != null && redisModel.act_time_data_list != null && newModel != null && newModel.act_time_data_list != null) {
                            redisModel.time = newModel.time;
                            for (PiBoxCom newIt : newModel.act_time_data_list) {
                                boolean comIsExit = false;
                                for (int i = 0; i < redisModel.act_time_data_list.size(); i++) {
                                    PiBoxCom redisIt = redisModel.act_time_data_list.get(i);

                                    if (newIt.com.equals(redisIt.com)) {
                                        comIsExit = true;
                                        //<editor-fold desc="检查地址列表">
                                        for (PiBoxComAddr newAddrIt : newIt.addr_list) {
                                            boolean addrIsExit = false;
                                            for (int j = 0; j < redisIt.addr_list.size(); j++) {
                                                PiBoxComAddr redisAddrIt = redisIt.addr_list.get(j);
                                                if (newAddrIt.addr_id.equals(redisAddrIt.addr_id)) {
                                                    addrIsExit = true;
                                                    redisAddrIt.addr = newAddrIt.addr;
                                                    redisAddrIt.value = newAddrIt.value;
                                                    break;
                                                }
                                            }
                                            if (!addrIsExit) {
                                                redisIt.addr_list.add(newAddrIt);
                                            }
                                        }
                                        //</editor-fold>
                                        break;
                                    }
                                }
                                if (!comIsExit) {
                                    redisModel.act_time_data_list.add(newIt);
                                }
                            }
                            redisPiBoxApi.saveRedisPiBoxActData(redisModel);
                            System.out.println("redis modify success");
                        } else if (newModel != null && newModel.act_time_data_list != null) {
                            newModel.machine_code = jsonObject.getString("machine_code");
                            redisPiBoxApi.saveRedisPiBoxActData(newModel);
                            System.out.println("redis add success");
                        }

					/*RedisPiBoxActData redisPiBoxActData = (RedisPiBoxActData) JSONObject.toBean(js,
                            RedisPiBoxActData.class);
					if (!jsonObject.getString("machine_code").isEmpty()) {
						redisPiBoxActData.machine_code=jsonObject.getString("machine_code");
					}*/
//					redisPiBoxApi.saveRedisPiBoxActData(redisPiBoxActData);
//					List<PiBoxCom> pis=redisPiBoxActData.getAct_time_data_list();
//					for(PiBoxCom p:pis){
//						
//						System.out.println("p.getCom()=="+p.getCom());
//						for(){
//							
//							
//							
//						}
//						
//					}
                    /*System.out.println(
                            "redisPiBoxActData.getAct_time_data_list()==" + redisPiBoxActData.act_time_data_list);
					System.out.println("redisPiBoxActData.getTime()=" + redisPiBoxActData.time);
					
					System.out.println("redisPiBoxActData.getMachine_code()==" + redisPiBoxActData.machine_code);*/

                    }

                    break;
                // 历史数据
                case HISTORY_DATA:

                    break;
                // 报警数据
                case ALARM_DATA:

                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            String simplename = e.getClass().getSimpleName();
            if (!"JSONException".equals(simplename)) {
                e.printStackTrace();
            }
            System.out.println("不能转成json的消息:" + boxMsg);


        }

    }

}
