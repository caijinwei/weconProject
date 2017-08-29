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

	private JSONObject createOption(int value, String text) {
		JSONObject option = new JSONObject();
		option.put("value", value);
		option.put("text", text);
		return option;
	}
}
