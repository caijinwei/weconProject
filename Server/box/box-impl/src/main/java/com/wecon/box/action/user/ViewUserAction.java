package com.wecon.box.action.user;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.entity.Account;
import com.wecon.box.entity.Page;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by zengzhipeng on 2017/8/7.
 */
@Label("操作视图帐号")
@RestController
public class ViewUserAction extends UserBaseAction {

    @RequestMapping("user/getviewusers")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output getViewUserList(@RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
        Page<Account> page = accountApi.getViewAccountList(AppContext.getSession().client.userId, pageIndex, pageSize);
        JSONObject data = new JSONObject();
        data.put("page", page);
        return new Output(data);
    }

    @RequestMapping("user/addviewuser")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output addViewUser(@Valid ViewUserCreateParam param) {
        Account viewUserInfo = new Account();
        viewUserInfo.username = param.username;
        viewUserInfo.password = param.password;
        viewUserInfo.state = param.state;
        accountApi.addViewAccount(AppContext.getSession().client.userId, viewUserInfo);
        return new Output();
    }

    @RequestMapping("user/chgviewuserstate")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output chgViewUserState(@Valid ViewUserChgStateParam param) {
        Account viewUserInfo = accountApi.getAccount(param.user_id);
        viewUserInfo.state = param.state;
        accountApi.updateAccount(viewUserInfo);
        return new Output();
    }
}

class ViewUserCreateParam {
    @Label("用户名")
    @NotNull
    @Length(max = 32, min = 1)
    public String username;
    @Label("密码md5")
    @NotNull
    @Length(max = 32, min = 32)
    public String password;
    @Label("状态")
    @NotNull
    @Range(min = 0, max = 1)
    public Integer state;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}

class ViewUserChgStateParam{
    @Label("用户Id")
    @NotNull
    public Long user_id;

    @Label("状态")
    @NotNull
    @Range(min = 0, max = 1)
    public Integer state;

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}