package com.wecon.box.entity;

/**
 * Created by Administrator on 2017/12/8.
 */
public class DeviceUse
{
    public long deviceId;
    public int useCode;
    public String useName;
    public String otherUseName;

    public DeviceUse(int useCode, String useName) {
        this.useCode = useCode;
        this.useName = useName;
    }

    public DeviceUse(long deviceId, int useCode, String useName, String otherUseName) {
        this.deviceId = deviceId;
        this.useCode = useCode;
        this.useName = useName;
        this.otherUseName = otherUseName;
    }
    public DeviceUse(){
        super();
    }
}
