package com.wecon.box.data.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.constant.Constant;
import com.wecon.box.data.util.SendvalueCallback;
import com.wecon.box.entity.*;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.box.param.BusinessDataParam;
import com.wecon.box.util.ClientMQTT;
import com.wecon.box.util.SendValue;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Client;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;


/**
 * Created by whp on 2017/8/8.
 */
@Label("获取业务数据")
@RestController
public class BusDataAction {
    @Autowired
    private DeviceApi deviceApi;
    @Autowired
    private SendValue sendValue;
    @Autowired
    private RealHisCfgApi realHisCfgApi;
    @Autowired
    private com.wecon.box.action.data.BusinessDataAction busDataAct;
    
    private static final Logger logger = LogManager.getLogger(BusDataAction.class.getName());
    /**
     * 实时监控点数据列表
     * @param param
     * @return
     */
    @RequestMapping("we-data/realdata")
    @WebApi(forceAuth = true, master = true)
    public Output getRealData(@Valid BusinessDataParam param) {
        if(0 == param.boxId){
            Client client = AppContext.getSession().client;
            /** 管理者账号 **/
            if (client.userInfo.getUserType() == 1) {
                param.boxId = -100;
            }
            /** 视图账号 **/
            else if (client.userInfo.getUserType() == 2) {
                param.boxId = -200;
            }
        }
        return busDataAct.getRealData(param);
    }

    /**
     * 历史监控点数据列表
     * @param param
     * @return
     */
    @RequestMapping("we-data/historydata")
    @WebApi(forceAuth = true, master = true)
    public Output getHistoryData(@Valid BusinessDataParam param) {

        return busDataAct.getHistoryData(param);
    }

    /**
     * 报警监控点数据列表
     * @param param
     * @return
     */
    @RequestMapping("we-data/alarmdata")
    @WebApi(forceAuth = true, master = true)
    public Output getAlarmData(@Valid BusinessDataParam param) {

        return busDataAct.getAlarmData(param);
    }

    /**
     * 盒子列表
     * @return
     */
    @RequestMapping("we-data/boxs")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output getBoxData(@Valid BusinessDataParam param) {

        return busDataAct.getBoxData(param);
    }


    /**
     * 实时监控点分组列表
     * @return
     */
    @RequestMapping("we-data/realgroups")
    @WebApi(forceAuth = true, master = true)
    public Output getGroupData(@Valid BusinessDataParam param) {
        param.type = 1;
        if(0 == param.boxId){
            Client client = AppContext.getSession().client;
            /** 管理者账号 **/
            if (client.userInfo.getUserType() == 1) {
                param.boxId = -100;
            }
            /** 视图账号 **/
            else if (client.userInfo.getUserType() == 2) {
                param.boxId = -200;
            }
        }
        return busDataAct.getGroupData(param);
    }

    /**
     * 历史监控点名称列表
     * @return
     */
    @RequestMapping("we-data/monitors")
    @WebApi(forceAuth = true, master = true)
    public Output getMonitorData(@Valid BusinessDataParam param) {

        return busDataAct.getMonitorData(param);
    }

    /**
     * 实时监控点配置列表
     * @return
     */
    @RequestMapping("we-data/realcfgs")
    @WebApi(forceAuth = true, master = true)
    public Output getRealcfg(BusinessDataParam param) {
        try {
            RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
            Page<RealHisCfgDevice> realHisCfgDevicePage = null;
            Client client = AppContext.getSession().client;
            Map<String, Object> bParams = new HashMap<String, Object>();
            if(param.boxId != 0){
                bParams.put("boxId", param.boxId);
            }
            if(param.groupId != 0){
                bParams.put("groupId", param.groupId);
            }
            param.pageIndex = 0 == param.pageIndex ? 1 : param.pageIndex;
            param.pageSize = 0 == param.pageSize ? 10 : param.pageSize;
            /** 管理者账号 **/
            if (client.userInfo.getUserType() == 1) {
                realHisCfgFilter.data_type = 0;
                realHisCfgFilter.account_id = client.userId;
                realHisCfgDevicePage = realHisCfgApi.getRealHisCfgDevicePage(realHisCfgFilter, bParams, param.pageIndex,
                        param.pageSize);
            }
            /** 视图账号 **/
            else if (client.userInfo.getUserType() == 2) {
                ViewAccountRoleFilter viewAccountRoleFilter = new ViewAccountRoleFilter();
                viewAccountRoleFilter.view_id = client.userId;
                viewAccountRoleFilter.data_type = 0;
                realHisCfgDevicePage = realHisCfgApi.getRealHisCfgDevicePage(viewAccountRoleFilter, bParams, param.pageIndex,
                        param.pageSize);
            }
            List<RealHisCfgDevice> realHisCfgDeviceList = realHisCfgDevicePage.getList();
            JSONObject json = new JSONObject();
            JSONArray arr = new JSONArray();

            if (realHisCfgDeviceList == null || realHisCfgDeviceList.size() < 1) {
                return new Output(json);
            }

            for (int i = 0; i < realHisCfgDeviceList.size(); i++) {
                RealHisCfgDevice realHisCfgDevice = realHisCfgDeviceList.get(i);
                JSONObject data = new JSONObject();
                data.put("monitorId", realHisCfgDevice.id);
                data.put("monitorName", CommonUtils.isNullOrEmpty(realHisCfgDevice.ref_alais) ? realHisCfgDevice.name
                        : realHisCfgDevice.ref_alais);
                data.put("upd_time", realHisCfgDevice.update_date);
                data.put("data_id", realHisCfgDevice.data_id);
                data.put("addr", realHisCfgDevice.addr);
                data.put("addr_type", realHisCfgDevice.addr_type);
                data.put("digit_count", realHisCfgDevice.digit_count);
                data.put("digit_binary", realHisCfgDevice.digit_binary);
                data.put("data_limit", realHisCfgDevice.data_limit);
                data.put("rid", realHisCfgDevice.rid);
                arr.add(data);
            }
            json.put("cfg_list", arr);
            json.put("totalPage", realHisCfgDevicePage.getTotalPage());
            json.put("totalRecord", realHisCfgDevicePage.getTotalRecord());
            json.put("currentPage", realHisCfgDevicePage.getCurrentPage());

            return new Output(json);
        } catch (Exception e) {
            logger.debug("Server error，" + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 修改实时监控点数据
     * @return
     */
    @RequestMapping("we-data/updrealdata")
    @WebApi(forceAuth = true, master = true)
    public Output updateRealData(@RequestParam("monitorId") String monitorId,
              @RequestParam("value") String value) {
        // 订阅消息
        if (!CommonUtils.isNullOrEmpty(monitorId)) {
            RealHisCfg realHisCfg = realHisCfgApi.getRealHisCfg(Long.parseLong(monitorId));
            Device device = deviceApi.getDevice(realHisCfg.device_id);
            String subscribeTopic = "pibox/cts/" + device.machine_code;

            SendvalueCallback sendvalueCallback = new SendvalueCallback(null, monitorId);
            ClientMQTT reclient = new ClientMQTT(subscribeTopic, "wecon-updrealdata", sendvalueCallback);
            reclient.start();

            // 发送数据
            sendValue.putMQTTMess(value, null, monitorId, OpTypeOption.WriteActPhone, reclient);
        }
        return new Output(new JSONObject());
    }
}
