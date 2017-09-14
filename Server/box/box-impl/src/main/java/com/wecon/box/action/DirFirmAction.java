package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DevFirmApi;
import com.wecon.box.entity.DevFirm;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2017/9/14.
 */
@RestController
@RequestMapping(value = "dirFirmAction")
public class DirFirmAction {

    @Autowired
    DevFirmApi devFirmApi;

    @Label("根据设备ID获取固件信息")
    @RequestMapping("/getDirFirmInfoByDevId")
    public Output getDevFirmInfoByDevId(@RequestParam("device_id") long deviceId) {

        if (deviceId <= 0) {
            throw new BusinessException(ErrorCodeOption.GetDevFirm_PramaIsUnFormate_MachineCode.key,ErrorCodeOption.GetDevFirm_PramaIsUnFormate_MachineCode.value);
        }
        DevFirm devFirm =devFirmApi.getDevFirm_device_id(deviceId);
        JSONObject data=new JSONObject();
        data.put("devFirmInfo",devFirm);
        return new Output(data);
    }
}
