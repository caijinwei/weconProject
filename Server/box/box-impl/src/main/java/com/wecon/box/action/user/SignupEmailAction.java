package com.wecon.box.action.user;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.Account;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.util.CheckUtil;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Client;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by zengzhipeng on 2017/8/1.
 */
@Label("邮箱地址注册")
@RestController
public class SignupEmailAction extends UserBaseAction {
    @RequestMapping("user/signupemail")
    @WebApi(forceAuth = false, master = true)
    public Output signupByEmail(@Valid SignupEmailParam param) {
        if (!CheckUtil.isValidEmail(param.email)) {
            throw new BusinessException(ErrorCodeOption.EmailErorr.key, ErrorCodeOption.EmailErorr.value);
        }

        Client client = AppContext.getSession().client;

        Account user = accountApi.signupByEmail(param.username, param.email, param.password);
        String sid = accountApi.createSession(user, client.appid, client.fuid, client.ip, client.timestamp, ConstKey.SESSION_EXPIRE_TIME);
        JSONObject data = new JSONObject();
        data.put("sid", sid);
//        data.put("uid", user.account_id);
        return new Output(data);
    }
}

class SignupEmailParam {
    @Label("用户名")
    @NotNull
    @Length(max = 32, min = 1)
    public String username;
    @Label("邮箱地址")
    @NotNull
    @Length(max = 32, min = 1)
    public String email;
    @Label("密码md5")
    @NotNull
    @Length(max = 32, min = 32)
    public String password;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
