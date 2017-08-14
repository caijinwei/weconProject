package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.ViewAccountRoleView;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
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

    @Label("实时历史监控点")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("showReal")
    public Output showRealpoint(@RequestParam("view_id") Integer viewId, @RequestParam("type") Integer type) {
        if (viewId == null || type == null) {
            return new Output();
        }
        List<ViewAccountRoleView> list = viewAccountRoleApi.getViewAccountRoleViewByViewID(type, viewId);
        JSONObject data = new JSONObject();
        data.put("list", list);
        System.out.println(data);
        return new Output(data);
    }
    /*
    *管理用户分配监控点展示（未分配）
    * */

    @Label("未分配实时历史监控点展示")
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
    @RequestMapping(value = "setViewPoint")
    public Output setViewPoint(@RequestParam("rights") String rights, @RequestParam("selectedId") String selectedId, @RequestParam("viewId") Integer viewId) {
        String[] ids = selectedId.split(",");
        String[] rightParams = rights.split(",");
        viewAccountRoleApi.setViewPoint(viewId, ids,rightParams);
        return new Output();
    }
}
