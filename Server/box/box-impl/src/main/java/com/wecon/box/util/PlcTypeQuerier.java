package com.wecon.box.util;

import com.wecon.box.entity.plcdom.Plc;
import com.wecon.common.util.CommonUtils;

import java.util.*;

/**
 * Created by whp on 2017/8/19.
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
     * 根据plc标签属性的name和value查询plc数据
     * @param key
     * @param value
     * @return
     */
    public List<Plc> query(String key, String value){
        List<Plc> localPlcTypeData = PlcTypeCache.localPlcTypeData;
        if(CommonUtils.isNullOrEmpty(key)){
            return null;
        }
        if(CommonUtils.isNullOrEmpty(value)){
            return localPlcTypeData;
        }
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

    /**
     * 根据plc属性的name获取所有属性值
     * @param key
     * @return
     */
    public List<String> queryValuesByKey(String key){
        if(CommonUtils.isNullOrEmpty(key)){
            return null;
        }
        List<Plc> localPlcTypeData = PlcTypeCache.localPlcTypeData;
        List<String> result = null;
        LinkedHashSet<String> set;
        if(null != localPlcTypeData){
            result = new ArrayList<String>();
            set = new LinkedHashSet<String>();
            for(Plc plc : localPlcTypeData){
                Map<String, String> attrs = plc.getAttributes();
                if(attrs.containsKey(key)){
                    set.add(attrs.get(key));
                }
            }
            result.addAll(set);
        }
        return result;
    }
}

