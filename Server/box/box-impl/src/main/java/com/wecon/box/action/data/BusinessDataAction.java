package com.wecon.box.action.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.*;
import com.wecon.box.entity.*;
import com.wecon.box.enums.DataTypeOption;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.AlarmCfgDataFilter;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.box.param.BusinessDataParam;
import com.wecon.common.util.CommonUtils;
import com.wecon.common.util.TimeUtil;
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
        json.put("totalPage", realHisCfgDevicePage.getTotalPage());
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
        Page<Map<String, Object>> realHisCfgDataPage = null;
        Map<String, Object> bParams = new HashMap<String, Object>();
        if(param.boxId != 0 && param.boxId != -100 && param.boxId != -200){
            bParams.put("boxId", param.boxId);
        }
        if(param.monitorId != 0){
            bParams.put("monitorId", param.monitorId);
        }
        if(!CommonUtils.isNullOrEmpty(param.monitorBeginTime)){
            bParams.put("monitorBeginTime", param.monitorBeginTime);
        }
        if(!CommonUtils.isNullOrEmpty(param.monitorEndTime)){
            bParams.put("monitorEndTime", param.monitorEndTime);
        }
        /** 管理者账号 **/
        if (client.userInfo.getUserType() == 1) {
            RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
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
        json.put("totalPage", realHisCfgDataPage.getTotalPage());
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
        filter.state = param.state;
        filter.start_date = param.monitorBeginTime;
        filter.end_date = param.monitorEndTime;
        filter.account_id = client.userId;
        Map<String, Object> bParams = new HashMap<>();
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
                data.put("monitorId", alarmCfg.alarm_cfg_id);
                data.put("monitorName", alarmCfg.name);
                data.put("state", alarmCfg.state);
                data.put("number", alarmCfg.value);
                data.put("monitorTime", alarmCfg.monitor_time);
                arr.add(data);
            }
            json.put("list", arr);
            json.put("totalPage", alarmCfgDataPage.getTotalPage());
        }

        return new Output(json);
    }

    /**
     * 获取盒子列表
     * @return
     */
    @RequestMapping("data/boxs")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output getBoxData(@Valid BusinessDataParam param) {
        Client client = AppContext.getSession().client;
        List<Map<String, Object>> result = deviceApi.getDevicesByGroup(client.userId, param.selAlarm);
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
        Map<String, Object> bParams = new HashMap<String, Object>();
        if(param.boxId != 0){
            bParams.put("boxId", param.boxId);
        }
        if(param.groupId != 0){
            bParams.put("groupId", param.groupId);
        }
        param.pageIndex = 1;
        param.pageSize = Integer.MAX_VALUE;
        /** 管理者账号 **/
        if (client.userInfo.getUserType() == 1) {
            /** 管理 **/
            realHisCfgFilter.addr_type = -1;
            realHisCfgFilter.data_type = 1;
            realHisCfgFilter.his_cycle = -1;
            realHisCfgFilter.state =3;
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
        Map realCfgMap = realHisCfgApi.getRealCfgDetail(param.monitorId);
        if(null != realCfgMap){
            String device_machine = realCfgMap.get("machine_code").toString();
            // 通过机器码去redis中获取数据
            RedisPiBoxActData redisPiBoxActData = redisPiBoxApi.getRedisPiBoxActData(device_machine);
            List<PiBoxCom> actTimeDataList = null == redisPiBoxActData ? null : redisPiBoxActData.act_time_data_list;
            JSONObject data = new JSONObject();
            data.put("monitorId", realCfgMap.get("id"));
            data.put("monitorName", realCfgMap.get("name"));
            data.put("connectDevice", realCfgMap.get("port"));
            int addrType = Integer.parseInt(realCfgMap.get("addr_type").toString());
            data.put("addrType", addrType==0?"位地址":addrType==1?"字节地址":addrType==2?"字地址":"双字");
            data.put("registerType", realCfgMap.get("rid"));
            // 主子地址分割
            String[] addrs = realCfgMap.get("addr").toString().split(",");
            if (addrs != null) {
                data.put("registerAddr", addrs[0]);
            }
            String dataType = DataTypeOption.getDataTypeValue(Long.parseLong(realCfgMap.get("data_id").toString()));
            data.put("dataType", dataType);
            // 整数位 小数位分割
            if (realCfgMap.get("digit_count") != null) {
                String[] numdecs = realCfgMap.get("digit_count").toString().split(",");
                if (numdecs != null) {
                    if (numdecs.length == 1) {
                        data.put("integerDigit", numdecs[0]);
                        data.put("decimalDigit", "");
                    } else if (numdecs.length == 2) {
                        data.put("integerDigit", numdecs[0]);
                        data.put("decimalDigit", numdecs[1]);
                    }
                }
            }
            data.put("describe", realCfgMap.get("describe"));
            if(null != actTimeDataList){
                for (int j = 0; j < actTimeDataList.size(); j++) {
                    PiBoxCom piBoxCom = actTimeDataList.get(j);
                    if (piBoxCom.com.equals(realCfgMap.get("plc_id").toString())) {
                        List<PiBoxComAddr> addrList = piBoxCom.addr_list;
                        for (int k = 0; k < addrList.size(); k++) {
                            PiBoxComAddr piBoxComAddr = addrList.get(k);
                            if (piBoxComAddr.addr_id.equals(realCfgMap.get("id").toString())) {
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
