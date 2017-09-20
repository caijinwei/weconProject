package com.wecon.box.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DriverApi;
import com.wecon.box.api.FileStorageApi;
import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.entity.*;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.util.Base64Util;
import com.wecon.box.util.DbLogUtil;
import com.wecon.box.util.ServerMqtt;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zengzhipeng on 2017/9/16.
 */
@RestController
@RequestMapping(value = "driveraction")
public class DriverAction {

    @Autowired
    protected DriverApi driverApi;

    @Autowired
    protected DbLogUtil dbLogUtil;
    @Autowired
    PlcInfoApi plcInfoApi;
    @Autowired
    FileStorageApi fileStorageApi;
    @Autowired
    DirFirmAction dirFirmAction;

    @Description("保存驱动")
    @RequestMapping(value = "/savedriver")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output saveDriver(@RequestParam("drivers") String drivers) {
        List<HashMap> list = JSON.parseArray(drivers, HashMap.class);
        List<Driver> driverList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Driver tmp = new Driver();
            tmp.driver = list.get(i).get("driver").toString();
            tmp.type = list.get(i).get("type").toString();
            tmp.file_md5 = list.get(i).get("file_md5").toString();
            tmp.file_id = Long.valueOf(list.get(i).get("file_id").toString());
            tmp.driver_id = Long.valueOf(list.get(i).get("driver_id").toString());
            driverList.add(tmp);
        }
        driverApi.batchAddDriver(driverList);
        return new Output();
    }

    @Description("获取列表")
    @RequestMapping(value = "/getdriverlist")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output getDriverList(@RequestParam("type") String type, @RequestParam("driver") String driver, @RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
        Page<Driver> page = driverApi.getDriverList(type, driver, pageIndex, pageSize);
        JSONObject data = new JSONObject();
        data.put("page", page);
        return new Output(data);
    }

    @Description("获取详情")
    @RequestMapping(value = "/getdriver")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output getDriver(@RequestParam("id") Long id) {
        DriverDetail model = driverApi.getDriverDetail(id);
        JSONObject data = new JSONObject();
        data.put("driver", model);
        return new Output(data);
    }

    @Description("删除驱动")
    @RequestMapping(value = "/deldriver")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output deleteDriver(@RequestParam("id") Long id) {
        Driver model = driverApi.getDriver(id);
        driverApi.delDriver(model);
        //<editor-fold desc="操作日志">
//        dbLogUtil.addOperateLog(OpTypeOption.DelFirm, ResTypeOption.Firm, model.firmware_id, model);
        //</editor-fold>
        return new Output();
    }

    @Label("检查更新")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("checkUpdate")
    public Output checkUpdate(@RequestParam("dev_model") String dev_model, @RequestParam("localVersionCode") String localVserionCode, @RequestParam("device_id") long device_id) {
        Object driverData = checkUpdatePlcDriver(device_id).getResult();
        Object firmData = dirFirmAction.getLatestFirmVersion(dev_model, localVserionCode).getResult();
        JSONObject data = new JSONObject();
        data.put("driverData", driverData);
        data.put("firmData", firmData);
        return new Output(data);
    }

    @Label("更新")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("update")
    public Output update(@RequestParam("updateType") Integer updateType, @RequestParam("device_id") long device_id, @RequestParam("machine_code") long machineCode, @RequestParam("versionName") String versionName, @RequestParam("version_code") String versionCode, @RequestParam("file_id") long file_id) {
        if (updateType == 1) {
            updateAllDriver(device_id, machineCode);
        } else if (updateType == 2) {
            dirFirmAction.updateFirm(versionName, versionCode, file_id, machineCode);
        } else if (updateType == 3) {
            updateAllDriver(device_id, machineCode);
            dirFirmAction.updateFirm(versionName, versionCode, file_id, machineCode);
        }
        return new Output();
    }

    @Label("遍历plc检查是否要更新驱动")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("checkUpdatePlcDriver")
    public Output checkUpdatePlcDriver(@RequestParam("device_id") long device_id) {
        if (device_id <= 0) {
            throw new BusinessException(ErrorCodeOption.UpdateDriver_ParamIs_Error.key, ErrorCodeOption.UpdateDriver_ParamIs_Error.value);
        }
        HashMap<String, Driver> driverMap = new HashMap<String, Driver>();
        HashMap<String, PlcInfo> plcInfoMap = new HashMap<String, PlcInfo>();
        List<PlcInfoDetail> plcList = plcInfoApi.getListPlcInfoDetail(device_id);
        for (PlcInfoDetail p : plcList) {
            if (!driverMap.containsKey(p.driver)) {
                //获取driver表的 driver对象
                Driver driver = driverApi.getDriverBydriver(p.driver);
                //file_md5不相同 存入map 准备更新
                if (!driver.file_md5.equals(p.file_md5)) {
                    JSONObject data = new JSONObject();
                    data.put("isUpdate", true);
                    return new Output(data);
                }
            }
        }

        JSONObject data = new JSONObject();
        data.put("isUpdate", false);
        return new Output(data);
    }

    @Label("plc更新驱动")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("updateAllDriver")
    public Output updateAllDriver(@RequestParam("device_id") long device_id, @RequestParam("machine_code") long machineCode) {
        ServerMqtt server = null;
        if (device_id <= 0 || machineCode <= 0) {
            throw new BusinessException(ErrorCodeOption.UpdateDriver_ParamIs_Error.key, ErrorCodeOption.UpdateDriver_ParamIs_Error.value);
        }
        HashMap<String, Driver> driverMap = new HashMap<String, Driver>();
        HashMap<String, PlcInfo> plcInfoMap = new HashMap<String, PlcInfo>();

        List<PlcInfoDetail> plcList = plcInfoApi.getListPlcInfoDetail(device_id);
        for (PlcInfoDetail p : plcList) {
            if (!driverMap.containsKey(p.driver)) {
                //获取driver表的 driver对象
                Driver driver = driverApi.getDriverBydriver(p.driver);
                //file_md5不相同 存入map 准备更新
                if (!driver.file_md5.equals(p.file_md5)) {
                    driverMap.put(driver.driver, driver);
                    plcInfoMap.put("plc_" + driver.driver, p);
//                    driverMap.put("plc_"+driver.driver,p);
                }
            }
        }
        //这边需要做批量  优化
        for (Map.Entry<String, Driver> driverEntry : driverMap.entrySet()) {
            FileStorage file = fileStorageApi.getFileStorage(driverEntry.getValue().driver_id);
            if (file != null) {
                //mqtt消息格式
                DriverFileParam param = new DriverFileParam();
                param.file_base64 = Base64Util.encode(file.file_data);
                param.file_name = file.file_name;
                param.file_md5 = file.file_md5;
                //取plcMap下的对应的type
                param.driver_type = plcInfoMap.get("plc_" + driverEntry.getKey()).type;
                JSONObject data = new JSONObject();
                data.put("act", 2008);
                data.put("machine_code", machineCode);
                data.put("data", param);
                data.put("feedback", 1);

                try {
                    if (server == null) {
                        server = new ServerMqtt("UD_" + AppContext.getSession().client.userId);
                    }
                    server.message = new MqttMessage();
                    server.message.setQos(1);
                    server.message.setRetained(true);
                    server.message.setPayload(data.toString().getBytes());
                    server.topic11 = server.client.getTopic("pibox/stc/" + machineCode);
                    server.publish(server.topic11, server.message);
                } catch (MqttException e) {
                    throw new BusinessException(ErrorCodeOption.Mqtt_Transport_Error.key, ErrorCodeOption.Mqtt_Transport_Error.value);
                }
            }
        }
        try {
            server.client.disconnect();
        } catch (MqttException e) {
            throw new BusinessException(ErrorCodeOption.Mqtt_Disconnect_Error.key, ErrorCodeOption.Mqtt_Disconnect_Error.value);
        }


        return new Output();
    }
}

class DriverFileParam {
    public String file_name;
    public String file_md5;
    public String file_base64;
    public String driver_type;
}
