package com.wecon.box.entity;


import java.sql.Timestamp;

/**
 * @author whp
 */
public class RealHisCfgExtend extends RealHisCfgDevice{
	public String com;
	public long addr_id;
	public String upd_time;
	public static String[] UPDATE_REAL_HIS_FIELD_FILTER = new String[]{"com", "addr_id", "upd_time", "data_type", "data_id", "name", "addr", "addr_type", "digit_count", "data_limit", "his_cycle"};

}
