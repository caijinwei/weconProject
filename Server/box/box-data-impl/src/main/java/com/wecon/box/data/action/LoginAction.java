package com.wecon.box.data.action;

import com.wecon.box.action.user.UserBaseAction;
import com.wecon.box.param.SigninParam;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by zengzhipeng on 2017/8/2.
 */
@Label("登录")
@RestController
public class LoginAction extends UserBaseAction {
    @Autowired
    private com.wecon.box.action.user.SigninAction signinAction;
    @RequestMapping("user/login")
    @WebApi(forceAuth = false, master = true)
    public Output signin(@Valid SigninParam param) {

        return signinAction.signin(param);
    }
}

