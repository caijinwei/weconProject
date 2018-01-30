package com.wecon.box.console.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.common.redis.RedisManager;
import com.wecon.common.util.CommonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;


/**
 * Created by whp on 2018/1/29.
 */
public class SubscribeTask extends Thread {
	private static final Logger logger = LogManager.getLogger(SubscribeTask.class);

	public void run() {
		/**
		 * 订阅Web端操作配置信息，以便推送该条数据给盒子端
		 */
		RedisManager.subscribe(ConstKey.REDIS_GROUP_NAME, new SubscribeListener(), new String[]{"upd_device_cfg"});
		logger.debug("upd_device_cfg subscribe success");
	}

	class SubscribeListener extends JedisPubSub {

		@Override
		public void onMessage(String channel, String message) {
			logger.debug("upd_device_cfg subscribe callback，channel：" + channel + "message：" + message);
			if(!CommonUtils.isNullOrEmpty(message)){
				BoxNotifyTask notifyTask = new BoxNotifyTask();
				try {
					Map<String, Object> param = JSON.parseObject(message, new TypeReference<Map<String, Object>>() {});
					int opType = Integer.parseInt(param.get("op_type").toString());
					long opId = Long.parseLong(param.get("op_id").toString());
					//通讯口更新
					if(OpTypeOption.UpdPlcInfo.getValue() == opType){
						notifyTask.updatePlcCfgHandle(opId);
					}
					//通讯口删除
					else if(OpTypeOption.DelPlcInfo.getValue() == opType){
						notifyTask.deletePlcCfgHandle(opId);
					}
					//实时历史监控点更新
					else if(OpTypeOption.UpdRealHisCfg.getValue() == opType){
						notifyTask.updateRealHisCfgHandle(opId);
					}
					//实时历史监控点删除
					else if(OpTypeOption.DelRealHisCfg.getValue() == opType){
						notifyTask.deleteAllCfgHandle(opType, opId);
					}
					//报警监控点更新
					else if(OpTypeOption.UpdAlarmCfg.getValue() == opType){
						notifyTask.updateAlarmCfgHandle(opId);
					}
					//报警监控点删除
					else if(OpTypeOption.DelAlarmCfg.getValue() == opType){
						notifyTask.deleteAllCfgHandle(opType, opId);
					}
				}catch (Exception e){
					logger.error("upd_device_cfg subscribe callback error：" + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
}
