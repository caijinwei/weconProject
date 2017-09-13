package com.wecon.box.action.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.*;
import com.wecon.box.entity.*;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.AlarmCfgDataFilter;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Client;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by whp on 2017/8/8.
 */
@Label("获取业务数据")
@RestController
public class BusinessDataAction {
    @Autowired
    private RedisPiBoxApi redisPiBoxApi;
    @Autowired
    private RealHisCfgApi realHisCfgApi;
    @Autowired
    private AlarmCfgDataApi alarmCfgDataApi;
    @Autowired
    private RealHisCfgDataApi realHisCfgDataApi;
    @Autowired
    private DeviceApi deviceApi;
    @Autowired
    private AccountDirApi accountDirApi;
    /**
     * 获取实时数据
     * @param param
     * @return
     */
    @RequestMapping("data/real")
    @WebApi(forceAuth = true, master = true)
    public Output getRealData(@Valid BusinessDataParam param) {
        Client client = AppContext.getSession().client;
        RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
        Page<RealHisCfgDevice> realHisCfgDevicePage = null;
        Map<String, Object> bParams = new HashMap<String, Object>();
        if(param.boxId != 0){
            bParams.put("boxId", param.boxId);
        }
        if(param.groupId != 0){
            bParams.put("groupId", param.groupId);
        }
        /** 管理者账号 **/
        if (client.userInfo.getUserType() == 1) {
            realHisCfgFilter.data_type = 0;
            realHisCfgFilter.account_id = client.userId;
            realHisCfgDevicePage = realHisCfgApi.getRealHisCfgDevicePage(realHisCfgFilter, bParams, param.pageIndex, param.pageSize);
        }
        /** 视图账号 **/
        else if (client.userInfo.getUserType() == 2) {
            ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();
            viewAccountRoleFilter.view_id = client.userId;
            viewAccountRoleFilter.data_type = 0;
            realHisCfgDevicePage = realHisCfgApi.getRealHisCfgDevicePage(viewAccountRoleFilter, bParams, param.pageIndex, param.pageSize);
        }
        List<RealHisCfgDevice> realHisCfgDeviceList = realHisCfgDevicePage.getList();
        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();

        if (realHisCfgDeviceList == null || realHisCfgDeviceList.size() < 1) {
            return new Output(json);
        }
        for (int i = 0; i < realHisCfgDeviceList.size(); i++) {
            RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.get(i);
            String device_machine = realHisCfgDevice.machine_code;
            // 通过机器码去redis中获取数据
            RedisPiBoxActData redisPiBoxActData = redisPiBoxApi.getRedisPiBoxActData(device_machine);
            List<PiBoxCom> actTimeDataList = null == redisPiBoxActData ? null : redisPiBoxActData.act_time_data_list;

            JSONObject data = new JSONObject();
            data.put("id", realHisCfgDevice.id);
            data.put("state", realHisCfgDevice.state);
            data.put("monitorName", realHisCfgDevice.name);
            data.put("number", 0);
            if(null != actTimeDataList){
                for (int j = 0; j < actTimeDataList.size(); j++) {
                    PiBoxCom piBoxCom = actTimeDataList.get(j);
                    if (Long.parseLong(piBoxCom.com) == realHisCfgDevice.plc_id) {
                        List<PiBoxComAddr> addrList = piBoxCom.addr_list;
                        for (int k = 0; k < addrList.size(); k++) {
                            PiBoxComAddr piBoxComAddr = addrList.get(k);
                            if (Long.parseLong(piBoxComAddr.addr_id) == realHisCfgDevice.id) {
                                data.put("state", piBoxComAddr.state);
                                data.put("number", piBoxComAddr.value);
                            }
                        }
                    }
                }
            }
            arr.add(data);
        }
        json.put("list", arr);
        return new Output(json);
    }

    /**
     * 获取历史数据
     * @param param
     * @return
     */
    @RequestMapping("data/history")
    @WebApi(forceAuth = true, master = true)
    public Output getHistoryData(@Valid BusinessDataParam param) {
        Client client = AppContext.getSession().client;
        RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
        Page<Map<String, Object>> realHisCfgDataPage = null;
        Map<String, Object> bParams = new HashMap<String, Object>();
        if(param.boxId != 0 && param.boxId != -100 && param.boxId != -200){
            bParams.put("boxId", param.boxId);
        }
        if(param.monitorId != 0){
            bParams.put("monitorId", param.monitorId);
        }
        /** 管理者账号 **/
        if (client.userInfo.getUserType() == 1) {
            realHisCfgFilter.data_type = 1;
            realHisCfgFilter.account_id = client.userId;
            realHisCfgDataPage = realHisCfgDataApi.getRealHisCfgDataPage(realHisCfgFilter, bParams, param.pageIndex, param.pageSize);
        }
        /** 视图账号 **/
        else if (client.userInfo.getUserType() == 2) {
            ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();
            viewAccountRoleFilter.view_id = client.userId;
            viewAccountRoleFilter.data_type = 1;
            realHisCfgDataPage = realHisCfgDataApi.getRealHisCfgDataPage(viewAccountRoleFilter, bParams, param.pageIndex, param.pageSize);
        }
        List<Map<String, Object>> realHisCfgDataList = realHisCfgDataPage.getList();
        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();
        if (realHisCfgDataList == null || realHisCfgDataList.size() < 1) {
            return new Output(json);
        }
        for(Map<String, Object> row : realHisCfgDataList){
            JSONObject data = new JSONObject();
            data.put("monitorName", row.get("monitorName"));
            data.put("number", row.get("number"));
            data.put("monitorTime", row.get("monitorTime"));
            arr.add(data);
        }

        json.put("list", arr);
        return new Output(json);
    }

    /**
     * 获取报警数据
     * @param param
     * @return
     */
    @RequestMapping("data/alarm")
    @WebApi(forceAuth = true, master = true)
    public Output getAlarmData(@Valid BusinessDataParam param) {
        Client client = AppContext.getSession().client;
        Page<AlarmCfgDataAlarmCfg> alarmCfgDataPage = null;
        AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
        filter.state = 1;
        filter.start_date = param.monitorBeginTime;
        filter.end_date = param.monitorEndTime;
        filter.account_id = client.userId;
        Map<String, Object> bParams = new HashMap<String, Object>();
        if(param.boxId != 0 && param.boxId != -100 && param.boxId != -200){
            bParams.put("boxId", param.boxId);
        }
        /** 管理者账号 **/
        if (client.userInfo.getUserType() == 1) {
            alarmCfgDataPage = alarmCfgDataApi.getAdminAlarmCfgDataPage(filter, bParams, param.pageIndex, param.pageSize);
        }
        /** 视图账号 **/
        else if (client.userInfo.getUserType() == 2) {
            alarmCfgDataPage = alarmCfgDataApi.getViewAlarmCfgDataPage(filter, bParams, param.pageIndex, param.pageSize);
        }

        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();
        List<AlarmCfgDataAlarmCfg> alarmCfgDataList = alarmCfgDataPage.getList();
        if(null != alarmCfgDataList){
            for(AlarmCfgDataAlarmCfg alarmCfg : alarmCfgDataList){
                JSONObject data = new JSONObject();
                data.put("monitorName", alarmCfg.name);
                data.put("state", alarmCfg.state);
                data.put("number", alarmCfg.value);
                data.put("monitorTime", alarmCfg.monitor_time);
                arr.add(data);
            }
            json.put("list", arr);
        }

        return new Output(json);
    }

    /**
     * 获取盒子列表
     * @return
     */
    @RequestMapping("data/boxs")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output getBoxData() {
        Client client = AppContext.getSession().client;
        List<Map<String, Object>> result = deviceApi.getDevicesByGroup(client.userId);
        JSONObject json = new JSONObject();
        json.put("list", result);
        return new Output(json);
    }

    /**
     * 获取分组数据
     * @return
     */
    @RequestMapping("data/group")
    @WebApi(forceAuth = true, master = true)
    public Output getGroupData(@Valid BusinessDataParam param) {
        if(0 == param.boxId){
            throw new BusinessException(ErrorCodeOption.DeviceId_Is_Unknown.key,
                    ErrorCodeOption.DeviceId_Is_Unknown.value);
        }
        Client client = AppContext.getSession().client;
        List<AccountDir> accountDirList = accountDirApi.getAccountDirList(client.userId, param.type, param.boxId);
        JSONArray arr = new JSONArray();
        if(null != accountDirList){
            for(AccountDir ad : accountDirList){
                JSONObject data = new JSONObject();
                data.put("groupId", ad.id);
                data.put("groupName", ad.name);
                arr.add(data);
            }
        }
        JSONObject json = new JSONObject();
        json.put("list", arr);
        return new Output(json);
    }

    /**
     * 获取监控点列表
     * @return
     */
    @RequestMapping("data/monitors")
    @WebApi(forceAuth = true, master = true)
    public Output getMonitorData(@Valid BusinessDataParam param) {
        Client client = AppContext.getSession().client;
        // 获取实时数据配置信息
        RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
        List<RealHisCfgDevice> realHisCfgDeviceList = null;
        /** 管理者账号 **/
        if (client.userInfo.getUserType() == 1) {
            /** 管理 **/
            realHisCfgFilter.addr_type = -1;
            realHisCfgFilter.data_type = 1;
            realHisCfgFilter.his_cycle = -1;
            realHisCfgFilter.state =-1;
            realHisCfgFilter.account_id = client.userId;
            realHisCfgFilter.dirId = -1;
            if (param.boxId != 0 && param.boxId != -100 && param.boxId != -200) {
                realHisCfgFilter.device_id = param.boxId;
            }
            realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(realHisCfgFilter);
        } else if (client.userInfo.getUserType() == 2) {
            /** 视图 **/
            // 通过视图获取配置信息
            ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();
            viewAccountRoleFilter.view_id = client.userId;
            viewAccountRoleFilter.cfg_type = 1;
            viewAccountRoleFilter.data_type = 1;
            viewAccountRoleFilter.role_type = -1;
            viewAccountRoleFilter.state = -1;
            viewAccountRoleFilter.dirId = -1;
            realHisCfgDeviceList = realHisCfgApi.getRealHisCfg(viewAccountRoleFilter);

        }
        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();
        if (realHisCfgDeviceList == null || realHisCfgDeviceList.size() < 1) {
            return new Output(json);
        }
        for(RealHisCfgDevice row : realHisCfgDeviceList){
            JSONObject data = new JSONObject();
            data.put("monitorId", row.id);
            data.put("monitorName", row.name);
            arr.add(data);
        }

        json.put("list", arr);
        return new Output(json);
    }

    /**
     * 获取实时数据详情
     * @return
     */
    @RequestMapping("data/realdetail")
    @WebApi(forceAuth = true, master = true)
    public Output getRealDetail(BusinessDataParam param) {
        JSONObject json = new JSONObject();
        RealHisCfgDevice realHisCfgDevice = realHisCfgApi.getRealHisCfgDevice(param.monitorId);
        if(null != realHisCfgDevice){
            String device_machine = realHisCfgDevice.machine_code;
            // 通过机器码去redis中获取数据
            RedisPiBoxActData redisPiBoxActData = redisPiBoxApi.getRedisPiBoxActData(device_machine);
            List<PiBoxCom> actTimeDataList = null == redisPiBoxActData ? null : redisPiBoxActData.act_time_data_list;
            JSONObject data = new JSONObject();
            data.put("monitorName", realHisCfgDevice.name);
            data.put("dataType", realHisCfgDevice.data_type);
            data.put("machine_code", realHisCfgDevice.machine_code);
            data.put("com", realHisCfgDevice.plc_id);
            data.put("addr_id", realHisCfgDevice.id);
            data.put("number", 0);
            if(null != actTimeDataList){
                for (int j = 0; j < actTimeDataList.size(); j++) {
                    PiBoxCom piBoxCom = actTimeDataList.get(j);
                    if (Long.parseLong(piBoxCom.com) == realHisCfgDevice.plc_id) {
                        List<PiBoxComAddr> addrList = piBoxCom.addr_list;
                        for (int k = 0; k < addrList.size(); k++) {
                            PiBoxComAddr piBoxComAddr = addrList.get(k);
                            if (Long.parseLong(piBoxComAddr.addr_id) == realHisCfgDevice.id) {
                                data.put("state", piBoxComAddr.state);
                                data.put("number", piBoxComAddr.value);
                            }
                        }
                    }
                }
            }
            json.put("detail", data);
        }

        return new Output(json);
    }
}

class BusinessDataParam {
    @Label("盒子ID")
    public long boxId;
    @Label("分组ID")
    public long groupId;
    @Label("监控点ID")
    public long monitorId;
    @Label("监控开始时间")
    public String monitorBeginTime;
    @Label("监控结束时间")
    public String monitorEndTime;
    @Label("当前页")
    public int pageIndex;
    @Label("每页条数")
    public int pageSize;
    @Label("监控点分组类型")
    public int type;

    public void setBoxId(long boxId) {
        this.boxId = boxId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public void setMonitorBeginTime(String monitorBeginTime) {
        this.monitorBeginTime = monitorBeginTime;
    }

    public void setMonitorId(long monitorId) {
        this.monitorId = monitorId;
    }

    public void setMonitorEndTime(String monitorEndTime) {
        this.monitorEndTime = monitorEndTime;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setType(int type) {
        this.type = type;
    }
}