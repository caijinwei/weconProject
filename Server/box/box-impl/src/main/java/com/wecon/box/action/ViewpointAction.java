package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.ViewAccountRoleView;
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

/**
 * Created by caijinw on 2017/8/10.
 */
@RestController
@RequestMapping("Viewpoint")
public class ViewpointAction {

    @Autowired
    ViewAccountRoleApi viewAccountRoleApi;

    @Label("实时历史监控点")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("showReal")
    public Output showRealpoint(@RequestParam("view_id") Integer viewId, @RequestParam("type") Integer type,@RequestParam("pageIndex")Integer pageIndex,@RequestParam("pageSize") Integer pageSize)
    {
        System.out.println("pageIndex的值是"+pageIndex);
        if (viewId == null || type == null) {
            return new Output();
        }
        Page<ViewAccountRoleView> page = viewAccountRoleApi.getViewAccountRoleViewByViewID(type, viewId,pageIndex,pageSize);
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
    public Output showRealHiss(@RequestParam("view_id") Integer viewId, @RequestParam("type") Integer type, @RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
        long userId = AppContext.getSession().client.userId;
        Page<RealHisCfg> page = viewAccountRoleApi.getViewRealHisCfgByViewAndAccId(viewId, userId, type, pageIndex, pageSize);
        JSONObject data = new JSONObject();
        data.put("page", page);
        System.out.println(page.getList());
        return new Output(data);
    }

    /*
    * 这边没做  验证
    * */
    @Label("为视图账号配置监控点")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping(value = "setViewPoint")
    public Output setViewPoint(@RequestParam("rights") String rights, @RequestParam("selectedId") String selectedId, @RequestParam("viewId") Integer viewId) {
        String[] ids = selectedId.split(",");
        String[] rightParams = rights.split(",");
        viewAccountRoleApi.setViewPoint(viewId, ids, rightParams);
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

}