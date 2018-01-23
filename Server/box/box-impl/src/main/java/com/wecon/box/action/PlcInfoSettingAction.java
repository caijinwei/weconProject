package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.*;
import com.wecon.box.entity.AlarmCfg;
import com.wecon.box.entity.PlcInfo;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.param.PlcInfoData;
import com.wecon.box.param.PlcInfoSettingParam;
import com.wecon.box.redis.RedisUpdDeviceCfg;
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
 * Created by caijinw on 2017/8/5.
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
    @Autowired
    DriverApi driverApi;
    @Autowired
    RealHisCfgApi realHisCfgApi;
    @Autowired
    AlarmCfgApi alarmCfgApi;
    @Autowired
    DeviceApi deviceApi;


    @Autowired
    RealHisCfgDataApi realHisCfgDataApi;
    @Autowired
    ViewAccountRoleApi viewAccountRoleApi;
    @Autowired
    AccountDirRelApi accountDirRelApi;
    @Autowired
    AlarmCfgDataApi alarmCfgDataApi;

    @Autowired
    RedisUpdDeviceCfg redisUpdDeviceCfg;


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
            //过滤掉删除配置的plc
            if (info.state == 3) {
                continue;
            }
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
        long currentPlcId = param.plc_id;
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
            //新增
            if (!plcInfo.port.equals("Ethernet")) {
                List<PlcInfo> plcList = plcInfoApi.getPortState(plcInfo.device_id, plcInfo.port);
                for (PlcInfo p : plcList) {
                    if (p.state != 3) {
                        throw new BusinessException(ErrorCodeOption.Is_Exist_PlcPort.key, ErrorCodeOption.Is_Exist_PlcPort.value);
                    }
                }
            }
        } else {
            //编辑

            //port变更
            if (!plcInfo.port.equals(plcInfoApi.findPlcInfoByPlcId((int) plcInfo.plc_id).port)) {
                if (!plcInfo.port.equals("Ethernet")) {
                    List<PlcInfo> plcList = plcInfoApi.getPortState(plcInfo.device_id, plcInfo.port);
                    for (PlcInfo p : plcList) {
                        if (p.state != 3) {
                            throw new BusinessException(ErrorCodeOption.Is_Exist_PlcPort.key, ErrorCodeOption.Is_Exist_PlcPort.value);
                        }
                    }
                }
            }
        }
        if (driverApi.getDriverBydriver(plcInfo.driver) == null) {
            throw new BusinessException(ErrorCodeOption.Driver_IsNot_Fount.key, ErrorCodeOption.Device_NotFound.value);
        }


        //更新操作
        if (plcInfo.plc_id > 0) {
            PlcInfo oldPlc = plcInfoApi.findPlcInfoByPlcId(plcInfo.plc_id);

            //修改驱动名称
            //设置state为3  且  将通讯口下的监控点全部删除掉
            plcInfo.state = 2;
            if (!oldPlc.type.equals(plcInfo.type) || !oldPlc.port.equals(plcInfo.port)) {
                plcInfoApi.updatePlcInfo(plcInfo);

//                List<RealHisCfg> oldRealHisCfgs=realHisCfgApi.findRealHisCfgsByPlcId(plcInfo.plc_id);
//                List<AlarmCfg> oldAlarmCfgs=alarmCfgApi.getAlarmByPlcId(plcInfo.plc_id);

                if (oldPlc.is_sync == 0) {
                    delPlcRelation(plcInfo.plc_id);
                } else {
                    //同步到盒子
                    realHisCfgApi.updateRealHisState(plcInfo.plc_id, 3, 0);
                    alarmCfgApi.updateAlarmCfgState(plcInfo.plc_id, 3, 0);

                    //实时历史消息推送
                    RealHisCfgFilter realHisCfgFilter = new RealHisCfgFilter();
                    realHisCfgFilter.plc_id = plcInfo.plc_id;
                    List<RealHisCfgDevice> realHisCfgList = realHisCfgApi.getRealHisCfg(realHisCfgFilter);
                    for (RealHisCfgDevice realHisCfgDevice : realHisCfgList) {
                        redisUpdDeviceCfg.pubDelRealHisCfg(realHisCfgDevice.id, plcInfo.plc_id);
                    }

                    //报警消息推送
                    List<AlarmCfg> alarmCfgList = alarmCfgApi.getAlarmByPlcId(plcInfo.plc_id);
                    for (AlarmCfg alarmCfg : alarmCfgList) {
                        redisUpdDeviceCfg.pubDelAlarmCfg(alarmCfg.alarmcfg_id, plcInfo.plc_id);
                    }
                }

                // <editor - fold desc = "操作日志" >
//                List<RealHisCfg> newRealHisCfgs=realHisCfgApi.findRealHisCfgsByPlcId(plcInfo.plc_id);
//                dbLogUtil.updOperateLog(OpTypeOption.DelRealHis,ResTypeOption.Plc,plcInfo.plc_id,oldRealHisCfgs,newRealHisCfgs);
//
//                List<AlarmCfg> alarmCfgs=alarmCfgApi.getAlarmByPlcId(plcInfo.plc_id);
//                dbLogUtil.updOperateLog(OpTypeOption.DelRealHis,ResTypeOption.Plc,plcInfo.plc_id,oldAlarmCfgs,newRealHisCfgs);
                //</editor-fold>
            } else {
                //没有修改驱动名称
                plcInfoApi.updatePlcInfo(plcInfo);
            }
            // <editor - fold desc = "操作日志" >
            dbLogUtil.updOperateLog(OpTypeOption.UpdatePlc, ResTypeOption.Plc, oldPlc.plc_id, oldPlc, plcInfo);
            //</editor-fold>
        } else {
            plcInfo.state = 1;
            plcInfo.is_sync = 0;
            long newPlcInfoId = plcInfoApi.savePlcInfo(plcInfo);
            currentPlcId = newPlcInfoId;

            // <editor - fold desc = "操作日志" >
            dbLogUtil.addOperateLog(OpTypeOption.AddPlc, ResTypeOption.Plc, newPlcInfoId, plcInfo);
            //</editor-fold>
        }

        //消息推送给redis 提高实时性
        redisUpdDeviceCfg.pubUpdPlcInfo(currentPlcId);

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
        PlcInfo plcInfo = plcInfoApi.getPlcInfo(plcId);
        if (plcInfo.is_sync == 0) {
            plcInfoApi.delPlcInfo(plcId);
            delPlcRelation(Long.parseLong(plcId+""));
        } else {
            plcInfoApi.unBundledPlc(plcId);

            //解绑消息推送redis
            if (redisUpdDeviceCfg.pubDelPlcInfo(plcId) == true) {
                //实时历史消息推送

                List<RealHisCfg> realHisCfgList = realHisCfgApi.getRealHisCfgList(Long.parseLong(plcId + ""));
                if (realHisCfgList != null) {
                    for (RealHisCfg realHisCfg : realHisCfgList) {
                        redisUpdDeviceCfg.pubDelRealHisCfg(realHisCfg.id, plcId);
                    }
                }

                //报警消息推送
                List<AlarmCfg> alarmCfgList = alarmCfgApi.getAlarmByPlcId(plcId);
                if (alarmCfgList != null) {
                    for (AlarmCfg alarmCfg : alarmCfgList) {
                        redisUpdDeviceCfg.pubDelAlarmCfg(alarmCfg.alarmcfg_id, plcId);
                    }
                }
            }
        }

        // <editor - fold desc = "操作日志" >
        dbLogUtil.addOperateLog(OpTypeOption.DelPlc, ResTypeOption.Plc, plcId, plcInfo);
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

    //删除监控点配置、数据、相关权限
    public void delPlcRelation(Long redundantPlcId) {
        //新增com1 或者com2  编辑com1 com2口
        //将未同步盒子(is_sync=0)且state=3的plcInfo --> 物理删除删除
        List<RealHisCfg> realHisCfgs = realHisCfgApi.getRealHisCfgList(redundantPlcId);
        List<AlarmCfg> alarmCfgs = alarmCfgApi.getAlarmByPlcId(redundantPlcId);
        ArrayList<Long> realHisCfgIds = new ArrayList<>();
        ArrayList<Long> alarmCfgIds = new ArrayList<>();
        for (RealHisCfg cfg : realHisCfgs) {
            realHisCfgIds.add(cfg.id);
        }
        for (AlarmCfg cfg : alarmCfgs) {
            alarmCfgIds.add(cfg.alarmcfg_id);
        }
        realHisCfgApi.batchDeleteById(realHisCfgIds);
        realHisCfgDataApi.batchDeleteById(realHisCfgIds);
        alarmCfgApi.batchDeleteById(alarmCfgIds);
        alarmCfgDataApi.batchDeleteById(alarmCfgIds);
        viewAccountRoleApi.batchDeleteViewAccRoleByCfgId(realHisCfgIds, 1);
        viewAccountRoleApi.batchDeleteViewAccRoleByCfgId(alarmCfgIds, 2);
        accountDirRelApi.batchDeleteAccDirRelByCfgId(realHisCfgIds, 1, 2);
        accountDirRelApi.batchDeleteAccDirRelByCfgId(alarmCfgIds, 3);
    }

}
