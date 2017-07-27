package com.wecon.box.param;

import com.wecon.box.entity.PiBoxCom;

import java.util.List;

/**
 * 盒子实时数据
 * Created by zengzhipeng on 2017/7/26.
 */
public class CtsActTimeData {
    /**
     * 采集时间(yyyy-MM-dd HH:mm:ss)
     */
    public String time;

    public List<PiBoxCom> act_time_data_list;
}
