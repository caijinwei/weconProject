package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DevFirmApi;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.FileStorageApi;
import com.wecon.box.api.FirmwareApi;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.DevFirm;
import com.wecon.box.entity.Device;
import com.wecon.box.entity.FileStorage;
import com.wecon.box.entity.FirmwareDetail;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.util.Base64Util;
import com.wecon.box.util.ServerMqtt;
import com.wecon.box.util.VerifyUtil;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by caijinw on 2017/9/14.
 */
@RestController
@RequestMapping(value = "dirFirmAction")
public class DirFirmAction {

    @Autowired
    FileStorageApi fileStorageApi;

    @Autowired
    DevFirmApi devFirmApi;

    @Autowired
    FirmwareApi firmwareApi;

    @Autowired
    protected DeviceApi deviceApi;

    @Label("根据设备ID获取固件信息")
    @RequestMapping("/getDirFirmInfoByDevId")
    public Output getDevFirmInfoByDevId(@RequestParam("device_id") long deviceId) {
        if (deviceId <= 0) {
            throw new BusinessException(ErrorCodeOption.GetDevFirm_PramaIsUnFormate_MachineCode.key, ErrorCodeOption.GetDevFirm_PramaIsUnFormate_MachineCode.value);
        }
        DevFirm devFirm = devFirmApi.getDevFirm_device_id(deviceId);
        JSONObject data = new JSONObject();
        data.put("devFirmInfo", devFirm);
        return new Output(data);
    }

    @Label("检查固件更新")
    @RequestMapping("/getLatestFirmVersion")
    public Output getLatestFirmVersion(@RequestParam("dev_model") String dev_model, @RequestParam("localVersionCode") String localVserionCode) {
        List<FirmwareDetail> list = firmwareApi.getFirmwareDetailList(dev_model);

        //获取数据库中  该设备类型对应的最新版本号
        FirmwareDetail result = null;
        String leastVerCode = "";
        String leastVerCodeNum = "";
        long file_id = 0;
        if (null == list || list.size() <= 0) {
            throw new BusinessException(ErrorCodeOption.DivModel_IsError.key, ErrorCodeOption.DivModel_IsError.value);
        }
        for (FirmwareDetail f : list) {
            if (leastVerCodeNum == "" || leastVerCodeNum == null) {
                leastVerCodeNum = VerifyUtil.getVersionNum(f.version_code);
                leastVerCode = f.version_code;
                result = f;
                continue;
            }
            String versionNum = VerifyUtil.getVersionNum(f.version_code);
            if (VerifyUtil.isNewVersion(leastVerCodeNum, versionNum)) {
                leastVerCodeNum = versionNum;
                leastVerCode = f.version_code;
                result = f;
            }
        }
        JSONObject data = new JSONObject();
        String localVersionCodeNum = VerifyUtil.getVersionNum(localVserionCode);
        if (VerifyUtil.isNewVersion(localVersionCodeNum, leastVerCodeNum)) {
            //需要更新
            data.put("isNewVersion", "true");
            data.put("versionCode", leastVerCode);
            data.put("file_id", file_id);
            data.put("fileData", result);
        } else {
            data.put("isNewVersion", "false");
            data.put("versionCode", leastVerCode);
            data.put("file_id", file_id);
        }
        return new Output(data);
    }


    @Label("更新固件")
    @RequestMapping("updateFirmFile")
    public Output updateFirm(@RequestParam("versionName") String versionName, @RequestParam("version_code") String versionCode, @RequestParam("file_id") long file_id, @RequestParam("device_id") Long device_id) {
        if (file_id <= 0) {
            throw new BusinessException(ErrorCodeOption.FileId_Is_Error.key, ErrorCodeOption.FileId_Is_Error.value);
        }
        Device deviceModel = deviceApi.getDevice(device_id);
        if (deviceModel == null) {
            throw new BusinessException(ErrorCodeOption.Device_NotFound.key, ErrorCodeOption.Device_NotFound.value);
        }
        ServerMqtt mqttServer = null;
        long userId = AppContext.getSession().client.userId;
        FileStorage fileStorage = fileStorageApi.getFileStorage(file_id);
        if (fileStorage == null) {
            throw new BusinessException(ErrorCodeOption.FileId_Is_Error.key, ErrorCodeOption.FileId_Is_Error.value);
        }
        FileParm file = new FileParm();
        file.file_md5 = fileStorage.file_md5;
        file.file_name = fileStorage.file_name;
        file.version_code = versionCode;
        file.version_name = versionName;
        JSONObject fileO = new JSONObject();
        file.file_base64 = Base64Util.encode(fileStorage.file_data);
        JSONObject.parseObject(JSONObject.toJSONString(file));


        JSONObject msg = new JSONObject();
        msg.put("act", 20007);
        msg.put("machine_code", deviceModel.machine_code);
        msg.put("data", file);
        msg.put("feedback", 0);

        try {
            if (mqttServer == null) {
                mqttServer = new ServerMqtt("up" + userId);
            }
            mqttServer.message = new MqttMessage();
            mqttServer.message.setQos(1);
            mqttServer.message.setRetained(true);
            //传输的消息体
            mqttServer.message.setPayload(msg.toString().getBytes());
            //pibox/<topic_type>/<machine_code>
            mqttServer.topic11 = mqttServer.client.getTopic(String.format(ConstKey.MQTT_SERVER_TOPICE, deviceModel.machine_code));
            mqttServer.publish(mqttServer.topic11, mqttServer.message);
        /*
        * pibox/cts/<machine_code>/logs
        * */
            mqttServer.client.disconnect();
        } catch (MqttException e) {
            System.out.println(e.getMessage());
            throw new BusinessException(ErrorCodeOption.Mqtt_Transport_Error.key, ErrorCodeOption.Mqtt_Transport_Error.value);
        }
        ;
        return new Output();
    }

    class FileParm {
        public String file_name;
        public String file_md5;
        public String file_base64;
        public String version_code;
        public String version_name;

    }


}
