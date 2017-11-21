package com.wecon.box.util;

import com.wecon.box.entity.PlcInfo;
import com.wecon.box.entity.plcdom.Plc;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by caijinw on 2017/8/24.
 */
@Component
public class PlcListByType {
    /*
     * UsbDevice="1"
     * Ethernet="1"
     * */
    public static List<PlcInfo> getPlcListByType(String comType, String comvalue) {
        PlcTypeParser.doParse();
        List<Plc> usbs = PlcTypeQuerier.getInstance().query(comType, comvalue);
        ArrayList<PlcInfo> plcSetings = new ArrayList<PlcInfo>();
        for (Plc c : usbs) {
            PlcInfo plcInfo = new PlcInfo();
            plcInfo.retry_times=2;
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
            plcInfo.driver=c.getAttributes().get("Driver");
            //这边是默认值，可输入
            plcInfo.baudrate = c.getAttributes().get("BoudRate");
            plcInfo.wait_timeout = paseInt(c.getAttributes().get("WaitTimeout"));
            plcInfo.rev_timeout = paseInt(c.getAttributes().get("RevTimeout"));
            plcInfo.plc_stat_no = paseInt(c.getAttributes().get("PlcStatNo"));
            if (null == c.getAttributes().get("Ethernet")) {
                plcInfo.box_stat_no = paseInt(c.getAttributes().get("HmiStatNo"));
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


/*
* 根据通讯协议分类（将一类通讯协议分组：设备类型）
* */
    public Map<String, List<PlcInfo>> getPlcByPtype(List<PlcInfo> list) {
        /*
        *  UsbDevice="1"
        * Ethernet="1"
        * */
        Map<String, List<PlcInfo>> result = new HashMap<String, List<PlcInfo>>();
        for (PlcInfo p : list) {
            List<PlcInfo> tmpList = new ArrayList();
            if (result.containsKey(p.ptype)) {
                tmpList = result.get(p.ptype);
            }
            tmpList.add(p);
            result.put(p.ptype, tmpList);
        }
        return result;
    }

    public Map<String, List<PlcInfo>> getPlcByType(List<PlcInfo> list) {
        Map<String, List<PlcInfo>> result = new HashMap<String, List<PlcInfo>>();
        for (PlcInfo p : list) {
            List<PlcInfo> tmpList = new ArrayList();
            if (result.containsKey(p.type)) {
                tmpList = result.get(p.type);
            }
            tmpList.add(p);
            result.put(p.type, tmpList);
        }
        return result;
    }

    /*
    * 获取com1 com2的所有对象
    * 需要算法改进   获取com  com2的lIST
    * */
    public static List<PlcInfo> getAllComType() {
        List<PlcInfo> result=new ArrayList<PlcInfo>();
        PlcTypeParser.doParse();
        List<PlcInfo> allType=getPlcListByType("plctype","");
         /*
        *  UsbDevice="1"
        * Ethernet="1"
        * */
        List<PlcInfo> restList = null;
        restList = getPlcListByType("UsbDevice", "1");
        restList.addAll(getPlcListByType("Ethernet", "1"));

       // allType.removeAll(restList);
        for(int i=0;i<allType.size();i++)
        {
            for(PlcInfo p:restList)
            {
                if(allType.get(i).type.equals(p.type))
                {
                    allType.remove(i);
                }
            }
        }

        return allType;
    }

    public static PlcInfo getPlcInfoByKey(List<PlcInfo> list,String key)
    {
        for(PlcInfo p:list)
        {
            if(key.equals(p.type))
            {
                return p;
            }
        }
        return null;
    }


    public static void main(String[] args) {
        getAllComType();
       // getAllComType1();
    }

    public static int paseInt(String str) {
        if (null != str) {
            return Integer.parseInt(str);
        } else {
            return 0;
        }
    }
}
