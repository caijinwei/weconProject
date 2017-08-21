package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AccountDirApi;
import com.wecon.box.entity.AccountDir;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by zengzhipeng on 2017/8/8.
 */
@RestController
@RequestMapping("userdiract")
public class AccountDirAction {
    @Autowired
    private AccountDirApi accountDirApi;

    @Description("获取用户指定类型的分组列表")
    @RequestMapping(value = "/getuserdirs")
    @WebApi(forceAuth = true, master = true, authority = {"1", "2"})
    public Output getAccountDirList(@RequestParam("type") Integer type) {
        if (type < 0 || type > 3) {
            throw new BusinessException(ErrorCodeOption.UserGroupTypeIsUndefined.key, ErrorCodeOption.UserGroupTypeIsUndefined.value);
        }
        List<AccountDir> list = accountDirApi.getAccountDirList(AppContext.getSession().client.userId, type);
        if (list.size() == 0) {
            AccountDir model = new AccountDir();
            model.account_id = AppContext.getSession().client.userId;
            model.name = "默认组";
            model.type = type;
            accountDirApi.addAccountDir(model);
            list = accountDirApi.getAccountDirList(AppContext.getSession().client.userId, type);
        }
        JSONObject data = new JSONObject();
        data.put("list", list);
        return new Output(data);
    }

    @Description("保存用户分组")
    @RequestMapping(value = "/saveuserdir")
    @WebApi(forceAuth = true, master = true, authority = {"1", "2"})
    public Output saveAccountDir(@Valid UserDirParam param) {
        AccountDir model = new AccountDir();
        model.account_id = AppContext.getSession().client.userId;
        model.id = param.id;
        model.name = param.name;
        model.type = param.type;
        model.device_id=param.device_id;
        if (model.id > 0) {
            accountDirApi.updateAccountDir(model);
        } else {
            accountDirApi.addAccountDir(model);
        }
        return new Output();
    }

    @Description("删除分组")
    @RequestMapping(value = "/deluserdir")
    @WebApi(forceAuth = true, master = true, authority = {"1", "2"})
    public Output delAccountDir(@RequestParam("id") Integer id) {
        AccountDir dir = accountDirApi.getAccountDir(id);
        if (dir != null && dir.account_id == AppContext.getSession().client.userId) {
            accountDirApi.delAccountDir(id);
        } else {
            throw new BusinessException(ErrorCodeOption.OnlyOperateOneselfGroup.key, ErrorCodeOption.OnlyOperateOneselfGroup.value);
        }
        return new Output();
    }

    @Description("获取首页盒子分组列表（包含绑定的盒子）")
    @RequestMapping(value = "/getmainboxs")
    @WebApi(forceAuth = true, master = true, authority = {"1", "2"})
    public Output getMainBoxs() {
        if (AppContext.getSession().client.userInfo.getUserType() == 1) {
            //管理帐号直接根据绑定盒子展示
            //1.默认分组获取所有绑定的盒子

            //2.根据分组，获取各个分组的绑定的盒子

        } else if (AppContext.getSession().client.userInfo.getUserType() == 2) {
            //视图帐号，根据拥有的监控点权限去获取盒子列表

        }


        return new Output();
    }
}

class UserDirParam {
    @Label("分组ID")
    @NotNull
    public Long id;
    @Label("分组名称")
    @NotNull
    @Length(max = 32, min = 1)
    public String name;
    @Label("分组分类")
    @NotNull
    @Range(min = 0, max = 3)
    public Integer type;
    @Label("设备ID")
    @NotNull
    public Long device_id;
    

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Integer type) {
        this.type = type;
    }

	public void setDevice_id(Long device_id) {
		this.device_id = device_id;
	}
    
}
