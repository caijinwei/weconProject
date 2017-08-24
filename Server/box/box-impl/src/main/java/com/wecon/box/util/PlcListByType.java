package com.wecon.box.util;

import com.wecon.box.entity.PlcInfo;
import com.wecon.box.entity.plcdom.Plc;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/24.
 */
@Component
public class PlcListByType {
    /*
     * UsbDevice="1"
     * Ethernet="1"
     * 获取 port 通讯协议 是 USB的
     * */
    public List<PlcInfo> getPlcListByType(String comType, String comvalue) {
        PlcTypeParser.doParse();
        List<Plc> usbs = PlcTypeQuerier.getInstance().query(comType, comvalue);
        ArrayList<PlcInfo> plcSetings = new ArrayList<PlcInfo>();
        for (Plc c : usbs) {
            PlcInfo plcInfo = new PlcInfo();
            plcInfo.ptype = c.getAttributes().get("Type");
            plcInfo.type = c.getAttributes().get("plctype");
            //通讯口   写死 com  com2  ethernet  USB  用这个主标识
            if (comType.equals("Ethernet")) {
                plcInfo.port = "Ethernet";
            } else if (comType.equals("UsbDevice")) {
                plcInfo.port = "USB";
            }

            plcInfo.check_bit = c.getAttributes().get("CheckBit");
            plcInfo.data_length = paseInt(c.getAttributes().get("DataLength"));
            plcInfo.stop_bit = paseInt(c.getAttributes().get("StopBit"));
            plcInfo.comtype = paseInt(c.getAttributes().get("ComType"));
            //这边是默认值，可输入
            plcInfo.baudrate = c.getAttributes().get("BoudRate");
            plcInfo.wait_timeout = paseInt(c.getAttributes().get("WaitTimeout"));
            plcInfo.rev_timeout = paseInt(c.getAttributes().get("RevTimeout"));
            plcInfo.box_stat_no = paseInt(c.getAttributes().get("PlcStatNo"));
            if (null == c.getAttributes().get("Ethernet")) {
                plcInfo.plc_stat_no = paseInt(c.getAttributes().get("HmiStatNo"));
            }

//            //连续长度 自己输
//            plcInfo.com_iodelaytime = param.com_iodelaytime;
//            plcInfo.com_stepinterval = param.com_stepinterval;

            //以太网才有的参数
            //网络类型   多选框
            if (null != c.getAttributes().get("Ethernet")) {

                plcInfo.net_type = Integer.valueOf(c.getAttributes().get("NETTYPE"));
                plcInfo.net_ipaddr = c.getAttributes().get("NETIPaddr");
                //   plcInfo.net_isbroadcast = param.net_isbroadcast; plcInfo.net_broadcastaddr = param.net_broadcastaddr;
                plcInfo.net_port = Integer.valueOf(c.getAttributes().get("NETPLCPort"));
            }
            plcSetings.add(plcInfo);
        }
        return plcSetings;
    }

    public int paseInt(String str) {
        if (null != str) {
            return Integer.parseInt(str);
        } else {
            return 0;
        }
    }
}
