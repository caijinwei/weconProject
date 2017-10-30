package com.wecon.box.console.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.*;
import com.wecon.box.console.config.ConnectOptions;
import com.wecon.box.console.util.MqttConfigContext;
import com.wecon.box.console.util.SpringContextHolder;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.constant.Constant;
import com.wecon.box.entity.*;
import com.wecon.box.util.Base64Util;
import com.wecon.box.util.Converter;
import com.wecon.box.util.GroupOp;
import com.wecon.common.redis.RedisManager;
import com.wecon.common.util.CommonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zengzhipeng on 2017/9/2.
 */
public class BoxNotifyTask extends Thread {
    public static MqttClient mqttClient;
    private final String clientId = "WECON_BOX_NOTIFY";
    private String serverTopicPrefix = "pibox/stc/";
    private final int ACT_UPDATE_PLC_CONFIG = 2001; //更新通讯口配置
    private final int ACT_UPDATE_REAL_HISTORY_CONFIG = 2002; //更新实时和历史监控点配置
    private final int ACT_UPDATE_ALARM_DATA_CONFIG = 2003; //更新报警数据配置
    private final int ACT_DELETE_MONITOR_CONFIG = 2004; //删除监控点配置
    private final int ACT_DELETE_PLC_CONFIG = 2005; //删除通讯口配置
    private final int ACT_SEND_DRIVER_FILE = 2008; //下发驱动文件

    private final int UPD_STATE_SUCCESS = 1;
    private final int UPD_STATE_GET_DRIVER = 0;

    private static int sleepTime = 1000 * 30;
    private static int publishSleepTime = 100;

    private static int publishPageSize = 50;

    private Map<Long, PlcExtend> plcCfgCache = new ConcurrentHashMap<Long, PlcExtend>();

    private static final Logger logger = LogManager.getLogger(BoxNotifyTask.class);

    public void run() {
        logger.info("BoxNotifyTask run start");
        while (true) {
            try {
                if (mqttClient == null || !mqttClient.isConnected()) {
                    logger.info("mqtt connection is disconnection !");
                    connect();
                    subscribe();
                }
                notifyHandle();
                sleep(sleepTime);

            } catch (Exception e) {
                logger.error(e);
            }
        }
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
    private synchronized void updatePlcCfgHandle(){
        try {
            logger.info("updatePlcCfgHandle，开始从DB获取数据");
            PlcInfoApi plcInfoApi = SpringContextHolder.getBean(PlcInfoApi.class);
            List<PlcExtend> plcExtendLst = plcInfoApi.getPlcExtendListByState(Constant.State.STATE_UPDATE_CONFIG, Constant.State.STATE_NEW_CONFIG);
            logger.info("updatePlcCfgHandle，获取更新条数："+(null==plcExtendLst?"0":plcExtendLst.size()));
            if(null != plcExtendLst){
                putPlcCache(plcExtendLst);
                Map<String, List<Map>> groupPlcExtends = GroupOp.groupCfgByMachineCode(Converter.convertListOjToMap(plcExtendLst), PlcExtend.UPDATE_PLC_FIELD_FILTER);
                if(null != groupPlcExtends){
                    for(Map.Entry<String, List<Map>> entry : groupPlcExtends.entrySet()){
                        //每个机器码分页进行下发，一页50条
                        List<List<Map>> cfgPage = getCfgListByPage(entry.getValue());
                        for(List<Map> cfgList : cfgPage){
                            BaseMsgResp<Map<String, List<Map>>> baseMsgResp = new BaseMsgResp<Map<String, List<Map>>>();
                            baseMsgResp.setAct(ACT_UPDATE_PLC_CONFIG);
                            baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                            baseMsgResp.setMachine_code(entry.getKey());
                            Map<String, List<Map>> plcExtendResp = new HashMap<String, List<Map>>();
                            plcExtendResp.put("upd_com_list", cfgList);
                            baseMsgResp.setData(plcExtendResp);
                            //发布数据给盒子
                            publish(JSON.toJSONString(baseMsgResp), serverTopicPrefix+entry.getKey());
                            logger.info("updatePlcCfgHandle，通知盒子成功。"+JSON.toJSONString(baseMsgResp));
                            //防止发布过于频繁
                            Thread.sleep(publishSleepTime);
                        }
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
    private synchronized void updateRealHisCfgHandle(){
        try {
            logger.info("updateRealHisCfgHandle，开始从DB获取数据");
            RealHisCfgApi realHisCfgApi = SpringContextHolder.getBean(RealHisCfgApi.class);
            List<RealHisCfgExtend> realHisCfgList = realHisCfgApi.getRealHisCfgListByState(Constant.State.STATE_UPDATE_CONFIG, Constant.State.STATE_NEW_CONFIG);
            logger.info("updateRealHisCfgHandle，获取更新条数："+(null==realHisCfgList?"0":realHisCfgList.size()));
            if(null != realHisCfgList){
                Map<String, List<Map>> groupRealHisCfg = GroupOp.groupCfgByMachineCode(Converter.convertListOjToMap(realHisCfgList), RealHisCfgExtend.UPDATE_REAL_HIS_FIELD_FILTER);
                if(null != groupRealHisCfg){
                    for(Map.Entry<String, List<Map>> entry : groupRealHisCfg.entrySet()){
                        //每个机器码分页进行下发，一页50条
                        List<List<Map>> cfgPage = getCfgListByPage(entry.getValue());
                        for(List<Map> cfgList : cfgPage){
                            BaseMsgResp<Map<String, List<BaseCfgCom<Map>>>> baseMsgResp = new BaseMsgResp<Map<String, List<BaseCfgCom<Map>>>>();
                            baseMsgResp.setAct(ACT_UPDATE_REAL_HISTORY_CONFIG);
                            baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                            baseMsgResp.setMachine_code(entry.getKey());
                            Map<String, List<Map>> cfgComMap = GroupOp.groupCfgFilterByCom(cfgList);
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
                            publish(JSON.toJSONString(baseMsgResp), serverTopicPrefix+entry.getKey());
                            logger.info("updateRealHisCfgHandle，通知盒子成功。"+JSON.toJSONString(baseMsgResp));
                            //防止发布过于频繁
                            Thread.sleep(publishSleepTime);
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error("updateRealHisCfgHandle，通知盒子失败，"+e.getMessage());
        }
    }

    /**
     * 更新报警数据配置
     */
    private synchronized void updateAlarmCfgHandle(){
        try {
            logger.info("updateAlarmCfgHandle，开始从DB获取数据");
            AlarmCfgApi alarmCfgApi = SpringContextHolder.getBean(AlarmCfgApi.class);
            List<AlarmCfgExtend> alarmCfgExtendList = alarmCfgApi.getAlarmCfgExtendListByState(Constant.State.STATE_UPDATE_CONFIG, Constant.State.STATE_NEW_CONFIG);
            logger.info("updateAlarmCfgHandle，获取更新条数："+(null==alarmCfgExtendList?"0":alarmCfgExtendList.size()));
            if(null != alarmCfgExtendList){
                Map<String, List<Map>> groupAlarmCfg = GroupOp.groupCfgByMachineCode(Converter.convertListOjToMap(alarmCfgExtendList), AlarmCfgExtend.UPDATE_ALARM_FIELD_FILTER);
                if(null != groupAlarmCfg){
                    for(Map.Entry<String, List<Map>> entry : groupAlarmCfg.entrySet()){
                        //每个机器码分页进行下发，一页50条
                        List<List<Map>> cfgPage = getCfgListByPage(entry.getValue());
                        for(List<Map> cfgList : cfgPage){
                            BaseMsgResp<Map<String, List<BaseCfgCom<Map>>>> baseMsgResp = new BaseMsgResp<Map<String, List<BaseCfgCom<Map>>>>();
                            baseMsgResp.setAct(ACT_UPDATE_ALARM_DATA_CONFIG);
                            baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                            baseMsgResp.setMachine_code(entry.getKey());
                            Map<String, List<Map>> cfgComMap = GroupOp.groupCfgFilterByCom(cfgList);
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
                            publish(JSON.toJSONString(baseMsgResp), serverTopicPrefix+entry.getKey());
                            logger.info("updateAlarmCfgHandle，通知盒子成功。"+JSON.toJSONString(baseMsgResp));
                            //防止发布过于频繁
                            Thread.sleep(publishSleepTime);
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error("updateAlarmCfgHandle，通知盒子失败，"+e.getMessage());
        }

    }

    /**
     * 删除监控点配置
     */
    private synchronized void deleteAllCfgHandle(){
        try {
            logger.info("deleteAllCfgHandle，开始从DB获取数据");
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
        }catch (Exception e){
            logger.error("deleteAllCfgHandle，通知盒子失败，"+e.getMessage());
        }

    }

    private synchronized void deleteRealHisCfgPublish(List<Map> cfgList, int delType){
        try {
            Map<String, List<Map>> groupCfgMap = GroupOp.groupCfgByMachineCode(cfgList);
            if(null != groupCfgMap){
                for(Map.Entry<String, List<Map>> entry : groupCfgMap.entrySet()){
                    //每个机器码分页进行下发，一页50条
                    List<List<Map>> cfgPage = getCfgListByPage(entry.getValue());
                    for(List<Map> cList : cfgPage){
                        BaseMsgResp<Map<String, List<Map>>> baseMsgResp = new BaseMsgResp<Map<String, List<Map>>>();
                        baseMsgResp.setAct(ACT_DELETE_MONITOR_CONFIG);
                        baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                        baseMsgResp.setMachine_code(entry.getKey());
                        Map<String, List<Map>> cfgComMap = GroupOp.groupCfgFilterByCom(cList, "addr_id", "upd_time");
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
                        publish(JSON.toJSONString(baseMsgResp), serverTopicPrefix+entry.getKey());
                        logger.info("deleteAllCfgHandle，通知盒子成功。"+JSON.toJSONString(baseMsgResp));
                        //防止发布过于频繁
                        Thread.sleep(publishSleepTime);
                    }
                }
            }
        }catch (Exception e){

        }

    }

    /**
     * 删除通讯口配置
     */
    private synchronized void deletePlcCfgHandle(){
        try {
            logger.info("deletePlcCfgHandle，开始从DB获取数据");
            PlcInfoApi plcInfoApi = SpringContextHolder.getBean(PlcInfoApi.class);
            List<PlcExtend> plcExtendLst = plcInfoApi.getPlcExtendListByState(Constant.State.STATE_DELETE_CONFIG);
            logger.info("deletePlcCfgHandle，获取删除条数："+(null==plcExtendLst?"0":plcExtendLst.size()));
            if(null != plcExtendLst){
                Map<String, List<Map>> groupPlcExtends = GroupOp.groupCfgByMachineCode(Converter.convertListOjToMap(plcExtendLst), "com", "upd_time");
                if(null != groupPlcExtends){
                    for(Map.Entry<String, List<Map>> entry : groupPlcExtends.entrySet()){
                        //每个机器码分页进行下发，一页50条
                        List<List<Map>> cfgPage = getCfgListByPage(entry.getValue());
                        for(List<Map> cList : cfgPage){
                            BaseMsgResp<Map<String, List<Map>>> baseMsgResp = new BaseMsgResp<Map<String, List<Map>>>();
                            baseMsgResp.setAct(ACT_DELETE_PLC_CONFIG);
                            baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                            baseMsgResp.setMachine_code(entry.getKey());
                            Map<String, List<Map>> plcExtendResp = new HashMap<String, List<Map>>();
                            plcExtendResp.put("del_com_list", cList);
                            baseMsgResp.setData(plcExtendResp);
                            //发布数据给盒子
                            publish(JSON.toJSONString(baseMsgResp), serverTopicPrefix+entry.getKey());
                            logger.info("deletePlcCfgHandle，通知盒子成功。"+JSON.toJSONString(baseMsgResp));
                            //防止发布过于频繁
                            Thread.sleep(publishSleepTime);
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error("deletePlcCfgHandle，通知盒子失败，"+e.getMessage());
        }

    }

    /**
     * 下发驱动文件
     * @param plcId
     */
    private synchronized void sendDriverFileHandle(long plcId){
        try {
            logger.info("sendDriverInfoHandle，开始从DB获取数据");
            DriverApi driverApi = SpringContextHolder.getBean(DriverApi.class);
            Map<String, Object> driverInfo = driverApi.getDriverExtend(plcId);
            if(null != driverInfo){
                BaseMsgResp<Map<String, Object>> baseMsgResp = new BaseMsgResp<Map<String, Object>>();
                baseMsgResp.setAct(ACT_SEND_DRIVER_FILE);
                baseMsgResp.setFeedback(BaseMsgResp.TYPE_FEEDBACK_NEED);
                String machine_code = driverInfo.get("machine_code").toString();
                baseMsgResp.setMachine_code(machine_code);
                baseMsgResp.setData(driverInfo);
                //发布数据给盒子
                publish(JSON.toJSONString(baseMsgResp), serverTopicPrefix+machine_code);
                logger.info("sendDriverInfoHandle，通知盒子成功。"+JSON.toJSONString(baseMsgResp));
            }

        }catch (Exception e){
            e.printStackTrace();
            logger.error("sendDriverInfoHandle，通知盒子失败，"+e.getMessage());
        }
    }

    /**
     * 处理盒子返回的消息
     * @param message
     */
    private synchronized void callBackHandle(String message){
        if(CommonUtils.isNullOrEmpty(message)){
            return;
        }
        PlcInfoApi plcInfoApi = SpringContextHolder.getBean(PlcInfoApi.class);
        RealHisCfgApi realHisCfgApi = SpringContextHolder.getBean(RealHisCfgApi.class);
        AlarmCfgApi alarmCfgApi = SpringContextHolder.getBean(AlarmCfgApi.class);
        AlarmCfgDataApi alarmCfgDataApi = SpringContextHolder.getBean(AlarmCfgDataApi.class);
        RealHisCfgDataApi realHisCfgDataApi = SpringContextHolder.getBean(RealHisCfgDataApi.class);
        ViewAccountRoleApi viewAccountRoleApi = SpringContextHolder.getBean(ViewAccountRoleApi.class);
        AccountDirRelApi accountDirRelApi = SpringContextHolder.getBean(AccountDirRelApi.class);

        try {
            //使用Map泛型主要是为了兼容所有类型反馈，比较灵活
            BaseMsgFeedback<Map<String, Object>> baseMsgFeedback =  JSON.parseObject(message,
                    new TypeReference<BaseMsgFeedback<Map<String, Object>>>(){});
            int fbAct = baseMsgFeedback.getFeedback_act();
            Map<String, Object> fbData = baseMsgFeedback.getData();
            //logger.info("接收盒子反馈消息，feedback_act："+fbAct+"。"+message);
            switch (fbAct){
                case ACT_UPDATE_PLC_CONFIG : //更新通讯口配置反馈
                    List<Map> updComList = (List<Map>)fbData.get("upd_com_list");
                    if(null != updComList && updComList.size() > 0){
                        for(Map m : updComList){
                            try {
                                int upd_state = Integer.parseInt(m.get("upd_state").toString());
                                long plcId = Long.parseLong(m.get("com").toString());
                                if(UPD_STATE_GET_DRIVER == upd_state){
                                    sendDriverFileHandle(plcId);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        List<String[]> fUpdArgs = getFeedbackUpdArgs(updComList, "com");
                        plcInfoApi.batchUpdateState(fUpdArgs);
                        plcInfoApi.batchUpdateFileMd5(fUpdArgs);

                        //更新失败的状态
                        List<String[]> fFailArgs = getFeedbackFailArgs(updComList, "com", null);
                        plcInfoApi.batchUpdateState(fFailArgs);

                        //状态为4的删除监控点下的配置
                        /*List<Long> plcIds = plcInfoApi.getDeleteIdsByUpdTime(getFeedbackDelArgs(updComList, "com", null));
                        if(null != plcIds){
                            for(Long id : plcIds){
                                for(Map.Entry<Long, PlcExtend> entry : plcCfgCache.entrySet()){
                                    PlcExtend p = entry.getValue();
                                    if(id == p.plc_id && p.state != Constant.State.STATE_UPDATE_CONFIG_PD){
                                        plcIds.remove(id);
                                    }
                                }
                            }
                            alarmCfgApi.batchDeleteByPlcId(plcIds);
                            realHisCfgApi.batchDeleteByPlcId(plcIds);
                            alarmCfgDataApi.batchDeleteByPlcId(plcIds);
                            realHisCfgDataApi.batchDeleteByPlcId(plcIds);
                        }*/

                        //删除缓存数据
                        removePlcCache(fUpdArgs);
                    }
                    break;
                case ACT_UPDATE_REAL_HISTORY_CONFIG : //更新实时和历史监控点配置
                    List<Map> updRealHisCfgList = (List<Map>)fbData.get("upd_real_his_cfg_list");
                    realHisCfgApi.batchUpdateState(getFeedbackUpdArgs(updRealHisCfgList, "addr_id"));
                    realHisCfgApi.batchUpdateState(getFeedbackFailArgs(updRealHisCfgList, "addr_id", null));
                    RedisManager.publish(ConstKey.REDIS_GROUP_NAME, baseMsgFeedback.getMachine_code(), baseMsgFeedback.getMachine_code());
                    break;
                case ACT_UPDATE_ALARM_DATA_CONFIG : //更新报警数据配置
                    List<Map> updAlarmCfgList = (List<Map>)fbData.get("upd_alarm_cfg_list");
                    alarmCfgApi.batchUpdateState(getFeedbackUpdArgs(updAlarmCfgList, "addr_id"));
                    alarmCfgApi.batchUpdateState(getFeedbackFailArgs(updAlarmCfgList, "addr_id", null));
                    break;
                case ACT_DELETE_MONITOR_CONFIG : //删除监控点配置
                    List<Map> delCfgList = (List<Map>)fbData.get("del_cfg_list");
                    List<Long> alarmCfgIds = alarmCfgApi.getDeleteIdsByUpdTime(getFeedbackDelArgs(delCfgList, "addr_id", Constant.DataType.DATA_TYPE_ALARM));
                    List<Long> realCfgIds = realHisCfgApi.getDeleteIdsByUpdTime(getFeedbackDelArgs(delCfgList, "addr_id", Constant.DataType.DATA_TYPE_REAL));
                    List<Long> hisCfgIds = realHisCfgApi.getDeleteIdsByUpdTime(getFeedbackDelArgs(delCfgList, "addr_id", Constant.DataType.DATA_TYPE_HISTORY));
                    List<Long> realHisCfgIds = new ArrayList<Long>();
                    if(null != realCfgIds){
                        realHisCfgIds.addAll(realCfgIds);
                    }
                    if(null != hisCfgIds){
                        realHisCfgIds.addAll(hisCfgIds);
                    }
                    //删除监控点配置、数据、相关权限
                    realHisCfgApi.batchDeleteById(realHisCfgIds);
                    realHisCfgDataApi.batchDeleteById(realHisCfgIds);
                    alarmCfgApi.batchDeleteById(alarmCfgIds);
                    alarmCfgDataApi.batchDeleteById(alarmCfgIds);
                    viewAccountRoleApi.batchDeleteViewAccRoleByCfgId(realHisCfgIds, 1);
                    viewAccountRoleApi.batchDeleteViewAccRoleByCfgId(alarmCfgIds, 2);
                    accountDirRelApi.batchDeleteAccDirRelByCfgId(realHisCfgIds, 1, 2);
                    accountDirRelApi.batchDeleteAccDirRelByCfgId(alarmCfgIds, 3);
                    //更新失败的状态
                    realHisCfgApi.batchUpdateState(getFeedbackFailArgs(delCfgList, "addr_id", Constant.DataType.DATA_TYPE_REAL));
                    realHisCfgApi.batchUpdateState(getFeedbackFailArgs(delCfgList, "addr_id", Constant.DataType.DATA_TYPE_HISTORY));
                    alarmCfgApi.batchUpdateState(getFeedbackFailArgs(delCfgList, "addr_id", Constant.DataType.DATA_TYPE_ALARM));
                    break;
                case ACT_DELETE_PLC_CONFIG : //删除通讯口配置
                    List<Map> delComList = (List<Map>)fbData.get("del_com_list");
                    List<Long> plcIds = plcInfoApi.getDeleteIdsByUpdTime(getFeedbackDelArgs(delComList, "com", null));
                    //删除通讯口数据，实时历史报警配置、数据
                    plcInfoApi.batchDeletePlc(plcIds);
                    alarmCfgApi.batchDeleteByPlcId(plcIds);
                    realHisCfgApi.batchDeleteByPlcId(plcIds);
                    alarmCfgDataApi.batchDeleteByPlcId(plcIds);
                    realHisCfgDataApi.batchDeleteByPlcId(plcIds);
                    List<Long> realHisCfgIdList = realHisCfgApi.getRealHisCfgIdsByPlcIds(plcIds);
                    List<Long> alarmCfgIdList = alarmCfgApi.getAlarmCfgIdsByPlcIds(plcIds);
                    viewAccountRoleApi.batchDeleteViewAccRoleByCfgId(realHisCfgIdList, 1);
                    viewAccountRoleApi.batchDeleteViewAccRoleByCfgId(alarmCfgIdList, 2);
                    accountDirRelApi.batchDeleteAccDirRelByCfgId(realHisCfgIdList, 1, 2);
                    accountDirRelApi.batchDeleteAccDirRelByCfgId(alarmCfgIdList, 3);
                    //更新失败的状态
                    List<String[]> fFailArgs = getFeedbackFailArgs(delComList, "com", null);
                    plcInfoApi.batchUpdateState(fFailArgs);
                    break;
                case ACT_SEND_DRIVER_FILE : //下发驱动文件
                    String upd_state = fbData.get("upd_state").toString();
                    String upd_time = fbData.get("upd_time").toString();
                    String com = fbData.get("com").toString();
                    List<String[]> fUpdArgs = new ArrayList<String[]>();
                    if(UPD_STATE_SUCCESS == Integer.parseInt(upd_state)){
                        fUpdArgs.add(new String[]{Constant.State.STATE_SYNCED_BOX+"", com, upd_time});
                        plcInfoApi.batchUpdateState(fUpdArgs);
                        plcInfoApi.batchUpdateFileMd5(fUpdArgs);
                    }else if(0 > Integer.parseInt(upd_state)){
                        fUpdArgs.add(new String[]{upd_state, com, upd_time});
                        plcInfoApi.batchUpdateState(fUpdArgs);
                    }
                    break;
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (JSONException e){
            //e.printStackTrace();
        }
    }

    private List<String[]> getFeedbackFailArgs(List<Map> cfgList, String idKey, Integer delType){
        if(null != cfgList){
            List<String[]> updArgList = new ArrayList<String[]>();
            for(Map m : cfgList){
                try {
                    int state = Integer.parseInt(m.get("upd_state").toString());
                    if(0 > state){
                        Object delTypeOj = m.get("del_type");
                        String id = m.get(idKey).toString();
                        String updTime = m.get("upd_time").toString();
                        if(null != delTypeOj && null != delType){
                            if(Integer.parseInt(delTypeOj.toString()) == delType){
                                updArgList.add(new String[]{state+"", id, updTime});
                            }
                        }else{
                            updArgList.add(new String[]{state+"", id, updTime});
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return updArgList;
        }
        return null;
    }

    private List<String[]> getFeedbackUpdArgs(List<Map> updCfgList, String idKey){
        if(null != updCfgList){
            List<String[]> updArgList = new ArrayList<String[]>();
            for(Map m : updCfgList){
                try {
                    if(UPD_STATE_SUCCESS == Integer.parseInt(m.get("upd_state").toString())){
                        updArgList.add(new String[]{Constant.State.STATE_SYNCED_BOX+"",
                                m.get(idKey).toString(), m.get("upd_time").toString()});
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return updArgList;
        }
        return null;
    }

    private List<String[]> getFeedbackDelArgs(List<Map> delCfgList, String idKey, Integer delType){
        if(null != delCfgList){
            List<String[]> delArgList = new ArrayList<String[]>();
            for(Map m : delCfgList){
                try {
                    if(UPD_STATE_SUCCESS == Integer.parseInt(m.get("upd_state").toString())){
                        Object delTypeOj = m.get("del_type");
                        String id = m.get(idKey).toString();
                        String updTime = m.get("upd_time").toString();
                        if(null != delTypeOj && null != delType){
                            if(Integer.parseInt(delTypeOj.toString()) == delType){
                                delArgList.add(new String[]{id, updTime});
                            }
                        }else{
                            delArgList.add(new String[]{id, updTime});
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return delArgList;
        }
        return null;
    }

    private  List<List<Map>> getCfgListByPage(List<Map> cfgList){
        if(null == cfgList){
            return null;
        }
        List<List<Map>> result = new ArrayList<List<Map>>();
        int size = cfgList.size();
        int startIndex = 0;
        while (startIndex + publishPageSize <= size){
            result.add(cfgList.subList(startIndex, startIndex + publishPageSize));
            startIndex = startIndex + publishPageSize;
        }
        if(startIndex < cfgList.size()){
            result.add(cfgList.subList(startIndex, cfgList.size()));
        }
        return result;
    }

    private void putPlcCache(List<PlcExtend> plcExtendLst){
        if(null == plcExtendLst || plcExtendLst.size() == 0){
            return;
        }
        for(PlcExtend p : plcExtendLst){
            plcCfgCache.put(p.plc_id, p);
        }
    }

    private void removePlcCache(List<String[]> fUpdArgs){
        if(null == fUpdArgs || fUpdArgs.size() == 0){
            return;
        }
        for(Map.Entry<Long, PlcExtend> entry : plcCfgCache.entrySet()){
            Long plc_id = entry.getValue().plc_id;
            for(String[] args : fUpdArgs){
                if(Long.parseLong(args[1]) == plc_id){
                    plcCfgCache.remove(plc_id);
                    break;
                }
            }
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
            logger.info("mqtt connect success!");
        }catch (MqttException e){
            e.printStackTrace();
            logger.error("mqtt connect fail!");
        }
    }

    /**
     * 发布消息
     * @param message
     */
    private void publish(String message, String serverTopic){
        MqttTopic mqttTopic = mqttClient.getTopic(serverTopic);
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(0); 
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
            //logger.info("连接断开，可以做重连");
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
            logger.info("deliveryComplete---------" + token.isComplete());
        }

        public void messageArrived(String topic, MqttMessage message) throws Exception {
            // subscribe后得到的消息会执行到这里面
            logger.info("接收盒子反馈消息 : "+ new String(message.getPayload()));
            callBackHandle(new String(message.getPayload()));
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
