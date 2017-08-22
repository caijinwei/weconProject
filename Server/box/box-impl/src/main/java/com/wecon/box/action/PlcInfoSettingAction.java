package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DevBindUserApi;
import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.entity.PlcInfo;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.param.PlcInfoData;
import com.wecon.box.param.PlcInfoSettingParam;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/5.
 */

@RestController
@RequestMapping("plcInfoAction")
public class PlcInfoSettingAction
{
    @Autowired
    PlcInfoApi plcInfoApi;
    @Autowired
    DevBindUserApi devBindUserApi;

    @Description("新增plc配置")
    @RequestMapping(value = "/addPlcInfo")
    @WebApi(forceAuth =true,master=true,authority = {"1"})
    public Output comPortSetting(@Valid PlcInfoSettingParam param)
    {
        long account_id=AppContext.getSession().client.userId;
        if(devBindUserApi.isRecord((int)param.device_id,account_id)==false)
        {
            throw new BusinessException(ErrorCodeOption.PiBoxDevice_IsNot_Found.key,ErrorCodeOption.PiBoxDevice_IsNot_Found.value);
        }
        PlcInfo plcInfo=new PlcInfo();
        plcInfo.type=param.type;
        plcInfo.port=param.port;

        plcInfo.baudrate=param.baudrate;
        plcInfo.box_stat_no=param.box_stat_no;
        plcInfo.check_bit=param.check_bit;
        plcInfo.com_iodelaytime=param.com_iodelaytime;
        plcInfo.com_stepinterval=param.com_stepinterval;
        plcInfo.comtype=param.comtype;
        plcInfo.data_length=param.data_length;
        plcInfo.rev_timeout=param.rev_timeout;
        plcInfo.com_stepinterval=param.com_stepinterval;
        plcInfo.device_id=param.device_id;
        plcInfo.net_broadcastaddr=param.net_broadcastaddr;
        plcInfo.net_ipaddr=param.net_ipaddr;
        plcInfo.net_isbroadcast=param.net_isbroadcast;
        plcInfo.net_port=param.net_port;
        plcInfo.net_type=param.net_type;


        // 驱动文件夹 没有不能输入  这里写默认值
        plcInfo.driver="驱动文件名（测试）Action 这边先写";
        plcInfoApi.savePlcInfo(plcInfo);
        return new Output();
    }


    @Label("展示该盒子下全部通讯口")
    @WebApi(forceAuth =true,master=true,authority = {"1"})
    @RequestMapping("showAllPlcConf")
    public Output showAllPlcConf(@RequestParam("device_id") Integer device_id)
    {

        long account_id=AppContext.getSession().client.userId;
        if(devBindUserApi.isRecord(device_id,account_id)==false)
        {
            throw new BusinessException(ErrorCodeOption.PiBoxDevice_IsNot_Found.key,ErrorCodeOption.PiBoxDevice_IsNot_Found.value);
        }
        JSONObject data=new JSONObject();

        List<PlcInfo> showAllPlcConf=plcInfoApi.showAllPlcInfoByDeviceId(device_id);
        List<PlcInfoData> infoDatas=new ArrayList<PlcInfoData>();
        for(PlcInfo info:showAllPlcConf)
        {
            PlcInfoData plcInfoData =new PlcInfoData();
            if(info.comtype==1)
            {
                plcInfoData.comtype="RS232";
            }else if(info.comtype==2)
            {
                plcInfoData.comtype="RS422";
            }else
            {
                plcInfoData.comtype="RS485";
            }
            plcInfoData.plcId=info.plc_id;
            plcInfoData.port= info.port;
            plcInfoData.type=info.type;
            infoDatas.add(plcInfoData);
        }

        data.put("infoDatas",infoDatas);

        return new Output(data);
    }

    @Label("更新通讯口配置")
    @WebApi(forceAuth =false,master=true)
    @RequestMapping("updataPlcInfo")
    public Output update(@Valid PlcInfoSettingParam param)
    {

        long account_id=AppContext.getSession().client.userId;
        if(devBindUserApi.isRecord((int)param.plc_id,account_id)==false)
        {
            throw new BusinessException(ErrorCodeOption.PiBoxDevice_IsNot_Found.key,ErrorCodeOption.PiBoxDevice_IsNot_Found.value);
        }
        PlcInfo plcInfo=new PlcInfo();
        plcInfo.plc_id=param.plc_id;
        plcInfo.type=param.type;
        plcInfo.port=param.port;

        plcInfo.baudrate=param.baudrate;
        plcInfo.box_stat_no=param.box_stat_no;
        plcInfo.check_bit=param.check_bit;
        plcInfo.com_iodelaytime=param.com_iodelaytime;
        plcInfo.com_stepinterval=param.com_stepinterval;
        plcInfo.comtype=param.comtype;
        plcInfo.data_length=param.data_length;
        plcInfo.rev_timeout=param.rev_timeout;
        plcInfo.com_stepinterval=param.com_stepinterval;
        plcInfo.device_id=param.device_id;
        plcInfo.net_broadcastaddr=param.net_broadcastaddr;
        plcInfo.net_ipaddr=param.net_ipaddr;
        plcInfo.net_isbroadcast=param.net_isbroadcast;
        plcInfo.net_port=param.net_port;
        plcInfo.net_type=param.net_type;

        plcInfoApi.updatePlcInfo(plcInfo);
        return new Output();
    }

}
