package com.wecon.box.entity;

import java.util.List;

/**
 * Created by whp on 2017/8/28.
 */
public class BaseCfgCom<T> {
    public String com;
    public List<T> cfg_list;
    public String getCom() {
        return com;
    }

    public void setCom(String com) {
        this.com = com;
    }

    public List<T> getCfg_list() {
        return cfg_list;
    }

    public void setCfg_list(List<T> cfg_list) {
        this.cfg_list = cfg_list;
    }
    
}
