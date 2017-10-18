package com.wecon.box.entity;

import java.sql.Timestamp;

/**
 * @author whp
 * 2017年10月11日
 */
public class AppVerInfo {
	public long id;
	public int platform;
	public String version_code;
	public String url;
	public String updateContent;
	public int isforce;
	public Timestamp create_date;
	public Timestamp update_date;
}
