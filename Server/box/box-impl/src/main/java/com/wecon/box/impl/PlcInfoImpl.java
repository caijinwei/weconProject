package com.wecon.box.impl;

import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.entity.PlcInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by caijinw on 2017/8/5.
 */
@Component
public class PlcInfoImpl implements PlcInfoApi{

    @Autowired
    private JdbcTemplate jdbcTemplate;
private final String SEL_COL = "plc_id,device_id,type,driver,box_stat_no,plc_stat_no,port,comtype,baudrate,stop_bit,data_length,check_bit,retry_times,wait_timeout,rev_timeout,com_stepinterval,com_iodelaytime,retry_timeout,net_port,net_type,net_isbroadcast,net_broadcastaddr,net_ipaddr,state,create_date,update_date";

    /*

    通讯口配置
    * */
    @Override
    public long savePlcInfo(PlcInfo model) {

        System.out.println("通讯口配置获取到的model值是"+model);
        /*
        * INSERT INTO plc_info (device_id,type,driver,box_stat_no,plc_stat_no,port,comtype,baudrate,stop_bit,
            data_length,check_bit,retry_times,wait_timeout,rev_timeout,com_stepinterval,com_iodelaytime,retry_timeout,net_port,net_type,net_isbroadcast,net_broadcastaddr
            ,net_ipaddr,state,create_date,update_date) VALUES("1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1",
            "1","1","1",NOW(),NOW());
*/

        String sql="INSERT INTO plc_info (device_id,type,driver,box_stat_no,plc_stat_no,port,comtype,baudrate,stop_bit,\n" +
                "data_length,check_bit,retry_times,wait_timeout,rev_timeout,com_stepinterval,com_iodelaytime,retry_timeout,net_port,net_type,net_isbroadcast,net_broadcastaddr\n" +
                ",net_ipaddr,state,create_date,update_date) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())";

        Object args[] = {model.device_id,model.type,model.driver,model.box_stat_no,model.plc_stat_no,model.port,model.comtype,model.baudrate,model.stop_bit,model.data_length,model.check_bit,
        model.retry_times,model.wait_timeout,model.rev_timeout,model.com_stepinterval,model.com_iodelaytime,model.rev_timeout,model.net_port,model.net_type,model.net_isbroadcast,model.net_broadcastaddr,
        model.net_ipaddr,model.state};
        return  jdbcTemplate.update(sql, args);

    }

    /*
    * 展示所有通讯口部分信息
    * */
    public List<PlcInfo> showAllPlcInfoByDeviceId(Integer deviceId)
    {
        Object args[]={deviceId};
        String sql="SELECT plc_id,port,comtype,type FROM plc_info WHERE device_id=?";
        return jdbcTemplate.query(sql,args,new RowMapper(){
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                PlcInfo model=new PlcInfo();
                model.plc_id=resultSet.getLong("plc_id");
                model.port=resultSet.getString("port");
                model.comtype=resultSet.getInt("comtype");
                model.type=resultSet.getString("type");
                return model;
            }
        });

    }
    @Override
    public boolean updatePlcInfo(PlcInfo model) {

        String sql="UPDATE plc_info SET" +
                "device_id=?,type,driver=?,box_stat_no=?,plc_stat_no=?,port=?,comtype=?,baudrate=?,stop_bit=?," +
                "data_length=?,check_bit=?,retry_times=?,wait_timeout=?,rev_timeout=?,com_stepinterval=?,com_iodelaytime=?," +
                "retry_timeout=?,net_port=?,net_type=?,net_isbroadcast=?,net_broadcastaddr=?,net_ipaddr=?,state=?,update_date=NOW()" +
                "WHERE plc_id=?";
        Object args[] = {model.device_id,model.type,model.driver,model.box_stat_no,model.plc_stat_no,model.port,model.comtype,model.baudrate,model.stop_bit,model.data_length,model.check_bit,
                model.retry_times,model.wait_timeout,model.rev_timeout,model.com_stepinterval,model.com_iodelaytime,model.rev_timeout,model.net_port,model.net_type,model.net_isbroadcast,model.net_broadcastaddr,
                model.net_ipaddr,model.state,model.plc_id};
    
        return true;
    }

    @Override
    public PlcInfo getPlcInfo(long plc_id) {
        return null;
    }

    @Override
    public void delPlcInfo(long plc_id) {

    }

	@Override
	public List<PlcInfo> getListPlcInfo(long device_id) {

		String sql = "select " + SEL_COL + " from plc_info where device_id=?";
		List<PlcInfo> list = jdbcTemplate.query(sql, new Object[] { device_id }, new DefaultPlcInfoRowMapper());
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}


public static final class DefaultPlcInfoRowMapper implements RowMapper<PlcInfo> {

		@Override
		public PlcInfo mapRow(ResultSet rs, int i) throws SQLException {
			PlcInfo model = new PlcInfo();
			model.plc_id = rs.getLong("plc_id");
			model.device_id = rs.getLong("device_id");
			model.type = rs.getString("type");
			model.driver = rs.getString("driver");
			model.box_stat_no = rs.getInt("box_stat_no");
			model.plc_stat_no = rs.getInt("plc_stat_no");
			model.port = rs.getString("port");
			model.comtype = rs.getInt("comtype");
			model.baudrate = rs.getString("baudrate");
			model.stop_bit = rs.getInt("stop_bit");
			model.data_length = rs.getInt("data_length");
			model.check_bit = rs.getString("check_bit");
			model.retry_times = rs.getInt("retry_times");
			model.wait_timeout = rs.getInt("wait_timeout");
			model.rev_timeout = rs.getInt("rev_timeout");
			model.com_stepinterval = rs.getInt("com_stepinterval");
			model.com_iodelaytime = rs.getInt("com_iodelaytime");
			model.retry_timeout = rs.getInt("retry_timeout");
			model.net_port = rs.getInt("net_port");
			model.net_type = rs.getInt("net_type");
			model.net_isbroadcast = rs.getInt("net_isbroadcast");
			model.net_broadcastaddr = rs.getInt("net_broadcastaddr");
			model.net_ipaddr = rs.getString("net_ipaddr");
			model.state = rs.getInt("state");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");
			return model;
		}
	}
}
