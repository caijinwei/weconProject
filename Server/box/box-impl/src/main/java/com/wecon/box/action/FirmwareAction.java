package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.FirmwareApi;
import com.wecon.box.entity.Firmware;
import com.wecon.box.entity.FirmwareDetail;
import com.wecon.box.entity.Page;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.box.param.FirmwareParam;
import com.wecon.box.util.DbLogUtil;
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

    @Autowired
    protected DbLogUtil dbLogUtil;

    @Description("保存固件")
    @RequestMapping(value = "/savefirmware")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output saveFirmware(@Valid FirmwareParam input) {
        Firmware model = paramConvertFirmware(input);

        if (input.firmware_id > 0) {
            Firmware modelOld = firmwareApi.getFirmware(input.firmware_id);
            firmwareApi.updateFirmware(model);
            //<editor-fold desc="操作日志">
            dbLogUtil.updOperateLog(OpTypeOption.AddFirm, ResTypeOption.Firm, model.firmware_id, modelOld, model);
            //</editor-fold>
        } else {
            firmwareApi.addFirmware(model);
            //<editor-fold desc="操作日志">
            dbLogUtil.addOperateLog(OpTypeOption.UpdFirm, ResTypeOption.Firm, model.firmware_id, model);
            //</editor-fold>
        }
        JSONObject data = new JSONObject();
        data.put("id", model.firmware_id);
        return new Output(data);
    }

    @Description("获取固件详情")
    @RequestMapping(value = "/getfirmware")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output getFirmware(@RequestParam("id") Long id) {
        Firmware model = firmwareApi.getFirmwareDetail(id);
        JSONObject data = new JSONObject();
        data.put("firmware", model);
        return new Output(data);
    }

    @Description("获取固件列表")
    @RequestMapping(value = "/getfirmwarelist")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output getFirmwareList(@RequestParam("dev_model") String dev_model, @RequestParam("firm_info") String firm_info, @RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
        Page<FirmwareDetail> page = firmwareApi.getFirmwareDetailList(dev_model, firm_info, pageIndex, pageSize);
        JSONObject data = new JSONObject();
        data.put("page", page);
        return new Output(data);
    }

    private Firmware paramConvertFirmware(FirmwareParam input) {
        Firmware model = new Firmware();
        if (input.firmware_id > 0) {
            model = firmwareApi.getFirmware(input.firmware_id);
            if (model == null) {
                throw new BusinessException(ErrorCodeOption.FirmwareNotExist.key, ErrorCodeOption.FirmwareNotExist.value);
            }
        }
        model.file_id = input.file_id;
        model.version_name = input.version_name;
        model.version_code = input.version_code;
        model.description = input.description;
        model.dev_model = input.dev_model;
        model.firm_info = input.firm_info;
        return model;
    }
}
