package com.wecon.box.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.*;
import com.wecon.box.entity.*;
import com.wecon.box.enums.DataTypeOption;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.box.filter.*;
import com.wecon.box.util.DbLogUtil;
import com.wecon.box.util.DeviceUseQuerier;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Client;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by caijinw on 2017/8/8.
 */
@RestController
@RequestMapping(value = "baseInfoAction")
public class DeviceAction {
    @Autowired
    DeviceApi deviceApi;

    @Autowired
    protected DbLogUtil dbLogUtil;

    @Autowired
    AccountApi accountApi;

    @Autowired
    DevBindUserApi devBindUserApi;

    @Autowired
    AccountDirRelApi accountDirRelApi;
    @Autowired
    private AccountDirApi accountDirApi;
    @Autowired
    AlarmCfgApi alarmCfgApi;
    @Autowired
    RealHisCfgApi realHisCfgApi;
    @Autowired
    private RedisPiBoxApi redisPiBoxApi;
    @Autowired
    private AlarmTriggerApi alarmTriggerApi;
    @Autowired
    private RealHisCfgDataApi realHisCfgDataApi;
    @Autowired
    private AlarmCfgDataApi alarmCfgDataApi;

    /**
     * 盒子基本信息的展示
     *
     * @param device_id
     * @return
     */
    @Label("盒子基本信息展示")
    @RequestMapping(value = "showBaseInfo")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output showBaseInfo(@RequestParam("device_id") Integer device_id) {

        Client client = AppContext.getSession().client;
        long account_id = client.userId;
        Integer userType = client.userInfo.getUserType();
        //验证是否拥有该盒子
        devBindUserApi.isRecord(device_id, AppContext.getSession().client.userId);

        Device device = deviceApi.getDevice(device_id);
        DeviceUse deviceUse = deviceApi.getDeviceUse(device_id);
        JSONObject data = new JSONObject();
        data.put("device", device);
        data.put("userType", userType);
        data.put("deviceUse",deviceUse);
        if (device == null || data.size() == 0) {
            throw new BusinessException(ErrorCodeOption.PiBoxDevice_IsNot_Found.key,
                    ErrorCodeOption.PiBoxDevice_IsNot_Found.value);
        }
        return new Output(data);
    }

    @Label("删除PIBox")
    @RequestMapping(value = "deletePIBox")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output deletePIBox(@RequestParam("device_id") Integer device_id) {
        if (device_id <= 0) {
            throw new BusinessException(ErrorCodeOption.DeviceId_Is_Error.key, ErrorCodeOption.DeviceId_Is_Error.value);
        }
        //验证是否拥有该盒子
        devBindUserApi.isRecord(device_id, AppContext.getSession().client.userId);

        long accountId = AppContext.getSession().client.userId;
        deviceApi.unbindDevice((int) accountId, device_id);
        // <editor-fold desc="操作日志">
        dbLogUtil.addOperateLog(OpTypeOption.UnBindDevice, ResTypeOption.Device, device_id,
                deviceApi.getDevice(device_id));
        // </editor-fold>
        return new Output();
    }

    @Label("获取所有盒子行业应用的option")
    @RequestMapping(value = "getDeviceUseOptions")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output getDeviceUseOptions(){
        JSONObject data=new JSONObject();
        data.put("data",DeviceUseQuerier.allDeviceUses);
        return new Output(data);
    }

    /*
     * 测试 没有用户登入等验证 直接先传入user_id
     */
    @Label("绑定盒子")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping(value = "/boundBox")
    public Output boundBox(@RequestParam("machine_code") String machine_code, @RequestParam("password") String password,
                           @RequestParam("name") String name, @RequestParam("acc_dir_id") Integer acc_dir_id,
                           @RequestParam("deviceUseCode")Integer deviceUseCode,
                           @RequestParam("deviceUseName")String deviceUseName) {


        /*
         * 验证是否该盒子是否存在
		 */
        Device device = deviceApi.getDevice(machine_code);
        if (device == null) {
            throw new BusinessException(ErrorCodeOption.Device_NotFound.key, ErrorCodeOption.Device_NotFound.value);
        } else if (!device.password.equals(password)) {
            throw new BusinessException(ErrorCodeOption.Device_Pwd_Error.key, ErrorCodeOption.Device_Pwd_Error.value);
        }
        long device_id = device.device_id;
		/*
		 * 该设备没有被别的用户绑定过
		 */
        if (devBindUserApi.findByDevId(device_id) != 0) {
            throw new BusinessException(ErrorCodeOption.Device_AlreadyBind.key,
                    ErrorCodeOption.Device_AlreadyBind.value);
        }
        //没有选择分组操作
        if (acc_dir_id == 0) {
            throw new BusinessException(ErrorCodeOption.Device_Bind_NotDir.key, ErrorCodeOption.Device_Bind_NotDir.value);
        }
        deviceApi.boundDevice(device_id, name, acc_dir_id);


         /*
        * 行业类型保存
        * */
        if(CommonUtils.isNotNull(deviceUseCode)){
            if(deviceUseCode!=999){
                DeviceUse deviceUse=new DeviceUse();
                deviceUse.useName=DeviceUseQuerier.allDeviceUsesMap.get(deviceUseCode);
                deviceUse.useCode=deviceUseCode;
                deviceUse.deviceId=device.device_id;
                deviceApi.updateDeviceUse(deviceUse);
            }else{
                DeviceUse deviceUse=new DeviceUse();
                deviceUse.otherUseName=deviceUseName;
                deviceUse.useCode=deviceUseCode;
                deviceUse.deviceId=device.device_id;
                deviceApi.updateDeviceUse(deviceUse);
            }
        }


        // <editor-fold desc="操作日志">
        dbLogUtil.addOperateLog(OpTypeOption.BindDevice, ResTypeOption.Device, device_id,
                deviceApi.getDevice(device_id));
        // </editor-fold>
        return new Output();
    }

    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @Description("根据用户获取分组下的盒子")
    @RequestMapping(value = "/getBoxGroup")
    public Output getBoxGroup() {
        Client client = AppContext.getSession().client;
        JSONObject json = new JSONObject();
        JSONArray devicearr = null;
        JSONArray allarr = new JSONArray();
        JSONObject data = null;
        JSONObject devicedata = null;
        List<Device> deviceList = null;
        List<AccountDir> accountDirList = accountDirApi.getAccountDirList(client.userId, 0);

        if (accountDirList == null || accountDirList.size() < 1) {
            // 没有盒子分组默认创建
            AccountDir accountDir = new AccountDir();
            accountDir.account_id = client.userId;
            accountDir.type = 0;
            accountDir.name = "默认组";
            long id = accountDirApi.addAccountDir(accountDir);
            if (id > 0) {
                accountDirList = accountDirApi.getAccountDirList(client.userId, 0);

            } else {
                throw new BusinessException(ErrorCodeOption.Get_DeviceList_Error.key,
                        ErrorCodeOption.Get_DeviceList_Error.value);
            }
        }

        for (int i = 0; i < accountDirList.size(); i++) {
            AccountDir accountDir = accountDirList.get(i);
            data = new JSONObject();
            devicearr = new JSONArray();
            data.put("accountdirId", accountDir.id);
            data.put("accountdirName", accountDir.name);

            deviceList = deviceApi.getDeviceList(client.userId, accountDir.id);
            if (deviceList != null) {
                for (int j = 0; j < deviceList.size(); j++) {

                    devicedata = new JSONObject();

                    Device device = deviceList.get(j);
                    devicedata.put("deviceId", device.device_id);
                    devicedata.put("deviceMap",device.map);
                    devicedata.put("deviceName", device.name);
                    devicedata.put("deviceState", device.state);
                    devicearr.add(devicedata);

                }
                data.put("deviceList", devicearr);
            }

            allarr.add(data);

        }

        json.put("allData", allarr);

        return new Output(json);
    }

    @Label("用户拖拽，更新用户分组")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping(value = "dragToUpdateDir")
    public Output dragToUpdateDir(@RequestParam("target_acc_dir_id") long targetAccDirId,
                                  @RequestParam("target_ref_id") long targetRefId, @RequestParam("from_acc_dir_id") long fromAccDirId,
                                  @RequestParam("from_ref_id") long fromRefId) {
        AccountDirRel newAccDirRel = new AccountDirRel();
        newAccDirRel.acc_dir_id = targetAccDirId;
        newAccDirRel.ref_id = targetRefId;
        AccountDirRel oldAccDirRel = new AccountDirRel();
        oldAccDirRel.acc_dir_id = fromAccDirId;
        oldAccDirRel.ref_id = fromRefId;
        accountDirRelApi.updateAccountDirRel(newAccDirRel, oldAccDirRel);
        // <editor-fold desc="操作日志">
        AccountDir fromAccDir = accountDirApi.getAccountDir(fromAccDirId);
        long device_id = fromAccDir.device_id;
        dbLogUtil.updOperateLog(OpTypeOption.DragDeviceDir, ResTypeOption.Device, device_id, fromAccDir,
                accountDirApi.getAccountDir(targetAccDirId));
        // </editor-fold>
        return new Output();
    }

    @Label("超级管理员查看所有device盒子")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    @RequestMapping(value = "showAllDeviceDir")
    public Output showAllDeviceDir(@RequestParam("state")Integer state,@RequestParam("machine_code")String machine_code,@RequestParam("accountId")long accountId,@RequestParam("device_id")String device_id,@RequestParam("bind_state")Integer bind_state,@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize){
        DeviceSearchFilter filter =new DeviceSearchFilter();
        filter.state=state;
        filter.machine_code=machine_code;
        filter.accountId=accountId;
        filter.device_id=Long.parseLong(device_id);
        filter.bind_state=bind_state;
        JSONObject data=new JSONObject();
        data.put("page",deviceApi.getAllDeviceByFilter(filter,pageNum,pageSize));
        return new Output(data);
    }

    @Label("超级管理员查看所有盒子的配置")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    @RequestMapping(value = "showAllSetting")
    public Output showAllSetting(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
                                 @RequestParam("machine_code") String machine_code, @RequestParam("settingtype") String settingtype,
                                 @RequestParam("bindstate") String bindstate) {
        JSONObject json = new JSONObject();
        if (!CommonUtils.isNullOrEmpty(machine_code) && !CommonUtils.isNullOrEmpty(bindstate)
                && !CommonUtils.isNullOrEmpty(settingtype)) {
            Device device = deviceApi.getDevice(machine_code);
            if (device != null) {
                if (Integer.valueOf(settingtype) != 2) {
                    RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
                    realHisCfgFilter.addr_type = -1;
                    realHisCfgFilter.state = -1;
                    if (Integer.valueOf(settingtype) == 0) {
                        realHisCfgFilter.data_type = 0;
                    } else {
                        realHisCfgFilter.data_type = 1;

                    }
                    realHisCfgFilter.his_cycle = -1;
                    realHisCfgFilter.state = -1;
                    realHisCfgFilter.device_id = device.device_id;
                    realHisCfgFilter.bind_state = Integer.valueOf(bindstate);
                    Page<RealHisCfgDevice> realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter, pageNum,
                            pageSize);
                    for (int i = 0; i < realHisCfgDeviceList.getList().size(); i++) {
                        RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.getList().get(i);
                        realHisCfgDevice.data_value = DataTypeOption.getDataTypeValue(realHisCfgDevice.data_id);

                    }
                    json.put("allSettingData", realHisCfgDeviceList);

                } else {
                    Page<AlarmCfgTrigger> listalrmCfgTrigger = alarmCfgApi.getAlarmList(device.device_id,
                            Integer.valueOf(bindstate), pageNum, pageSize);
                    if (listalrmCfgTrigger.getList().size() > 0) {
                        for (int i = 0; i < listalrmCfgTrigger.getList().size(); i++) {
                            AlarmCfgTrigger alarmCfgTrigger = listalrmCfgTrigger.getList().get(i);
                            alarmCfgTrigger.data_value = DataTypeOption.getDataTypeValue(alarmCfgTrigger.data_id);
                            alarmCfgTrigger.id = alarmCfgTrigger.alarmcfg_id;
                            AlarmTriggerFilter filter = new AlarmTriggerFilter();
                            filter.alarmcfg_id = alarmCfgTrigger.alarmcfg_id;
                            filter.type = -1;
                            List<AlarmTrigger> listAlarmTrigger = alarmTriggerApi.getAlarmTrigger(filter);
                            if (alarmCfgTrigger.addr_type == 0) {// 位地址

                                alarmCfgTrigger.triggerValue = listAlarmTrigger.get(0).value;

                            } else {
                                if (alarmCfgTrigger.condition_type == 0) {// 一个条件
                                    AlarmTrigger alarmTrigger = listAlarmTrigger.get(0);
                                    String typevalue = "";
                                    switch (alarmTrigger.type) {
                                        case 0:
                                            typevalue = "等于";
                                            break;
                                        case 1:
                                            typevalue = "不等于";
                                            break;
                                        case 2:
                                            typevalue = "大于";
                                            break;
                                        case 3:
                                            typevalue = "大于等于";
                                            break;
                                        case 4:
                                            typevalue = "小于";
                                            break;
                                        case 5:
                                            typevalue = "小于等于";
                                            break;

                                        default:
                                            break;
                                    }
                                    alarmCfgTrigger.triggerValue = "值 " + " " + typevalue
                                            + listAlarmTrigger.get(0).value;
                                } else {// 与 或
                                    StringBuffer buffer = new StringBuffer();
                                    buffer.append("值");
                                    String typevalue = "";
                                    switch (listAlarmTrigger.get(0).type) {
                                        case 0:
                                            typevalue = "等于";
                                            break;
                                        case 1:
                                            typevalue = "不等于";
                                            break;
                                        case 2:
                                            typevalue = "大于";
                                            break;
                                        case 3:
                                            typevalue = "大于等于";
                                            break;
                                        case 4:
                                            typevalue = "小于";
                                            break;
                                        case 5:
                                            typevalue = "小于等于";
                                            break;

                                        default:
                                            break;
                                    }
                                    buffer.append(" ");
                                    buffer.append(typevalue);
                                    buffer.append(listAlarmTrigger.get(0).value);
                                    buffer.append(" ");
                                    if (alarmCfgTrigger.condition_type == 1) {
                                        buffer.append("与");
                                    } else {
                                        buffer.append("或");
                                    }
                                    switch (listAlarmTrigger.get(1).type) {
                                        case 0:
                                            typevalue = "等于";
                                            break;
                                        case 1:
                                            typevalue = "不等于";
                                            break;
                                        case 2:
                                            typevalue = "大于";
                                            break;
                                        case 3:
                                            typevalue = "大于等于";
                                            break;
                                        case 4:
                                            typevalue = "小于";
                                            break;
                                        case 5:
                                            typevalue = "小于等于";
                                            break;

                                        default:
                                            break;
                                    }
                                    buffer.append(" ");
                                    buffer.append("值");
                                    buffer.append(" ");
                                    buffer.append(typevalue);
                                    buffer.append(listAlarmTrigger.get(1).value);
                                    alarmCfgTrigger.triggerValue = buffer.toString();
                                }
                            }

                            alarmCfgTrigger.listAlarmTrigger = alarmTriggerApi.getAlarmTrigger(filter);
                        }

                    }
                    json.put("allSettingData", listalrmCfgTrigger);

                }

            }

        }

        return new Output(json);

    }

    @Label("超级管理员查看所有盒子的数据")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    @RequestMapping(value = "showAllData")
    public Output showAllData(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
                              @RequestParam("datatype") String datatype, @RequestParam("settingId") String settingId,
                              @RequestParam("start_date") String start_date, @RequestParam("end_date") String end_date) {
        JSONObject json = new JSONObject();
        if (!CommonUtils.isNullOrEmpty(settingId) && !CommonUtils.isNullOrEmpty(datatype)) {

            if (Integer.parseInt(datatype) == 0) {// 实时数据

                /** 获取实时数据配置信息 **/
                RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
                /** 管理 **/
                realHisCfgFilter.id = Long.parseLong(settingId);
                realHisCfgFilter.addr_type = -1;
                realHisCfgFilter.data_type = 0;
                realHisCfgFilter.his_cycle = -1;
                realHisCfgFilter.state = -1;
                realHisCfgFilter.bind_state = -1;
                Page<RealHisCfgDevice> realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter, pageNum,
                        pageSize);

                // 如果该账号下无实时数据配置文件直接返回空
                if (realHisCfgDeviceList != null && realHisCfgDeviceList.getList().size() > 0) {
                    for (int i = 0; i < realHisCfgDeviceList.getList().size(); i++) {
                        RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.getList().get(i);
                        String device_machine = realHisCfgDevice.machine_code;
                        // 通过机器码去redis中获取数据
                        RedisPiBoxActData redisPiBoxActData = redisPiBoxApi.getRedisPiBoxActData(device_machine);
                        if (redisPiBoxActData != null) {
                            List<PiBoxCom> act_time_data_list = redisPiBoxActData.act_time_data_list;
                            realHisCfgDevice.monitor_time = redisPiBoxActData.time;
                            for (int j = 0; j < act_time_data_list.size(); j++) {
                                PiBoxCom piBoxCom = act_time_data_list.get(j);

                                if (realHisCfgDevice.plc_id == Long.parseLong(piBoxCom.com)) {

                                    List<PiBoxComAddr> addr_list = piBoxCom.addr_list;
                                    for (int x = 0; x < addr_list.size(); x++) {
                                        PiBoxComAddr piBoxComAddr = addr_list.get(x);

                                        if (realHisCfgDevice.id == Long.parseLong(piBoxComAddr.addr_id)) {
                                            realHisCfgDevice.re_state = piBoxComAddr.state;
                                            realHisCfgDevice.re_value = piBoxComAddr.value;

                                        }

                                    }

                                }

                            }
                        }
                    }
                }
                json.put("allData", realHisCfgDeviceList);

            } else if (Integer.parseInt(datatype) == 1) {// 历史数据
                Page<RealHisCfgData> realHisCfgDataList = new Page<RealHisCfgData>(pageNum, pageSize, 0);

                RealHisCfgDataFilter realHisCfgDataFilter = new RealHisCfgDataFilter();

                realHisCfgDataFilter.real_his_cfg_id = Long.parseLong(settingId);
                realHisCfgDataFilter.start_date = start_date;
                realHisCfgDataFilter.end_date = end_date;
                realHisCfgDataFilter.state = -1;

                realHisCfgDataList = realHisCfgDataApi.getRealHisCfgDataList(realHisCfgDataFilter, pageNum, pageSize);

                json.put("allData", realHisCfgDataList);

            } else if (Integer.parseInt(datatype) == 2) {// 报警数据
                Page<AlarmCfgDataAlarmCfg> alarmCfgDataAlarmCfgList = null;
                AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
                filter.alarm_cfg_id = Long.parseLong(settingId);
                filter.start_date = start_date;
                filter.end_date = end_date;
                filter.state = -1;
                alarmCfgDataAlarmCfgList = alarmCfgDataApi.getRealHisCfgDataList(filter, pageNum, pageSize);
                json.put("allData", alarmCfgDataAlarmCfgList);

            }

        }

        return new Output(json);

    }

    @Label("修改盒子别名和备注,地图")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("chgPiboxInFoName")
    public Output chgPiboxInFoName(@RequestParam("deviceId") Integer deviceId,
                                   @RequestParam("piBoxName") String piBoxName, @RequestParam("remark") String remark,
                                   @RequestParam("deviceUseCode") Integer deviceUseCode,
                                   @RequestParam("deviceUseName") String deviceUseName,
                                   @RequestParam("maxHisDataCount")Integer maxHisDataCount,
                                   @RequestParam("map") String map) {
        Device device = deviceApi.getDevice(deviceId);
        Device oldDevice = device;

        //验证是否拥有该盒子
        devBindUserApi.isRecord(deviceId, AppContext.getSession().client.userId);

        if (null == deviceId || null == device) {
            throw new BusinessException(ErrorCodeOption.Device_NotFound.key, ErrorCodeOption.Device_NotFound.value);
        }
        if (CommonUtils.isNotNull(piBoxName)) {
            device.name = piBoxName;
        }
        if (CommonUtils.isNotNull(remark)) {
            device.remark = remark;
        }
        if (CommonUtils.isNotNull(map)) {
            device.map = map;
        }
        if(CommonUtils.isNotNull(maxHisDataCount)){
            device.max_his_data_count=maxHisDataCount;
        }
        if(CommonUtils.isNotNull(deviceUseCode)){
            if(deviceUseCode!=999){
                DeviceUse deviceUse=new DeviceUse();
                deviceUse.useName=DeviceUseQuerier.allDeviceUsesMap.get(deviceUseCode);
                deviceUse.useCode=deviceUseCode;
                deviceUse.deviceId=deviceId;
                deviceApi.updateDeviceUse(deviceUse);
            }else{
                DeviceUse deviceUse=new DeviceUse();
                deviceUse.otherUseName=deviceUseName;
                deviceUse.useCode=deviceUseCode;
                deviceUse.deviceId=deviceId;
                deviceApi.updateDeviceUse(deviceUse);
            }
        }

        deviceApi.updateDevice(device);
        // <editor-fold desc="操作日志">
        dbLogUtil.updOperateLog(OpTypeOption.UpdateDeviceInfo, ResTypeOption.Device, deviceId, oldDevice, device);
        // </editor-fold>
        return new Output();
    }
}