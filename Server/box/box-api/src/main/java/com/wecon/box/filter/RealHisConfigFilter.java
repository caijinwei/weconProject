package com.wecon.box.filter;

/**
 * Created by caijw on 2017/12/23.
 */
public class RealHisConfigFilter {
    public Long id;
    public Long data_id;
    public Long account_id;
    public Long plc_id;
    public String name;// 名称
    public String addr;// 地址
    public Integer addr_type;// 0：位地址 1：字地址 2：双字
    public String describe;// 描述
    public String digit_count;// 整数位数，小数位数
    public String data_limit;// 数据范围
    public Integer his_cycle;// 历史数据采集周期
    public Integer data_type;// 0：实时数据 1：历史数据
    public Integer state;// 状态:1-启用; 0-未启用
    public Integer bind_state;// 1.绑定状态 0.解绑状态'
    public Long device_id;
    public String rid;// 寄存器类型
    public Long dirId;// 分组id
    public String ext_unit;//单位

    @Override
    public String toString() {
        return "RealHisConfigFilter{" +
                "id=" + id +
                ", data_id=" + data_id +
                ", account_id=" + account_id +
                ", plc_id=" + plc_id +
                ", name='" + name + '\'' +
                ", addr='" + addr + '\'' +
                ", addr_type=" + addr_type +
                ", describe='" + describe + '\'' +
                ", digit_count='" + digit_count + '\'' +
                ", data_limit='" + data_limit + '\'' +
                ", his_cycle=" + his_cycle +
                ", data_type=" + data_type +
                ", state=" + state +
                ", bind_state=" + bind_state +
                ", device_id=" + device_id +
                ", rid='" + rid + '\'' +
                ", dirId=" + dirId +
                ", ext_unit='" + ext_unit + '\'' +
                '}';
    }

}
