package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.FirmwareApi;
import com.wecon.box.entity.Firmware;
import com.wecon.box.param.FirmwareParam;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by zengzhipeng on 2017/9/14.
 */
@RestController
@RequestMapping(value = "firmwareaction")
public class FirmwareAction {
    @Autowired
    private FirmwareApi firmwareApi;

    @Description("保存固件")
    @RequestMapping(value = "/savefirmware")
    @WebApi(forceAuth = false, master = true, authority = {"0"})
    public Output saveFirmware(@Valid FirmwareParam input) {
        Firmware model = paramConvertFirmware(input);

        if (model.firmware_id > 0) {
            firmwareApi.updateFirmware(model);
        } else {
            firmwareApi.addFirmware(model);
        }
        JSONObject data = new JSONObject();
        data.put("id", model.firmware_id);
        return new Output(data);
    }

    @Description("获取固件详情")
    @RequestMapping(value = "/getfirmware")
    @WebApi(forceAuth = false, master = true, authority = {"0"})
    public Output getFirmware(@RequestParam("id") Long id) {
        Firmware model = firmwareApi.getFirmwareDetail(id);
        JSONObject data = new JSONObject();
        data.put("firmware", model);
        return new Output(data);
    }

    private Firmware paramConvertFirmware(FirmwareParam input) {
        Firmware model = new Firmware();
        model.firmware_id = input.firmware_id;
        model.file_id = input.file_id;
        model.version_name = input.version_name;
        model.version_code = input.version_code;
        model.description = input.description;
        model.dev_model = input.dev_model;
        model.firm_info = input.firm_info;
        return model;
    }
}
