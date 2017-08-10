package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.ViewAccountRoleView;
import com.wecon.restful.annotation.WebApi;
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

    @Label("实时监控点")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping("showReal")
    public Output showRealpoint(@RequestParam("view_id") Integer viewId,@RequestParam("type") Integer type)
    {
        if(viewId==null||type==null)
        {
            return new Output();
        }
        List<ViewAccountRoleView> list=viewAccountRoleApi.getViewAccountRoleViewByViewID(type,viewId);

        JSONObject data=new JSONObject();
        data.put("list",list);
        System.out.println(data);
        return new Output(data);
    }
}
