package com.wecon.box.action.user;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.Account;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.util.BoxWebConfigContext;
import com.wecon.box.util.EmailUtil;
import com.wecon.box.util.VerifyUtil;
import com.wecon.common.redis.RedisManager;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zengzhipeng on 2017/8/5.
 */
@Label("修改用户信息")
@RestController
public class ChangeAction extends UserBaseAction {
    @Label("修改密码")
    @RequestMapping("user/chgpwd")
    @WebApi(forceAuth = true, master = true)
    public Output changePwd(@Valid ChgPwdParam param) {
        accountApi.updateAccountPwd(AppContext.getSession().client.userId, param.oldpwd, param.newpwd);
        return new Output();
    }

    @Label("更换邮箱")
    @RequestMapping("user/chgemail")
    @WebApi(forceAuth = true, master = true)
    public Output changeEmail(@Valid ChgEmailParam param) {
        if (!VerifyUtil.isValidEmail(param.email)) {
            throw new BusinessException(ErrorCodeOption.EmailError.key, ErrorCodeOption.EmailError.value);
        }
        Client client = AppContext.getSession().client;
        Account user = accountApi.getAccount(client.userId);
        if (user.email != null && user.email.equalsIgnoreCase(param.email)) {
            throw new BusinessException(ErrorCodeOption.EmailIsSame.key, ErrorCodeOption.EmailIsSame.value);
        }
        //发送验证邮箱。等验证过后才更新到db
        String token = UUID.randomUUID().toString().replace("-", "");
        Map<String, String> map = new HashMap<String, String>();
        map.put(token, param.email);
        String redisKey = String.format(ConstKey.REDIS_EMAIL_CHANGE_TOKEN, user.account_id);
        RedisManager.hmset(ConstKey.REDIS_GROUP_NAME, redisKey, map, 3600);
        String url = BoxWebConfigContext.boxWebConfig.getEmailActiveUrl() + "?t=2&u=" + user.account_id + "&token=" + token;
        String content = "<h1>请点击下面链接完成激活操作！</h1><h3><a href='" + url + "'>" + url + "</a></h3>";
        EmailUtil.send(param.email, "邮箱更改激活邮件", content);

        return new Output();
    }

    @Label("更换手机号码")
    @RequestMapping("user/chgphonenum")
    @WebApi(forceAuth = true, master = true)
    public Output changePhonenum(@Valid ChgPhoneParam param) {
        if (!VerifyUtil.isChinaPhone(param.phonenum)) {
            throw new BusinessException(ErrorCodeOption.PhonenumError.key, ErrorCodeOption.PhonenumError.value);
        }
        //验证码是否有效
        String redisKey = String.format(ConstKey.REDIS_PHONE_SIGNIN_VERCODE, param.phonenum);
        String vercode = RedisManager.get(ConstKey.REDIS_GROUP_NAME, redisKey);
        if (vercode == null || !vercode.equals(param.vercode)) {
            throw new BusinessException(ErrorCodeOption.SmsVercodeError.key, ErrorCodeOption.SmsVercodeError.value);
        }
        Client client = AppContext.getSession().client;
        Account user = accountApi.getAccount(client.userId);
        user.phonenum = param.phonenum;
        accountApi.updateAccountPhone(user);
        JSONObject data = new JSONObject();
        data.put("phonenum", user.phonenum);
        return new Output(data);
    }
}

class ChgPwdParam {
    @Label("旧密码md5")
    @NotNull
    @Length(max = 32, min = 32)
    public String oldpwd;

    @Label("新密码md5")
    @NotNull
    @Length(max = 32, min = 32)
    public String newpwd;

    public void setOldpwd(String oldpwd) {
        this.oldpwd = oldpwd;
    }

    public void setNewpwd(String newpwd) {
        this.newpwd = newpwd;
    }
}

class ChgEmailParam {
    @Label("邮箱地址")
    @NotNull
    @Length(max = 32, min = 1)
    public String email;

    public void setEmail(String email) {
        this.email = email;
    }
}

class ChgPhoneParam {
    @Label("手机号码")
    @NotNull
    @Length(max = 16, min = 1)
    public String phonenum;

    @Label("短信验证码")
    @NotNull
    public String vercode;

    public void setVercode(String vercode) {
        this.vercode = vercode;
    }

    public void setPhonenum(String phonenum) {
        this.phonenum = phonenum;
    }
}


