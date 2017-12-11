package com.wecon.box.action.dbsel;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.LogAccountApi;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.util.VerifyUtil;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import com.wecon.restful.doc.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;

import java.io.UnsupportedEncodingException;

/**
 * Created by zengzhipeng on 2017/12/7.
 */
@Label("查询MySql数据")
@RestController
public class MysqlAction {
    @Autowired
    protected LogAccountApi logAccountApi;

    @RequestMapping("mysql/sel")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output getViewUserList(@RequestParam("sqlstr") String sqlstr) throws UnsupportedEncodingException {
        sqlstr = URLDecoder.decode(sqlstr, "utf-8");
        if (VerifyUtil.isSelStr(sqlstr)) {
            JSONArray rs = logAccountApi.getSelData(sqlstr);
            JSONObject data = new JSONObject();
            data.put("rs", rs);
            return new Output(data);
        } else {
            throw new BusinessException(ErrorCodeOption.SqlStrIsLllegality.key,
                    ErrorCodeOption.SqlStrIsLllegality.value);
        }
    }
}
