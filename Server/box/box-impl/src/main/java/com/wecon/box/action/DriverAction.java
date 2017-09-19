package com.wecon.box.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DriverApi;
import com.wecon.box.entity.*;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.box.param.DriverBatchParam;
import com.wecon.box.param.DriverParam;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        //<editor-fold desc="操作日志">
        dbLogUtil.addOperateLog(OpTypeOption.BatchDriver, ResTypeOption.Driver, 0, driverList);
        //</editor-fold>
        return new Output();
    }

    @Description("保存驱动(单个)")
    @RequestMapping(value = "/savedriver1")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output saveDriverOne(@RequestParam("drivers") String drivers) {
        List<HashMap> list = JSON.parseArray(drivers, HashMap.class);

        Driver model = new Driver();
        model.driver_id = Long.valueOf(list.get(0).get("driver_id").toString());
        if (model.driver_id > 0) {
            model = driverApi.getDriver(model.driver_id);
            if (model == null) {
                throw new BusinessException(ErrorCodeOption.DriverNotExist.key, ErrorCodeOption.DriverNotExist.value);
            }
        }
        model.driver = list.get(0).get("driver").toString();
        model.type = list.get(0).get("type").toString();
        model.file_md5 = list.get(0).get("file_md5").toString();
        model.file_id = Long.valueOf(list.get(0).get("file_id").toString());
        model.description = list.get(0).get("description").toString();
        if (model.driver_id > 0) {
            Driver modelOld = driverApi.getDriver(model.driver_id);
            driverApi.updateDriver(model);
            //<editor-fold desc="操作日志">
            dbLogUtil.updOperateLog(OpTypeOption.UpdDriver, ResTypeOption.Driver, model.driver_id, modelOld, model);
            //</editor-fold>
        } else {
            List<Driver> driverList = new ArrayList<>();
            driverList.add(model);
            driverApi.batchAddDriver(driverList);
            //<editor-fold desc="操作日志">
            dbLogUtil.addOperateLog(OpTypeOption.BatchDriver, ResTypeOption.Driver, model.driver_id, driverList);
            //</editor-fold>
        }
        Driver modelNow = driverApi.getDriver(list.get(0).get("type").toString());
        JSONObject data = new JSONObject();
        data.put("id", modelNow.driver_id);
        return new Output(data);
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
        dbLogUtil.addOperateLog(OpTypeOption.DelDriver, ResTypeOption.Driver, model.driver_id, model);
        //</editor-fold>
        return new Output();
    }
}
