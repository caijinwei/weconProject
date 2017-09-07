package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DevBindUserApi;
import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.entity.PlcInfo;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.box.param.PlcInfoData;
import com.wecon.box.param.PlcInfoSettingParam;
import com.wecon.box.util.DbLogUtil;
import com.wecon.box.util.PlcListByType;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/5.
 */

@RestController
@RequestMapping("plcInfoAction")
public class PlcInfoSettingAction {
    @Autowired
    PlcInfoApi plcInfoApi;
    @Autowired
    DevBindUserApi devBindUserApi;
    @Autowired
    PlcListByType plcListByType;
    @Autowired
    DbLogUtil dbLogUtil;

//    @Description("新增plc配置")
//    @RequestMapping(value = "/addPlcInfo")
//    @WebApi(forceAuth = true, master = true, authority = {"1"})
//    public Output comPortSetting(@Valid PlcInfoSettingParam param) {
//        long account_id = AppContext.getSession().client.userId;
//        if (devBindUserApi.isRecord((int) param.device_id, account_id) == false) {
//            throw new BusinessException(ErrorCodeOption.PiBoxDevice_IsNot_Found.key, ErrorCodeOption.PiBoxDevice_IsNot_Found.value);
//        }
//        PlcInfo plcInfo = new PlcInfo();
//        plcInfo.type = param.type;
//        plcInfo.port = param.port;
//        plcInfo.state = param.state;
//        plcInfo.baudrate = param.baudrate;
//        plcInfo.box_stat_no = param.box_stat_no;
//        plcInfo.check_bit = param.check_bit;
//        plcInfo.com_iodelaytime = param.com_iodelaytime;
//        plcInfo.com_stepinterval = param.com_stepinterval;
//        plcInfo.comtype = param.comtype;
//        plcInfo.stop_bit = param.stop_bit;
//        plcInfo.data_length = param.data_length;
//        plcInfo.rev_timeout = param.rev_timeout;
//        plcInfo.com_stepinterval = param.com_stepinterval;
//        plcInfo.device_id = param.device_id;
//        plcInfo.net_broadcastaddr = param.net_broadcastaddr;
//        plcInfo.net_ipaddr = param.net_ipaddr;
//        plcInfo.net_isbroadcast = param.net_isbroadcast;
//        plcInfo.net_port = param.net_port;
//        plcInfo.net_type = param.net_type;
//        plcInfo.driver = param.driver;
//        if (!plcInfo.port.equals("Ethernet")) {
//            if (plcInfoApi.isExistPort(plcInfo.device_id, plcInfo.port)) {
//                throw new BusinessException(ErrorCodeOption.Is_Exist_PlcPort.key, ErrorCodeOption.Is_Exist_PlcPort.value);
//            }
//        }
//        plcInfoApi.savePlcInfo(plcInfo);
//        //<editor-fold desc="操作日志">
//        dbLogUtil.addOperateLog(OpTypeOption.AddPlc, ResTypeOption.Plc, , dir);
//        //</editor-fold>
//        return new Output();
//    }
//

    @Label("展示该盒子下全部通讯口")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("showAllPlcConf")
    public Output showAllPlcConf(@RequestParam("device_id") Integer device_id) {

        long account_id = AppContext.getSession().client.userId;
        if (devBindUserApi.isRecord(device_id, account_id) == false) {
            throw new BusinessException(ErrorCodeOption.PiBoxDevice_IsNot_Found.key, ErrorCodeOption.PiBoxDevice_IsNot_Found.value);
        }
        JSONObject data = new JSONObject();

        List<PlcInfo> showAllPlcConf = plcInfoApi.showAllPlcInfoByDeviceId(device_id);
        List<PlcInfoData> infoDatas = new ArrayList<PlcInfoData>();
        for (PlcInfo info : showAllPlcConf) {
            PlcInfoData plcInfoData = new PlcInfoData();
//            if (info.comtype == 1) {
//                plcInfoData.comtype = "RS232";
//            } else if (info.comtype == 2) {
//                plcInfoData.comtype = "RS422";
//            } else {
//                plcInfoData.comtype = "RS485";
//            }
            plcInfoData.comtype = info.comtype + "";
            plcInfoData.plcId = info.plc_id;
            plcInfoData.port = info.port;
            plcInfoData.type = info.type;
            plcInfoData.state = info.state;
            infoDatas.add(plcInfoData);
        }

        data.put("infoDatas", infoDatas);

        return new Output(data);
    }

//    @Label("更新通讯口配置")
//    @WebApi(forceAuth = true, master = true, authority = {"1"})
//    @RequestMapping("updataPlcInfo")
//    public Output update(@Valid PlcInfoSettingParam param) {
//        long account_id = AppContext.getSession().client.userId;
//
//        PlcInfo plcInfo = new PlcInfo();
//        plcInfo.plc_id = param.plc_id;
//        plcInfo.type = param.type;
//        plcInfo.port = param.port;
//
//        plcInfo.state = param.state;
//        plcInfo.baudrate = param.baudrate;
//        plcInfo.box_stat_no = param.box_stat_no;
//        plcInfo.check_bit = param.check_bit;
//        plcInfo.com_iodelaytime = param.com_iodelaytime;
//        plcInfo.com_stepinterval = param.com_stepinterval;
//        plcInfo.comtype = param.comtype;
//        plcInfo.data_length = param.data_length;
//        plcInfo.rev_timeout = param.rev_timeout;
//        plcInfo.com_stepinterval = param.com_stepinterval;
//        plcInfo.device_id = param.device_id;
//        plcInfo.net_broadcastaddr = param.net_broadcastaddr;
//        plcInfo.net_ipaddr = param.net_ipaddr;
//        plcInfo.net_isbroadcast = param.net_isbroadcast;
//        plcInfo.net_port = param.net_port;
//        plcInfo.net_type = param.net_type;
//        plcInfo.driver = param.driver;
//        if (!plcInfo.port.equals(plcInfoApi.findPlcInfoByPlcId((int) plcInfo.plc_id).port)) {
//            if (!plcInfo.port.equals("Ethernet")) {
//                if (plcInfoApi.isExistPort(plcInfo.device_id, plcInfo.port)) {
//                    throw new BusinessException(ErrorCodeOption.Is_Exist_PlcPort.key, ErrorCodeOption.Is_Exist_PlcPort.value);
//                }
//            }
//        }
//        plcInfoApi.updatePlcInfo(plcInfo);
//        return new Output();
//    }

    @Label("更新通讯口配置")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("savePlcInfo")
    public Output savePlcInfo(@Valid PlcInfoSettingParam param) {
        long account_id = AppContext.getSession().client.userId;

        PlcInfo plcInfo = new PlcInfo();
        plcInfo.plc_id = param.plc_id;
        plcInfo.type = param.type;
        plcInfo.port = param.port;
        plcInfo.baudrate = param.baudrate;
        plcInfo.box_stat_no = param.box_stat_no;
        plcInfo.check_bit = param.check_bit;
        plcInfo.com_iodelaytime = param.com_iodelaytime;
        plcInfo.com_stepinterval = param.com_stepinterval;
        plcInfo.comtype = param.comtype;
        plcInfo.data_length = param.data_length;
        plcInfo.rev_timeout = param.rev_timeout;
        plcInfo.com_stepinterval = param.com_stepinterval;
        plcInfo.device_id = param.device_id;
        plcInfo.net_broadcastaddr = param.net_broadcastaddr;
        plcInfo.net_ipaddr = param.net_ipaddr;
        plcInfo.net_isbroadcast = param.net_isbroadcast;
        plcInfo.net_port = param.net_port;
        plcInfo.net_type = param.net_type;
        plcInfo.driver = param.driver;

        plcInfo.plc_stat_no = param.plc_stat_no;
        plcInfo.stop_bit = param.stop_bit;
        plcInfo.retry_times = param.retry_times;
        plcInfo.wait_timeout = param.wait_timeout;

        if (plcInfo.plc_id <= 0) {
            if (!plcInfo.port.equals("Ethernet")) {
                if (plcInfoApi.isExistPort(plcInfo.device_id, plcInfo.port)) {
                    throw new BusinessException(ErrorCodeOption.Is_Exist_PlcPort.key, ErrorCodeOption.Is_Exist_PlcPort.value);
                }
            }
        } else {
            if (!plcInfo.port.equals(plcInfoApi.findPlcInfoByPlcId((int) plcInfo.plc_id).port)) {
                if (!plcInfo.port.equals("Ethernet")) {
                    if (plcInfoApi.isExistPort(plcInfo.device_id, plcInfo.port)) {
                        throw new BusinessException(ErrorCodeOption.Is_Exist_PlcPort.key, ErrorCodeOption.Is_Exist_PlcPort.value);
                    }
                }
            }
        }
        if (plcInfo.plc_id > 0) {
            plcInfo.state = 2;
            PlcInfo oldPlc=plcInfoApi.findPlcInfoByPlcId(plcInfo.plc_id);
            plcInfoApi.updatePlcInfo(plcInfo);
            // <editor - fold desc = "操作日志" >
            dbLogUtil.updOperateLog(OpTypeOption.UpdatePlc,ResTypeOption.Plc,oldPlc.plc_id,plcInfo,oldPlc);
            //</editor-fold>
        } else {
            plcInfo.state = 1;
            long newPlcInfoId=plcInfoApi.savePlcInfo(plcInfo);
            // <editor - fold desc = "操作日志" >
            dbLogUtil.addOperateLog(OpTypeOption.UpdatePlc,ResTypeOption.Plc,newPlcInfoId,plcInfo);
            //</editor-fold>
        }
        return new Output();
    }

    @Label("通讯口配置默认值展示")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("showPlcSetDefault")
    public Output showPlcSetDefault() {
        /*
        * UsbDevice="1"
        * Ethernet="1"
        * 获取 port 通讯协议 是 USB的
        * */
        JSONObject data = new JSONObject();
//        List<PlcInfo> usbDeviceList = plcListByType.getPlcListByType("UsbDevice", "1");
//        List<PlcInfo> ethernetList = plcListByType.getPlcListByType("Ethernet", "1")；
        //     List<PlcInfo> comList = plcListByType.getAllComType();
        /*
        * 获取以map<pType,List<plcInfo>>\
        * ptype为分组    的list
        * */
        Map<String, List<PlcInfo>> usbDeviceMapListByPtype = plcListByType.getPlcByPtype(plcListByType.getPlcListByType("UsbDevice", "1"));
        Set<String> usbDeviceMapListKeyByPtype = usbDeviceMapListByPtype.keySet();
        Map<String, List<PlcInfo>> ethernetMapListByPtype = plcListByType.getPlcByPtype(plcListByType.getPlcListByType("Ethernet", "1"));
        Set<String> ethernetMapListKeyByPtype = ethernetMapListByPtype.keySet();
        Map<String, List<PlcInfo>> comMapListByPtype = plcListByType.getPlcByPtype(plcListByType.getAllComType());
        Set<String> comMapListKeyByPtype = comMapListByPtype.keySet();


        /*
        * 获取以map<type,List<plcInfo>>
        * */
        Map<String, List<PlcInfo>> usbDeviceMapListByType = plcListByType.getPlcByType(plcListByType.getPlcListByType("UsbDevice", "1"));
        Map<String, List<PlcInfo>> ethernetMapListByType = plcListByType.getPlcByType(plcListByType.getPlcListByType("Ethernet", "1"));
        Map<String, List<PlcInfo>> comMapListByType = plcListByType.getPlcByType(plcListByType.getAllComType());

        data.put("comMapListKeyByPtype", comMapListKeyByPtype);
        data.put("comMapListByPtype", comMapListByPtype);
        data.put("comMapListByType", comMapListByType);

        data.put("usbDeviceMapListByType", usbDeviceMapListByType);
        data.put("ethernetMapListByType", ethernetMapListByType);
        data.put("usbDeviceMapListByPtype", usbDeviceMapListByPtype);
        data.put("ethernetMapListByPtype", ethernetMapListByPtype);
        data.put("usbDeviceMapListKeyByPtype", usbDeviceMapListKeyByPtype);
        data.put("ethernetMapListKeyByPtype", ethernetMapListKeyByPtype);

        /*
        * 获取UsebDevice
        * */
        return new Output(data);
    }

    @Label("plc解绑")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("unbundledPlc")
    public Output unBundledPlc(@RequestParam("plc_id") Integer plcId) {
        if (null == plcId) {
            throw new BusinessException(ErrorCodeOption.Is_Not_Params_DeviceID.key, ErrorCodeOption.Is_Not_Params_DeviceID.value);
        }
        plcInfoApi.unBundledPlc(plcId);
        // <editor - fold desc = "操作日志" >
        dbLogUtil.addOperateLog(OpTypeOption.DelPlc,ResTypeOption.Plc,plcId,plcInfoApi.findPlcInfoByPlcId(plcId));
        //</editor-fold>
        return new Output();
    }

    @Label("单个plc展示（用于修改plc展示）")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("findPlcInfoById")
    public Output findPlcInfoById(@RequestParam("plc_id") Integer plcId) {
        if (null == plcId) {
            throw new BusinessException(ErrorCodeOption.Is_Not_Params_DeviceID.key, ErrorCodeOption.Is_Not_Params_DeviceID.value);
        }
        JSONObject data = new JSONObject();
        data.put("plcInfo", plcInfoApi.findPlcInfoByPlcId(plcId));
        return new Output(data);
    }


}
