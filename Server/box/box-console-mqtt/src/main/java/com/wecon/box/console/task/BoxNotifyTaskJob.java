package com.wecon.box.console.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.console.config.ConnectOptions;
import com.wecon.box.console.util.MqttConfigContext;
import com.wecon.box.console.util.SpringContextHolder;
import com.wecon.box.constant.Constant;
import com.wecon.box.entity.BaseMsgFeedback;
import com.wecon.box.entity.BaseMsgResp;
import com.wecon.box.entity.PlcExtend;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.common.util.CommonUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实现服务端与盒子通信的定时任务，每隔一段时间从数据库扫描获取需要
 * 下发给盒子的数据，通过mqtt实现发布消息给盒子并订阅盒子反馈的消息
 * 如果mqtt连接断开或失效则重连
 * Created by whp on 2017/8/24.
 */
public class BoxNotifyTaskJob implements Job {
    public MqttClient mqttClient;
    private String serverTopic = "pibox/stc/#";
    private final String clientId = "WECON_REVEIVE";

    private final int ACT_UPDATE_PLC_CONFIG = 2001;
    private final int ACT_UPDATE_REAL_HISTORY_CONFIG = 2002;
    private final int ACT_UPDATE_ALARM_DATA_CONFIG = 2003;
    private final int ACT_DELETE_MONITOR_CONFIG = 2004;

    private final int UPD_STATE_SUCCESS = 1;

    private static Logger logger = LoggerFactory.getLogger(BoxNotifyTaskJob.class);

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (mqttClient != null && mqttClient.isConnected()) {
            logger.debug("mqtt connection is normal !");
            //---通知盒子操作---
            updatePlcConfigHandle();
            return;
        }

        connect();
        subscribe();
    }

    /**
     * 更新通讯口配置通知盒子
     */
    private void updatePlcConfigHandle(){
        PlcInfoApi plcInfoApi = SpringContextHolder.getBean(PlcInfoApi.class);
        List<PlcExtend> plcExtendLst = plcInfoApi.getPlcExtendListByState(Constant.State.STATE_UPDATE_CONFIG);
        if(null != plcExtendLst){
            Map<String, List<PlcExtend>> groupPlcExtends = groupByMachineCode(plcExtendLst);
            if(null != groupPlcExtends){
                for(Map.Entry<String, List<PlcExtend>> entry : groupPlcExtends.entrySet()){
                    BaseMsgResp<Map<String, List<PlcExtend>>> baseMsgResp = new BaseMsgResp<Map<String, List<PlcExtend>>>();
                    baseMsgResp.setAct(ACT_UPDATE_PLC_CONFIG);
                    baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                    baseMsgResp.setMachine_code(entry.getKey());
                    Map<String, List<PlcExtend>> plcExtendResp = new HashMap<String, List<PlcExtend>>();
                    plcExtendResp.put("upd_com_list", entry.getValue());
                    baseMsgResp.setData(plcExtendResp);
                    //发布数据给盒子
                    serverTopic = serverTopic.replace("#", entry.getKey());
                    publish(JSON.toJSONString(baseMsgResp));
                }
            }
        }
    }

    /**
     * 更新实时和历史监控点配置
     */
    private void updateRealHistoryConfigHandle(){
        RealHisCfgApi realHisCfgApi = SpringContextHolder.getBean(RealHisCfgApi.class);
        List<RealHisCfgDevice> realHisCfgDList = realHisCfgApi.getRealHisCfgListByState(Constant.State.STATE_UPDATE_CONFIG);
        if(null != realHisCfgDList){

        }
    }

    /**
     * 对通讯口数据根据机器码进行分组
     * @param plcExtendLst
     * @return
     */
    private Map<String, List<PlcExtend>> groupByMachineCode(List<PlcExtend> plcExtendLst){
        if(null == plcExtendLst || plcExtendLst.size() == 0){
            return null;
        }

        Map<String, List<PlcExtend>> gPlcExtendMap = new HashMap<String, List<PlcExtend>>();
        for(PlcExtend p : plcExtendLst){
            if(CommonUtils.isNullOrEmpty(p.machine_code)) continue;
            List<PlcExtend> mPlcExtendLst = gPlcExtendMap.get(p.machine_code);
            if(null == mPlcExtendLst){
                mPlcExtendLst = new ArrayList<PlcExtend>();
            }
            mPlcExtendLst.add(p);
            gPlcExtendMap.put(p.machine_code, mPlcExtendLst);
        }

        return gPlcExtendMap;
    }

    /**
     * 处理盒子返回的消息
     * @param message
     */
    private void callBackHandle(String message){
        if(CommonUtils.isNullOrEmpty(message)){
            return;
        }
        PlcInfoApi plcInfoApi = SpringContextHolder.getBean(PlcInfoApi.class);

        //使用Map泛型主要是为了兼容所有类型反馈，比较灵活
        BaseMsgFeedback<Map<String, List<Map>>> baseMsgFeedback =  JSON.parseObject(message,
                new TypeReference<BaseMsgFeedback<Map<String, List<Map>>>>(){});

        int fbAct = baseMsgFeedback.getFeedback_act();
        try {
            switch (fbAct){
                case ACT_UPDATE_PLC_CONFIG : //更新通讯口配置反馈
                    Map<String, List<Map>> fbData = baseMsgFeedback.getData();
                    List<Map> updComList = fbData.get("upd_com_list");
                    if(null != updComList){
                        List<int[]> updArgList = new ArrayList<int[]>();
                        for(Map m : updComList){
                            if(UPD_STATE_SUCCESS == Integer.parseInt(m.get("upd_state").toString())){
                                updArgList.add(new int[]{Constant.State.STATE_SYNCED_BOX,
                                        Integer.parseInt(m.get("com").toString())});
                            }
                        }
                        plcInfoApi.batchUpdatePlcState(updArgList);
                    }
                    break;
                case ACT_UPDATE_REAL_HISTORY_CONFIG : //更新实时和历史监控点配置
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /**
     * 连接mqtt
     */
    private void connect(){
        MqttConnectOptions mqttConnectOptions = ConnectOptions.getConnectOptions(MqttConfigContext.mqttConfig.getUsername(),
                MqttConfigContext.mqttConfig.getPassword());
        try {
            mqttClient = new MqttClient(MqttConfigContext.mqttConfig.getHost(), clientId, new MemoryPersistence());
            mqttClient.connect(mqttConnectOptions);
            logger.error("mqtt connect success!");
        }catch (MqttException e){
            e.printStackTrace();
            logger.error("mqtt connect fail!");
        }
    }

    /**
     * 发布消息
     * @param message
     */
    private void publish(String message){
        MqttTopic mqttTopic = mqttClient.getTopic(serverTopic);
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(2);
        mqttMessage.setRetained(true);
        mqttMessage.setPayload(message.getBytes());
        PublishTask publishTask = new PublishTask(mqttTopic, mqttMessage);
        publishTask.start();
    }

    /**
     * 订阅消息
     */
    private void subscribe(){
        try {
            mqttClient.subscribe("pibox/cts/#");
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    /**
     * 订阅消息的回调，在messageArrived方法中获取订阅的消息
     */
    public class SubscribeCallback implements MqttCallback {
        public void connectionLost(Throwable cause) {
            // 连接丢失后，一般在这里面进行重连
            logger.debug("连接断开，可以做重连");
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
            logger.debug("deliveryComplete---------" + token.isComplete());
        }

        public void messageArrived(String topic, MqttMessage message) throws Exception {
            // subscribe后得到的消息会执行到这里面
            logger.debug("接收消息内容 : ", new String(message.getPayload()));
            callBackHandle(new String(message.getPayload()));
        }
    }
}
