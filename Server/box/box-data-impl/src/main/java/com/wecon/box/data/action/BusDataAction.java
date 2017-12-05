package com.wecon.box.data.action;

import com.wecon.box.param.BusinessDataParam;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


/**
 * Created by whp on 2017/8/8.
 */
@Label("获取业务数据")
@RestController
public class BusDataAction {
    @Autowired
    private com.wecon.box.action.data.BusinessDataAction busDataAct;
    /**
     * 获取实时数据
     * @param param
     * @return
     */
    @RequestMapping("we-data/data/real")
    @WebApi(forceAuth = true, master = true)
    public Output getRealData(@Valid BusinessDataParam param) {

        return busDataAct.getRealData(param);
    }

    /**
     * 获取历史数据
     * @param param
     * @return
     */
    @RequestMapping("we-data/data/history")
    @WebApi(forceAuth = true, master = true)
    public Output getHistoryData(@Valid BusinessDataParam param) {

        return busDataAct.getHistoryData(param);
    }

    /**
     * 获取报警数据
     * @param param
     * @return
     */
    @RequestMapping("we-data/data/alarm")
    @WebApi(forceAuth = true, master = true)
    public Output getAlarmData(@Valid BusinessDataParam param) {

        return busDataAct.getAlarmData(param);
    }

    /**
     * 获取盒子列表
     * @return
     */
    @RequestMapping("we-data/data/boxs")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output getBoxData(@Valid BusinessDataParam param) {

        return busDataAct.getBoxData(param);
    }

    /**
     * 获取分组数据
     * @return
     */
    @RequestMapping("we-data/data/groups")
    @WebApi(forceAuth = true, master = true)
    public Output getGroupData(@Valid BusinessDataParam param) {

        return busDataAct.getGroupData(param);
    }

    /**
     * 获取监控点列表
     * @return
     */
    @RequestMapping("we-data/data/monitors")
    @WebApi(forceAuth = true, master = true)
    public Output getMonitorData(@Valid BusinessDataParam param) {

        return busDataAct.getMonitorData(param);
    }

    /**
     * 获取实时数据详情
     * @return
     */
    @RequestMapping("we-data/data/realdetail")
    @WebApi(forceAuth = true, master = true)
    public Output getRealDetail(BusinessDataParam param) {

        return busDataAct.getRealDetail(param);
    }
}
