package com.wecon.box.param;

/**
 * Created by Administrator on 2017/12/23.
 */
public class CopyDeviceParam {

    public String fromDeviceId;

    public String toDeviceId;

    public String isCopyBaseInfo;

    public String isCopyCom;

    public String isCopyReal;

    public String isCopyHis;

    public String isCopyAlarm;

    public void setFromDeviceId(String fromDeviceId) {
        this.fromDeviceId = fromDeviceId;
    }

    public void setToDeviceId(String toDeviceId) {
        this.toDeviceId = toDeviceId;
    }

    public void setCopyBaseInfo(String copyBaseInfo) {
        isCopyBaseInfo = copyBaseInfo;
    }

    public void setCopyReal(String copyReal) {
        isCopyReal = copyReal;
    }

    public void setCopyCom(String copyCom) {
        isCopyCom = copyCom;
    }

    public void setCopyHis(String copyHis) {
        isCopyHis = copyHis;
    }

    public void setCopyAlarm(String copyAlarm) {
        isCopyAlarm = copyAlarm;
    }

    @Override
    public String toString() {
        return "CopyDeviceParam{" +
                "fromDeviceId='" + fromDeviceId + '\'' +
                ", toDeviceId='" + toDeviceId + '\'' +
                ", isCopyBaseInfo='" + isCopyBaseInfo + '\'' +
                ", isCopyCom='" + isCopyCom + '\'' +
                ", isCopyReal='" + isCopyReal + '\'' +
                ", isCopyHis='" + isCopyHis + '\'' +
                ", isCopyAlarm='" + isCopyAlarm + '\'' +
                '}';
    }
}
