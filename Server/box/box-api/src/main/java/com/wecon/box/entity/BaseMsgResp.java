package com.wecon.box.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 下发通知盒子的基本消息结构
 * Created by whp on 2017/8/24.
 */
public class BaseMsgResp <T> {
    public static final int TYPE_FEEDBACK_NEED = 1;
    public static final int TYPE_FEEDBACK_UNNEED = 0;
    private int act; //消息类型
    private String machine_code; //设备码
    private int feedback; //是否需要反馈，1-需要，0-不需要
    T data;

    public int getAct() {
        return act;
    }

    public void setAct(int act) {
        this.act = act;
    }

    public String getMachine_code() {
        return machine_code;
    }

    public void setMachine_code(String machine_code) {
        this.machine_code = machine_code;
    }

    public int getFeedback() {
        return feedback;
    }

    public void setFeedback(int feedback) {
        this.feedback = feedback;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
