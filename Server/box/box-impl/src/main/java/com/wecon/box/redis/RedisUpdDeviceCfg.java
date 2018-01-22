package com.wecon.box.redis;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.Device;
import com.wecon.box.entity.PlcInfo;
import com.wecon.common.redis.RedisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by caijw on 2018/1/20.
 */

@Component
public class RedisUpdDeviceCfg {
    @Autowired
    PlcInfoApi plcInfoApi;
    @Autowired
    DeviceApi deviceApi;
    public  boolean  pubUpdPlcInfo(long currentPlcId){
        //消息推送给redis 提高实时性
        //    1.1盒子在线
        PlcInfo newplcInfo = plcInfoApi.getPlcInfo(currentPlcId);
        Device device = deviceApi.getDevice(newplcInfo.device_id);
        if(device.state == 1) {
            pubAction(10,currentPlcId,device.device_id);
            return true;
        }else{
            return false;
        }
    }
    public boolean pubDelPlcInfo(long currentPlcId){
        //    1.1盒子在线
        PlcInfo plcInfo = plcInfoApi.getPlcInfo(currentPlcId);
        Device device = deviceApi.getDevice(plcInfo.device_id);
        if(device.state == 1) {
            pubAction(11,currentPlcId,device.device_id);
            return true;
        }else{
            return false;
        }
    }
    public boolean pubDelAlarmCfg(long alarmCfgId ,long currentPlcId ){
        //    1.1盒子在线
        PlcInfo plcInfo = plcInfoApi.getPlcInfo(currentPlcId);
        Device device = deviceApi.getDevice(plcInfo.device_id);
        if(device.state == 1) {
            pubAction(31,alarmCfgId,device.device_id);
            return true;
        }else{
            return false;
        }
    }
    public boolean pubUpdAlarmCfg(long alarmCfgId ,long currentPlcId){
        //    1.1盒子在线
        PlcInfo plcInfo = plcInfoApi.getPlcInfo(currentPlcId);
        Device device = deviceApi.getDevice(plcInfo.device_id);
        if(device.state == 1) {
            pubAction(30,alarmCfgId,device.device_id);
            return true;
        }else{
            return false;
        }
    }

    public boolean pubDelRealHisCfg(long realHisCfgId , long currentPlcId){
        //    1.1盒子在线
        PlcInfo plcInfo = plcInfoApi.getPlcInfo(currentPlcId);
        Device device = deviceApi.getDevice(plcInfo.device_id);
        if(device.state == 1) {
            pubAction(21,realHisCfgId,device.device_id);
            return true;
        }else{
            return false;
        }
    }

    public boolean pubUpdRealHisCfg(long realHisCfgId , long currentPlcId){
        //    1.1盒子在线
        PlcInfo plcInfo = plcInfoApi.getPlcInfo(currentPlcId);
        Device device = deviceApi.getDevice(plcInfo.device_id);
        if(device.state == 1) {
            pubAction(20,realHisCfgId,device.device_id);
            return true;
        }else{
            return false;
        }
    }

    public static void pubAction (int opType,long opId,long device_id ){
        JSONObject data = new JSONObject();
        data.put("op_type", opType);
        data.put("op_id", opId);
        data.put("device_id",device_id);
        RedisManager.publish(ConstKey.REDIS_GROUP_NAME, ConstKey.REDIS_CHANNEL_UPD_DEVICE_CFG, data.toJSONString());
    }

}
