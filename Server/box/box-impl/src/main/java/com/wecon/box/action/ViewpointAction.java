package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.*;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by caijinw on 2017/8/10.
 */
@RestController
@RequestMapping("Viewpoint")
public class ViewpointAction {

    @Autowired
    ViewAccountRoleApi viewAccountRoleApi;
    @Autowired
    DeviceApi deviceApi;

    @Label("实时历史监控点")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("showReal")
    public Output showRealpoint(@RequestParam("view_id") Integer viewId, @RequestParam("type") Integer type, @RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
        if (viewId == null || type == null) {
            return new Output();
        }
        Page<ViewAccountRoleView> page = viewAccountRoleApi.getViewAccountRoleViewByViewID(type, viewId, pageIndex, pageSize);
        JSONObject data = new JSONObject();
        data.put("page", page);
        return new Output(data);
    }

    @Label("视图账户报警监控点")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("showAlarm")
    public Output showAlarmpoint(@RequestParam("view_id") Integer viewId, @RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
        Page<ViewAccountRoleView> page = viewAccountRoleApi.getAlarmViewAccountRoleViewByViewID(viewId, pageIndex, pageSize);
        JSONObject data = new JSONObject();
        data.put("page", page);
        return new Output(data);
    }
   /*
    *管理用户分配监控点展示（未分配）
    * */

    @Label("未分配实时历史监控点展示")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping(value = "showRealHiss")
    public Output showRealHiss(@RequestParam("view_id") Integer viewId, @RequestParam("device_id") Integer device_id, @RequestParam("type") Integer type, @RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
        long userId = AppContext.getSession().client.userId;
        Page<RealHisCfg> page = null;
        if (device_id != 0) {
            page = viewAccountRoleApi.getViewRealHisCfgByViewAndDeivceId(userId, viewId, device_id, type, pageIndex, pageSize);
        } else {
            page = viewAccountRoleApi.getViewRealHisCfgByViewAndAccId(viewId, userId, type, pageIndex, pageSize);
        }
        JSONObject data = new JSONObject();
        data.put("page", page);
        System.out.println(page.getList());
        return new Output(data);
    }

    /*
    * device_id=0 表示全部盒子的监控点
    * */
    @Label("未分配报警监控点展示")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping(value = "showAlarmrestpoint")
    public Output showAlarmrestpoint(@RequestParam("view_id") Integer viewId, @RequestParam("device_id") Integer device_id, @RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
        long account_id = AppContext.getSession().client.userId;
        Page<AlarmCfg> page = null;
        if (device_id == 0) {
            page = viewAccountRoleApi.getViewAlarmCfgByView(account_id, viewId, pageIndex, pageSize);
        } else {
            page = viewAccountRoleApi.getViewAlarmCfgByViewAndDeivceId(account_id, viewId, device_id, pageIndex, pageSize);
        }

        JSONObject data = new JSONObject();
        data.put("page", page);
        return new Output(data);
    }

    /*
    *   有赋值权限
    *   public void setViewPoint(Integer viewId, String[] ids, String[] rights ,Integer cgf_type)
    * */
    @Label("为视图账号配置（实时）监控点")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping(value = "setViewPoint")
    public Output setViewPoint(@RequestParam("rights") String rights, @RequestParam("selectedId") String selectedId, @RequestParam("viewId") Integer viewId) {
        String[] ids = selectedId.split(",");
        String[] rightParams = rights.split(",");
        viewAccountRoleApi.setViewPoint(viewId, ids, rightParams, 1);
        return new Output();
    }

    /*
    *  没有权限
    *          设rights=null
    *  cgf_type
    *           1) 1 历史
    *           2）2 报警
    * */
    @Label("为视图账户配置（报警历史）监控点")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping(value = "setViewHisAlarmPoint")
    public Output setViewHisAlarmPoint(@RequestParam("selectedId") String selectedId, @RequestParam("viewId") Integer viewId, @RequestParam("cfg_type") Integer cgf_type) {
        String[] ids = selectedId.split(",");
        viewAccountRoleApi.setViewPoint(viewId, ids, null, cgf_type);
        return new Output();
    }

    @Label("视图账户监控点解绑")
    @RequestMapping(value = "deletePoint")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output deletePoint(@RequestParam("viewId") Integer viewId, @RequestParam("roleType") Integer roleType, @RequestParam("pointId") Integer pointId) {
        if (viewId == null || roleType == null || pointId == null) {
            throw new BusinessException(ErrorCodeOption.Viewpoint_Dlete_False.key, ErrorCodeOption.Viewpoint_Dlete_False.value);
        }
        viewAccountRoleApi.deletePoint(viewId, roleType, pointId);
        return new Output();
    }

    @Label("修改视图监控点权限")
    @RequestMapping(value = "updateViewPointRoleType")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output updateViewPointRoleType(@RequestParam("viewId") Integer viewId, @RequestParam("pointId") Integer pointId, @RequestParam("roleType") Integer roleType) {
        if (pointId == null || roleType == null) {
            throw new BusinessException(ErrorCodeOption.ViewpointRoleTypePrams_Update_False.key, ErrorCodeOption.ViewpointRoleTypePrams_Update_False.value);
        }
        viewAccountRoleApi.updateViewPointRoleType(viewId, pointId, roleType);
        return new Output();
    }

    @Label("管理员账户下盒子名称")
    @RequestMapping(value = "getDeviceName")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output getDevicesName() {
        long acc_id = AppContext.getSession().client.userId;
        List<Device> list = deviceApi.getDeviceNameByAccId(acc_id);

        JSONObject data = new JSONObject();
        data.put("list", list);
        return new Output(data);
    }

}
