package com.wecon.box.util;

import com.wecon.box.entity.DeviceUse;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/8.
 */
public class DeviceUseQuerier {

    public static List<DeviceUse> allDeviceUses;
    public static Map<Integer,String> allDeviceUsesMap;
    static {
        //进行初始化获取参数
        System.out.println("111");
        init();
    }
    public static void init(){
        allDeviceUses =new ArrayList<DeviceUse>();
        allDeviceUsesMap = new HashMap<Integer,String>();
       Document dom= DOMUtil.getXMLByFilePath("/deviceUse.xml");
        Element rootElement=dom.getRootElement();
        List<Element> useElements=rootElement.elements("use");
        for(Element u :useElements){
            String useCodeValue=u.attributeValue("useCode");
            String useNameValue=u.attributeValue("useName");
            if(useCodeValue == null || useNameValue == null || useNameValue.equals("") || !VerifyUtil.isNumeric(useCodeValue)){
                continue;
            }
            allDeviceUses.add(new DeviceUse(Integer.parseInt(useCodeValue),useNameValue));
            allDeviceUsesMap.put(Integer.parseInt(useCodeValue),useNameValue);
        }
    }


//    public static void main(String[] args){
//        List<DeviceUse> allDeviceUses = DeviceUseQuerier.allDeviceUses;
//        JSONObject  data =new JSONObject();
//        data.put("data",allDeviceUses);
//        System.out.println(data.toJSONString());
//    }
}
