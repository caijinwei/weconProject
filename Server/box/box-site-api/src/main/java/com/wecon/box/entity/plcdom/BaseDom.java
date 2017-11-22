package com.wecon.box.entity.plcdom;

import org.dom4j.Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by whp on 2017/8/19.
 */
public class BaseDom {
    private Map<String, String> attributes;

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributeLst){
        if(null == attributeLst){
            return;
        }
        attributes = new HashMap<String, String>();
        for(Attribute attr : attributeLst){
            attributes.put(attr.getName(), attr.getValue());
        }
    }
}
