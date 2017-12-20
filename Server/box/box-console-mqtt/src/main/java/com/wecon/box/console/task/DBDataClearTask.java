package com.wecon.box.console.task;

import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.RealHisCfgDataApi;
import com.wecon.box.console.util.SpringContextHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by whp on 2017/12/8.
 */
public class DBDataClearTask extends Thread {
	private static int sleepTime = 1000 * 30000;
	private static boolean isRuning = false;
	private static final Logger logger = LogManager.getLogger(DBDataClearTask.class);

	public void run() {
		logger.info("DBDataClearTask run start");
		while (true) {
			try {
				if(!isRuning){
					isRuning = true;
					clearHisData();
				}

				sleep(sleepTime);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	/**
	 * 清除盒子多余的历史数据
	 */
	private void clearHisData() {
		logger.info("begin clear history data");
		DeviceApi deviceApi = SpringContextHolder.getBean(DeviceApi.class);
		final RealHisCfgDataApi realHisCfgDataApi = SpringContextHolder.getBean(RealHisCfgDataApi.class);
		try {
			List<Long> deviceIds = deviceApi.getAllDeviceIds();
			if(null != deviceIds){
				for(final Long deviceId : deviceIds){
					ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
					cachedThreadPool.execute(new Runnable() {
						public void run() {
							int delRecord = realHisCfgDataApi.clearHisCfgData(deviceId);
							logger.info("清除盒子【"+deviceId+"】历史数据，删除条数："+delRecord);
						}
					});
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			logger.info("fail clear history data");
		}

		isRuning = false;
		logger.info("end clear history data");
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
