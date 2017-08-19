package com.wecon.box.entity.plcdom;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by win7 on 2017/8/19.
 */
public class Plc extends BaseDom{
   /* private AddrDom wordaddr;
    private AddrDom dwordaddr;
    private AddrDom bitaddr;
    private AddrDom byteaddr;*/

    //使用map是为了比较灵活，plc文件增加标签不需要修改实体
    private Map<String, AddrDom> addrs;

    public Map<String, AddrDom> getAddrs() {
        if(null == addrs){
            addrs = new HashMap<String, AddrDom>();
        }
        return addrs;
    }

    public void setAddrs(Map<String, AddrDom> addrs) {
        this.addrs = addrs;
    }

}
