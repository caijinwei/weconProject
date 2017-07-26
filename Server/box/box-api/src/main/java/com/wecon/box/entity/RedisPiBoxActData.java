package com.wecon.box.entity;

import java.util.List;

/**
 * redis保存实时数据结构
 * Created by zengzhipeng on 2017/7/26.
 */
public class RedisPiBoxActData {
    public String machine_code;
    public String time;
    public List<PiBoxCom> act_time_data_list;
}
