package com.wecon.box.param;

/**
 * Created by caijinw on 2017/8/7.
 */
public class PlcInfoData
{
    public long plcId;
    public String type;
    public String port;
    public String comtype;
    public Integer state;

    @Override
    public String toString() {
        return "PlcInfoData{" +
                "plcId=" + plcId +
                ", type='" + type + '\'' +
                ", port='" + port + '\'' +
                ", comtype='" + comtype + '\'' +
                '}';
    }
}
