package com.wecon.box.action.user;

import com.alibaba.fastjson.JSONObject;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Client;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by zengzhipeng on 2017/8/3.
 */
@Label("用户验证")
@RestController
public class CheckAction extends UserBaseAction {
    /**
     * 主要给加载html页面时使用，在没调用接口之前就想验证用户是否有登录，可以调用此接口
     *
     * @return
     */
    @RequestMapping("user/check")
    @WebApi(forceAuth = false, master = true)
    public Output checkSid() {
        JSONObject data = new JSONObject();
        Client client = AppContext.getSession().client;
        if (client.userId <= 0) {
            data.put("auth", "0");
        } else {
            data.put("auth", "1");
        }
        return new Output(data);
    }
}
