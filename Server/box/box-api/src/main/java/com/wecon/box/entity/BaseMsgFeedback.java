package com.wecon.box.entity;

import java.util.List;
import java.util.Map;

/**
 * 盒子反馈给服务端的基本消息结构
 * Created by whp on 2017/8/24.
 */
public class BaseMsgFeedback<T> {
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_FAIL = 0;
    private int act; //反馈的消息类型
    private int feedback_act; //要求反馈的消息类型
    private String machine_code; //设备码
    private String msg;
    private int code; //1-消息接收成功，0-接收失败
    T data;

    public int getAct() {
        return act;
    }

    public void setAct(int act) {
        this.act = act;
    }

    public int getFeedback_act() {
        return feedback_act;
    }

    public void setFeedback_act(int feedback_act) {
        this.feedback_act = feedback_act;
    }

    public String getMachine_code() {
        return machine_code;
    }

    public void setMachine_code(String machine_code) {
        this.machine_code = machine_code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public class UpdComFeedBack {
        private List<Map> upd_com_list;
        public List<Map> getUpd_com_list() {
            return upd_com_list;
        }

        public void setUpd_com_list(List<Map> upd_com_list) {
            this.upd_com_list = upd_com_list;
        }

    }

}
