package com.wecon.box.console.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.AlarmCfgApi;
import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.console.config.ConnectOptions;
import com.wecon.box.console.util.MqttConfigContext;
import com.wecon.box.console.util.SpringContextHolder;
import com.wecon.box.constant.Constant;
import com.wecon.box.entity.*;
import com.wecon.box.util.Converter;
import com.wecon.box.util.GroupOp;
import com.wecon.common.util.CommonUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;
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
    private final String clientId = "WECON_BOX_NOTIFY";

    private final int ACT_UPDATE_PLC_CONFIG = 2001; //更新通讯口配置
    private final int ACT_UPDATE_REAL_HISTORY_CONFIG = 2002; //更新实时和历史监控点配置
    private final int ACT_UPDATE_ALARM_DATA_CONFIG = 2003; //更新报警数据配置
    private final int ACT_DELETE_MONITOR_CONFIG = 2004; //删除监控点配置
    private final int ACT_DELETE_PLC_CONFIG = 2005; //删除通讯口配置

    private final int UPD_STATE_SUCCESS = 1;

    private static Logger logger = Logger.getLogger(BoxNotifyTaskJob.class);

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (mqttClient != null && mqttClient.isConnected()) {
            logger.debug("mqtt connection is normal !");
            notifyHandle();
            return;
        }

        connect();
        subscribe();
        notifyHandle();
    }

    /**
     * 通知盒子操作
     */
    private void notifyHandle(){
        updatePlcCfgHandle();
        updateRealHisCfgHandle();
        updateAlarmCfgHandle();
        deleteAllCfgHandle();
        deletePlcCfgHandle();
    }

    /**
     * 更新通讯口配置通知盒子
     */
    private void updatePlcCfgHandle(){
        try {
            logger.debug("updatePlcCfgHandle，开始从DB获取数据");
            PlcInfoApi plcInfoApi = SpringContextHolder.getBean(PlcInfoApi.class);
            List<PlcExtend> plcExtendLst = plcInfoApi.getPlcExtendListByState(Constant.State.STATE_UPDATE_CONFIG);
            logger.debug("updatePlcCfgHandle，获取更新条数："+(null==plcExtendLst?"0":plcExtendLst.size()));
            if(null != plcExtendLst){
                Map<String, List<Map>> groupPlcExtends = GroupOp.groupCfgByMachineCode(Converter.convertListOjToMap(plcExtendLst));
                if(null != groupPlcExtends){
                    for(Map.Entry<String, List<Map>> entry : groupPlcExtends.entrySet()){
                        BaseMsgResp<Map<String, List<Map>>> baseMsgResp = new BaseMsgResp<Map<String, List<Map>>>();
                        baseMsgResp.setAct(ACT_UPDATE_PLC_CONFIG);
                        baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                        baseMsgResp.setMachine_code(entry.getKey());
                        Map<String, List<Map>> plcExtendResp = new HashMap<String, List<Map>>();
                        plcExtendResp.put("upd_com_list", entry.getValue());
                        baseMsgResp.setData(plcExtendResp);
                        //发布数据给盒子
                        serverTopic = serverTopic.replace("#", entry.getKey());
                        publish(JSON.toJSONString(baseMsgResp));
                        logger.debug("updatePlcCfgHandle，通知盒子成功。"+JSON.toJSONString(baseMsgResp));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("updatePlcCfgHandle，通知盒子失败，"+e.getMessage());
        }
    }

    /**
     * 更新实时和历史监控点配置
     */
    private void updateRealHisCfgHandle(){
        RealHisCfgApi realHisCfgApi = SpringContextHolder.getBean(RealHisCfgApi.class);
        List<RealHisCfgExtend> realHisCfgList = realHisCfgApi.getRealHisCfgListByState(Constant.State.STATE_UPDATE_CONFIG);
        if(null != realHisCfgList){
            Map<String, List<Map>> groupRealHisCfg = GroupOp.groupCfgByMachineCode(Converter.convertListOjToMap(realHisCfgList));
            if(null != groupRealHisCfg){
                for(Map.Entry<String, List<Map>> entry : groupRealHisCfg.entrySet()){
                    BaseMsgResp<Map<String, List<BaseCfgCom<Map>>>> baseMsgResp = new BaseMsgResp<Map<String, List<BaseCfgCom<Map>>>>();
                    baseMsgResp.setAct(ACT_UPDATE_REAL_HISTORY_CONFIG);
                    baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                    baseMsgResp.setMachine_code(entry.getKey());
                    Map<String, List<Map>> cfgComMap = GroupOp.groupCfgFilterByCom(entry.getValue());
                    List<BaseCfgCom<Map>> cfgComList = new ArrayList<BaseCfgCom<Map>>();
                    for(Map.Entry<String, List<Map>> cfgEntry : cfgComMap.entrySet()){
                        BaseCfgCom<Map> cfgCom = new BaseCfgCom<Map>();
                        cfgCom.setCom(cfgEntry.getKey());
                        cfgCom.setCfg_list(cfgEntry.getValue());
                        cfgComList.add(cfgCom);
                    }
                    Map<String, List<BaseCfgCom<Map>>> realHisCfgResp = new HashMap<String, List<BaseCfgCom<Map>>>();
                    realHisCfgResp.put("upd_real_his_cfg_list", cfgComList);
                    baseMsgResp.setData(realHisCfgResp);
                    //发布数据给盒子
                    serverTopic = serverTopic.replace("#", entry.getKey());
                    publish(JSON.toJSONString(baseMsgResp));
                }
            }
        }
    }

    /**
     * 更新报警数据配置
     */
    private void updateAlarmCfgHandle(){
        AlarmCfgApi alarmCfgApi = SpringContextHolder.getBean(AlarmCfgApi.class);
        List<AlarmCfgExtend> alarmCfgExtendList = alarmCfgApi.getAlarmCfgExtendListByState(Constant.State.STATE_UPDATE_CONFIG);
        if(null != alarmCfgExtendList){
            Map<String, List<Map>> groupAlarmCfg = GroupOp.groupCfgByMachineCode(Converter.convertListOjToMap(alarmCfgExtendList));
            if(null != groupAlarmCfg){
                for(Map.Entry<String, List<Map>> entry : groupAlarmCfg.entrySet()){
                    BaseMsgResp<Map<String, List<BaseCfgCom<Map>>>> baseMsgResp = new BaseMsgResp<Map<String, List<BaseCfgCom<Map>>>>();
                    baseMsgResp.setAct(ACT_UPDATE_ALARM_DATA_CONFIG);
                    baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                    baseMsgResp.setMachine_code(entry.getKey());
                    Map<String, List<Map>> cfgComMap = GroupOp.groupCfgFilterByCom(entry.getValue());
                    List<BaseCfgCom<Map>> cfgComList = new ArrayList<BaseCfgCom<Map>>();
                    for(Map.Entry<String, List<Map>> cfgEntry : cfgComMap.entrySet()){
                        BaseCfgCom<Map> cfgCom = new BaseCfgCom<Map>();
                        cfgCom.setCom(cfgEntry.getKey());
                        cfgCom.setCfg_list(cfgEntry.getValue());
                        cfgComList.add(cfgCom);
                    }
                    Map<String, List<BaseCfgCom<Map>>> realHisCfgResp = new HashMap<String, List<BaseCfgCom<Map>>>();
                    realHisCfgResp.put("upd_alarm_cfg_list", cfgComList);
                    baseMsgResp.setData(realHisCfgResp);
                    //发布数据给盒子
                    serverTopic = serverTopic.replace("#", entry.getKey());
                    publish(JSON.toJSONString(baseMsgResp));
                }
            }
        }
    }

    /**
     * 删除监控点配置
     */
    private void deleteAllCfgHandle(){
        RealHisCfgApi realHisCfgApi = SpringContextHolder.getBean(RealHisCfgApi.class);
        AlarmCfgApi alarmCfgApi = SpringContextHolder.getBean(AlarmCfgApi.class);
        List<RealHisCfgExtend> realHisCfgList = realHisCfgApi.getRealHisCfgListByState(Constant.State.STATE_DELETE_CONFIG);
        List<AlarmCfgExtend> alarmCfgExtendList = alarmCfgApi.getAlarmCfgExtendListByState(Constant.State.STATE_DELETE_CONFIG);
        if(null != realHisCfgList){
            List<RealHisCfgDevice> realCfgList = new ArrayList<RealHisCfgDevice>();
            List<RealHisCfgDevice> hisCfgList = new ArrayList<RealHisCfgDevice>();
            for(RealHisCfgDevice r : realHisCfgList){
                if(r.data_type == Constant.DataType.DATA_TYPE_REAL){
                    realCfgList.add(r);
                }else if(r.data_type == Constant.DataType.DATA_TYPE_HISTORY){
                    hisCfgList.add(r);
                }
            }
            deleteRealHisCfgPublish(Converter.convertListOjToMap(realCfgList), Constant.DataType.DATA_TYPE_REAL);
            deleteRealHisCfgPublish(Converter.convertListOjToMap(hisCfgList), Constant.DataType.DATA_TYPE_HISTORY);
        }
        if(null != alarmCfgExtendList){
            deleteRealHisCfgPublish(Converter.convertListOjToMap(alarmCfgExtendList), Constant.DataType.DATA_TYPE_ALARM);
        }
    }

    private void deleteRealHisCfgPublish(List<Map> cfgList, int delType){
        Map<String, List<Map>> groupCfgMap = GroupOp.groupCfgByMachineCode(cfgList);
        if(null != groupCfgMap){
            for(Map.Entry<String, List<Map>> entry : groupCfgMap.entrySet()){
                BaseMsgResp<Map<String, List<Map>>> baseMsgResp = new BaseMsgResp<Map<String, List<Map>>>();
                baseMsgResp.setAct(ACT_DELETE_MONITOR_CONFIG);
                baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                baseMsgResp.setMachine_code(entry.getKey());
                Map<String, List<Map>> cfgComMap = GroupOp.groupCfgFilterByCom(entry.getValue(), "addr_id", "upd_time");
                List<Map> cfgComList = new ArrayList<Map>();
                for(Map.Entry<String, List<Map>> cfgEntry : cfgComMap.entrySet()){
                    Map m = new HashMap();
                    m.put("com", cfgEntry.getKey());
                    m.put("del_type", delType);
                    m.put("cfg_id_list", cfgEntry.getValue());
                    cfgComList.add(m);
                }
                Map<String, List<Map>> realHisCfgResp = new HashMap<String, List<Map>>();
                realHisCfgResp.put("del_cfg_list", cfgComList);
                baseMsgResp.setData(realHisCfgResp);
                //发布数据给盒子
                serverTopic = serverTopic.replace("#", entry.getKey());
                publish(JSON.toJSONString(baseMsgResp));
            }
        }
    }

    /**
     * 删除通讯口配置
     */
    private void deletePlcCfgHandle(){
        PlcInfoApi plcInfoApi = SpringContextHolder.getBean(PlcInfoApi.class);
        List<PlcExtend> plcExtendLst = plcInfoApi.getPlcExtendListByState(Constant.State.STATE_DELETE_CONFIG);
        if(null != plcExtendLst){
            Map<String, List<Map>> groupPlcExtends = GroupOp.groupCfgByMachineCode(Converter.convertListOjToMap(plcExtendLst), "com", "upd_time");
            if(null != groupPlcExtends){
                for(Map.Entry<String, List<Map>> entry : groupPlcExtends.entrySet()){
                    BaseMsgResp<Map<String, List<Map>>> baseMsgResp = new BaseMsgResp<Map<String, List<Map>>>();
                    baseMsgResp.setAct(ACT_DELETE_PLC_CONFIG);
                    baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                    baseMsgResp.setMachine_code(entry.getKey());
                    Map<String, List<Map>> plcExtendResp = new HashMap<String, List<Map>>();
                    plcExtendResp.put("del_com_list", entry.getValue());
                    baseMsgResp.setData(plcExtendResp);
                    //发布数据给盒子
                    serverTopic = serverTopic.replace("#", entry.getKey());
                    publish(JSON.toJSONString(baseMsgResp));
                }
            }
        }
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
        RealHisCfgApi realHisCfgApi = SpringContextHolder.getBean(RealHisCfgApi.class);
        AlarmCfgApi alarmCfgApi = SpringContextHolder.getBean(AlarmCfgApi.class);

        try {
            //使用Map泛型主要是为了兼容所有类型反馈，比较灵活
            BaseMsgFeedback<Map<String, List<Map>>> baseMsgFeedback =  JSON.parseObject(message,
                    new TypeReference<BaseMsgFeedback<Map<String, List<Map>>>>(){});
            int fbAct = baseMsgFeedback.getFeedback_act();
            Map<String, List<Map>> fbData = baseMsgFeedback.getData();
            switch (fbAct){
                case ACT_UPDATE_PLC_CONFIG : //更新通讯口配置反馈
                    List<Map> updComList = fbData.get("upd_com_list");
                    plcInfoApi.batchUpdateState(getFeedbackArgs(updComList, "com"));
                    break;
                case ACT_UPDATE_REAL_HISTORY_CONFIG : //更新实时和历史监控点配置
                    List<Map> updRealHisCfgList = fbData.get("upd_ real_his_cfg_list");
                    realHisCfgApi.batchUpdateState(getFeedbackArgs(updRealHisCfgList, "addr_id"));
                    break;
                case ACT_UPDATE_ALARM_DATA_CONFIG : //更新报警数据配置
                    List<Map> updAlarmCfgList = fbData.get("upd_alarm_cfg_list");
                    alarmCfgApi.batchUpdateState(getFeedbackArgs(updAlarmCfgList, "addr_id"));
                    break;
                case ACT_DELETE_MONITOR_CONFIG : //删除监控点配置
                    List<Map> delCfgList = fbData.get("del_cfg_list");
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private List<int[]> getFeedbackArgs(List<Map> updCfgList, String idKey){
        if(null != updCfgList){
            List<int[]> updArgList = new ArrayList<int[]>();
            for(Map m : updCfgList){
                if(UPD_STATE_SUCCESS == Integer.parseInt(m.get("upd_state").toString())){
                    updArgList.add(new int[]{Constant.State.STATE_SYNCED_BOX,
                            Integer.parseInt(m.get(idKey).toString())});
                }
            }
            return updArgList;
        }
        return null;
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
            mqttClient.setCallback(new SubscribeCallback());
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
            logger.debug("接收消息内容 : "+ new String(message.getPayload()));
            callBackHandle(new String(message.getPayload()));
        }
    }
}
