package com.wecon.box.entity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lanpenghui
 * 2017年8月1日
 */
public class RealHisCfg {
	/**
	 * `ID` bigint(20) NOT NULL AUTO_INCREMENT, 
	 * `DATA_ID` bigint(20) DEFAULT NULL, 
	 * `ACCOUNT_ID` bigint(20) NOT NULL,
	 * `PLC_ID` bigint(20) NOT NULL,
	 * `NAME` varchar(64) NOT NULL COMMENT '名称',
	 * `ADDR` varchar(64) NOT NULL COMMENT '地址',
	 * `ADDR_TYPE` int(11) NOT NULL COMMENT '0：位地址\r\n 1：字地址\r\n 2：双字',
	 * `DESCRIBE` varchar(64) DEFAULT NULL COMMENT '描述', 
	 * `DIGIT_COUNT`varchar(32) DEFAULT NULL COMMENT '整数位数，小数位数', 
	 * `DATA_LIMIT` varchar(32)DEFAULT NULL COMMENT '数据范围',
	 * `HIS_CYCLE` int(11) DEFAULT NULL COMMENT '历史数据采集周期', 
	 * `DATA_TYPE` int(11) NOT NULL COMMENT '0：实时数据\r\n 1：历史数据',
	 * `STATE` int(11) DEFAULT NULL COMMENT '状态:1-启用; 0-未启用', 
	 * `CREATE_DATE`datetime DEFAULT NULL,
     * `UPDATE_DATE` datetime DEFAULT NULL, 
     * `bind_state` int(11) DEFAULT '1' COMMENT '1.绑定状态 0.解绑状态',
     *  ext_unit 单位，只对于字地址可以填写，可不填
	 */
	public long id;
	public long data_id;
	public long account_id;
	public long device_id;
	public long plc_id;
	public long dir_id;
	public String name;//名称
	public String addr;//地址
	public int addr_type;//0：位地址 1：字地址 2：双字
	public String describe;//描述
	public String digit_count;//整数位数，小数位数
	public String data_limit;//数据范围
	public String digit_binary;//进制
	public int his_cycle;//历史数据采集周期
	public int data_type;//0：实时数据 1：历史数据
	public int state;//状态:1-启用; 0-未启用
	public int bind_state;//1.绑定状态 0.解绑状态
	public String rid;//寄存器类型
	public String ext_unit;//单位
	public String dead_set;//死区设置(只有实时数据) 16位无符号 16位有符号 32位无符号 32位有符号 16位BCD 32位BCD--整型  64位双精度--浮点型
	public Timestamp create_date;
	public Timestamp update_date;
	public static Map<String, Boolean> operCache = new HashMap<String, Boolean>();
public static void main(String[] a){
	operCache.put("1030", false);
	if(null !=operCache.get(1030+"")){
		operCache.put(1030+"", true);
	}
	System.out.print(operCache);
}
}
