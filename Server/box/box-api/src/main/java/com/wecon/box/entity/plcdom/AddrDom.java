package com.wecon.box.entity.plcdom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by win7 on 2017/8/19.
 */
public class AddrDom extends BaseDom{
    private List<Res> resList = new ArrayList<Res>();

    public List<Res> getResList() {
        return resList;
    }

    public void setResList(List<Res> resList) {
        this.resList = resList;
    }
}
