package com.wecon.box.util;

import com.wecon.box.entity.plcdom.Plc;
import com.wecon.common.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by win7 on 2017/8/19.
 */
public class PlcTypeQuerier {
    private volatile static PlcTypeQuerier instance;

    private PlcTypeQuerier() {}

    public static PlcTypeQuerier getInstance() {
        if (instance == null) {
            synchronized (PlcTypeQuerier.class) {
                if (instance == null) {
                    instance = new PlcTypeQuerier();
                }
            }
        }
        return instance;
    }

    /**
     * 根据plc标签属性的name和value查询
     * @param key
     * @param value
     * @return
     */
    public List<Plc> query(String key, String value){
        if(CommonUtils.isNullOrEmpty(key) || CommonUtils.isNullOrEmpty(value)){
            return null;
        }
        List<Plc> localPlcTypeData = PlcTypeCache.localPlcTypeData;
        List<Plc> result = null;
        if(null != localPlcTypeData){
            result = new ArrayList<Plc>();
            for(Plc plc : localPlcTypeData){
                Map<String, String> attrs = plc.getAttributes();
                if(null != attrs){
                    if(value.equals(attrs.get(key))){
                        result.add(plc);
                    }
                }
            }
        }
        return result;
    }
}

