package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.LogAccountApi;
import com.wecon.box.entity.LogAccount;
import com.wecon.box.entity.LogAccountFilter;
import com.wecon.box.entity.Page;
import com.wecon.box.util.OptionUtil;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by zengzhipeng on 2017/8/30.
 */
@RestController
@RequestMapping("/oplogaction")
public class OperateLogAction {
    @Autowired
    private LogAccountApi logAccountApi;

    @Autowired
    private OptionUtil optionService;

    @Description("获取枚举信息")
    @RequestMapping(value = "/getlogrel")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output getRelInfo() {
        JSONObject data = new JSONObject();
        data.put("OpTypeOption", optionService.getOpTypeOptions());
        data.put("ResTypeOption", optionService.getResTypeOptions());
        return new Output(data);
    }

    @Description("获取日志列表")
    @RequestMapping(value = "/getloglist")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output getLogList(@Valid LogAccountFilter filter, @RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) {
        Page<LogAccount> page = logAccountApi.getLogList(filter, pageIndex, pageSize);
        JSONObject data = new JSONObject();
        data.put("page", page);
        return new Output(data);
    }
}
