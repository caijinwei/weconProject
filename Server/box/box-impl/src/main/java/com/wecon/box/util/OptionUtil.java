package com.wecon.box.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.enums.DataTypeOption;

/**
 * Created by zengzhipeng on 2017/8/24.
 */
public class OptionUtil {

    public JSONArray getDataTypeOptionOptions() {
        JSONArray options = new JSONArray();
        for (DataTypeOption it : DataTypeOption.values()) {
            options.add(createOption(it.getValue(), it.getKey()));
        }
        return options;
    }

    /**
     * 生成单个配置项的json object值
     *
     * @param value 值
     * @param text 值描述内容
     * @return 返回jsonobject（含有两个数据：value，key）
     */
    private JSONObject createOption(int value, String text) {
        JSONObject option = new JSONObject();
        option.put("value", value);
        option.put("text", text);
        return option;
    }
}
