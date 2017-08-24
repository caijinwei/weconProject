package com.wecon.box.util;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wecon.box.enums.DataTypeOption;

/**
 * Created by zengzhipeng on 2017/8/24.
 */
@Component
public class OptionUtil {

    public JSONArray getDataTypeOptionOptions() {
        JSONArray options = new JSONArray();
        for (DataTypeOption it : DataTypeOption.values()) {
            options.add(createOption(it.getValue(), it.getKey()));
        }
        return options;
    }

    /**
     * 鐢熸垚鍗曚釜閰嶇疆椤圭殑json object鍊�
     *
     * @param value 鍊�
     * @param text 鍊兼弿杩板唴瀹�
     * @return 杩斿洖jsonobject锛堝惈鏈変袱涓暟鎹細value锛宬ey锛�
     */
    private JSONObject createOption(int value, String text) {
        JSONObject option = new JSONObject();
        option.put("value", value);
        option.put("text", text);
        return option;
    }
}
