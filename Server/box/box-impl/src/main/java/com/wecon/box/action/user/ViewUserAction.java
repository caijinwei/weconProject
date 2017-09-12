package com.wecon.box.action.user;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.entity.Account;
import com.wecon.box.entity.Page;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.box.filter.AccountFilter;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
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
@Label("操作视图帐号、帐号查询")
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
        //<editor-fold desc="操作日志">
        dbLogUtil.addOperateLog(OpTypeOption.AddViewUser, ResTypeOption.Account, viewUserInfo.account_id, viewUserInfo);
        //</editor-fold>
        return new Output();
    }

    @RequestMapping("user/chgviewuserstate")
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    public Output chgViewUserState(@Valid ViewUserChgStateParam param) {
        Account viewUserInfo = accountApi.getAccount(param.user_id);
        Account viewUserInfoOld = accountApi.getAccount(param.user_id);
        viewUserInfo.state = param.state;
        accountApi.updateAccountState(viewUserInfo);
        //<editor-fold desc="操作日志">
        dbLogUtil.updOperateLog(OpTypeOption.UpdViewUser, ResTypeOption.Account, viewUserInfo.account_id, viewUserInfoOld, viewUserInfo);
        //</editor-fold>
        return new Output();
    }

    @RequestMapping("user/getallusers")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output getAllUserList(@Valid AllUserSearchParam param, @RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
        AccountFilter filter = new AccountFilter();
        filter.account_id = param.account_id;
        filter.alias = param.alias;
        filter.state = param.state;
        filter.type = param.type;

        Page<Account> page = accountApi.getAccountList(filter, pageIndex, pageSize);
        JSONObject data = new JSONObject();
        data.put("page", page);
        return new Output(data);
    }


    @Label("超管用户修改用户密码或状态")
    @RequestMapping("user/chgInfo")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output chgAccountInfo(@RequestParam("account_id") long accountId, @RequestParam("state") Integer state, @RequestParam("password") String password) {
        Account oldAcc = accountApi.getAccount(accountId);
        Account newAcc = accountApi.getAccount(accountId);
        if (CommonUtils.isNotNull(password)&&!password.equals("")) {
            newAcc.password = password;
            accountApi.updatePwd(accountId, password);
            //<editor-fold desc="操作日志">
            dbLogUtil.updOperateLog(OpTypeOption.ChgPwd, ResTypeOption.Account, accountId, oldAcc, newAcc);
            //</editor-fold>
        } else if (state != null) {
            newAcc.state = state;
            accountApi.updateAccountState(newAcc);
            //<editor-fold desc="操作日志">
            dbLogUtil.updOperateLog(OpTypeOption.SetUserState, ResTypeOption.Account, accountId, oldAcc, newAcc);
            //</editor-fold>
        }
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

class ViewUserChgStateParam {
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

class AllUserSearchParam {
    @Label("用户名")
    public Long account_id;
    @Label("用户名/手机号/邮箱")
    public String alias;
    @Label("状态")
    @Range(min = -1, max = 1)
    public Integer state;
    @Label("用户类型")
    public Integer type;

    public void setAccount_id(Long account_id) {
        this.account_id = account_id;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}