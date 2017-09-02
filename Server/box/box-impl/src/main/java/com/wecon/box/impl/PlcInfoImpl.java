package com.wecon.box.impl;

import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.entity.PlcExtend;
import com.wecon.box.entity.PlcInfo;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.restful.core.BusinessException;
import com.wecon.common.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by caijinw on 2017/8/5.
 */
@Component
public class PlcInfoImpl implements PlcInfoApi {

    @Autowired
    public PlatformTransactionManager transactionManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final String SEL_COL = "plc_id,device_id,type,driver,box_stat_no,plc_stat_no,port,comtype,baudrate,stop_bit,data_length,check_bit,retry_times,wait_timeout,rev_timeout,com_stepinterval,com_iodelaytime,retry_timeout,net_port,net_type,net_isbroadcast,net_broadcastaddr,net_ipaddr,state,create_date,update_date";

    /*

    通讯口配置
    * */
    @Override
    public long savePlcInfo(PlcInfo model) {

        System.out.println("通讯口配置获取到的model值是" + model);
        /*
        * INSERT INTO plc_info (device_id,type,driver,box_stat_no,plc_stat_no,port,comtype,baudrate,stop_bit,
            data_length,check_bit,retry_times,wait_timeout,rev_timeout,com_stepinterval,com_iodelaytime,retry_timeout,net_port,net_type,net_isbroadcast,net_broadcastaddr
            ,net_ipaddr,state,create_date,update_date) VALUES("1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1",
            "1","1","1",NOW(),NOW());
*/

        String sql = "INSERT INTO plc_info (device_id,type,driver,box_stat_no,plc_stat_no,port,comtype,baudrate,stop_bit,\n" +
                "data_length,check_bit,retry_times,wait_timeout,rev_timeout,com_stepinterval,com_iodelaytime,retry_timeout,net_port,net_type,net_isbroadcast,net_broadcastaddr\n" +
                ",net_ipaddr,state,create_date,update_date) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())";

        Object args[] = {model.device_id, model.type, model.driver, model.box_stat_no, model.plc_stat_no, model.port, model.comtype, model.baudrate, model.stop_bit, model.data_length, model.check_bit,
                model.retry_times, model.wait_timeout, model.rev_timeout, model.com_stepinterval, model.com_iodelaytime, model.rev_timeout, model.net_port, model.net_type, model.net_isbroadcast, model.net_broadcastaddr,
                model.net_ipaddr, model.state};
        System.out.println("--------------------------"+args.toString());
        return jdbcTemplate.update(sql, args);

    }

    /*
    * 展示所有通讯口部分信息
    * */
    public List<PlcInfo> showAllPlcInfoByDeviceId(Integer deviceId) {
        Object args[] = {deviceId};
        String sql = "SELECT plc_id,port,comtype,type,state FROM plc_info WHERE device_id=? ";
        return jdbcTemplate.query(sql, args, new RowMapper() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                PlcInfo model = new PlcInfo();
                model.plc_id = resultSet.getLong("plc_id");
                model.port = resultSet.getString("port");
                model.comtype = resultSet.getInt("comtype");
                model.type = resultSet.getString("type");
                model.state=resultSet.getInt("state");
                return model;
            }
        });

    }

    @Override
    public boolean updatePlcInfo(PlcInfo model) {

        String sql = "UPDATE plc_info SET  " +
                "device_id=?,type=?,driver=?,box_stat_no=?,plc_stat_no=?,port=?,comtype=?,baudrate=?,stop_bit=?, " +
                "data_length=?,check_bit=?,retry_times=?,wait_timeout=?,rev_timeout=?,com_stepinterval=?,com_iodelaytime=?, " +
                "retry_timeout=?,net_port=?,net_type=?,net_isbroadcast=?,net_broadcastaddr=?,net_ipaddr=?,state=?,update_date=NOW()" +
                "WHERE plc_id=?";
        Object args[] = {model.device_id, model.type, model.driver, model.box_stat_no, model.plc_stat_no, model.port, model.comtype, model.baudrate, model.stop_bit, model.data_length, model.check_bit,
                model.retry_times, model.wait_timeout, model.rev_timeout, model.com_stepinterval, model.com_iodelaytime, model.rev_timeout, model.net_port, model.net_type, model.net_isbroadcast, model.net_broadcastaddr,
                model.net_ipaddr, model.state, model.plc_id};
        System.out.println(model.toString());
        jdbcTemplate.update(sql, args);
        return true;
    }

    @Override
    public PlcInfo getPlcInfo(long plc_id) {
    	String sql = "select " + SEL_COL + " from plc_info where plc_id=?";
		List<PlcInfo> list = jdbcTemplate.query(sql, new Object[] { plc_id }, new DefaultPlcInfoRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
    }

    @Override
    public void delPlcInfo(long plc_id) {

    }

    @Override
    public List<PlcInfo> getListPlcInfo(long device_id) {

        String sql = "select " + SEL_COL + " from plc_info where device_id=?";
        List<PlcInfo> list = jdbcTemplate.query(sql, new Object[]{device_id}, new DefaultPlcInfoRowMapper());
        if (!list.isEmpty()) {
            return list;
        }
        return null;
    }
    @Override
    public PlcInfo findPlcInfoByPlcId(Integer plcId) {
        String sql="select "+SEL_COL+"  FROM plc_info where plc_id=?";
       PlcInfo info=jdbcTemplate.queryForObject(sql,new Object[]{plcId},new DefaultPlcInfoRowMapper());
        return info;
    }

	@Override
	public List<PlcExtend> getPlcExtendListByState(int... state){
		String sql = "select p.*, d.machine_code from plc_info p, device d where p.device_id = d.device_id and (p.state = ? or p.state = ?)";
		List<PlcExtend> list = jdbcTemplate.query(sql, new Object[] {state[0], state[1] }, new DefaultPlcExtendRowMapper());
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}

	@Override
	public boolean batchUpdateState(final List<int[]> updList){
		if(null == updList || updList.size() == 0){
			return false;
		}
		String sql = "update plc_info set state = ? where plc_id = ?";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			public int getBatchSize() {
				return updList.size();
				//这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
			}
			public void setValues(PreparedStatement ps, int i)throws SQLException {
				int[] arg = updList.get(i);
				ps.setInt(1, arg[0]);
				ps.setInt(2, arg[1]);
			}
		});
		return true;
	}

    @Override
    public boolean batchDeletePlc(final List<Integer> ids){
        if(null == ids || ids.size() == 0){
            return false;
        }
        StringBuilder idSb = new StringBuilder();
        for(int id : ids){
            idSb.append(",").append(id);
        }
        String sql = "delete from plc_info where plc_id in("+idSb.substring(1)+")";
        jdbcTemplate.update(sql);

        return true;
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

    @Override
    public boolean isExistPort(long device_id, String port) {
        if (null == port) {
            return false;
        }
        Object args[] = {device_id, port};
        String sql = "SELECT COUNT(port) FROM plc_info WHERE device_id=? AND port=?";
        Integer count = jdbcTemplate.queryForObject(sql, args, Integer.class);
        if (count > 0) {
            return true;
        }
        return false;
    }

    public void unBundledPlc(final Integer plcId) {
        /*
        * UPDATE alarm_cfg a set   a.bind_state=0; WHERE a.plc_id=3;

        UPDATE real_his_cfg a set a.bind_state=0 WHERE  a.plc_id=7;

        UPDATE plc_info a set a.state=3 WHERE a.plc_id=34;


        * */
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        try {
            tt.execute(new TransactionCallback() {
                           @Override
                           public Object doInTransaction(TransactionStatus ts) {
                               jdbcTemplate.update(" UPDATE alarm_cfg a set   a.bind_state=0 WHERE a.plc_id=?", new Object[]{plcId});
                               jdbcTemplate.update("  UPDATE real_his_cfg a set a.bind_state=0 WHERE  a.plc_id=?", new Object[]{plcId});
                               jdbcTemplate.update("UPDATE plc_info a set a.state=3 WHERE a.plc_id=?", new Object[]{plcId});
                               return true;
                           }
                       }
            );
        } catch (Exception e) {

            throw new BusinessException(ErrorCodeOption.AccountExisted.key, ErrorCodeOption.AccountExisted.value);
//            throw new RuntimeException(e);
        }
    }



	public static final class DefaultPlcExtendRowMapper implements RowMapper<PlcExtend> {
		@Override
		public PlcExtend mapRow(ResultSet rs, int i) throws SQLException {
			PlcExtend model = new PlcExtend();
			model.plc_id = rs.getLong("plc_id");
			model.com = model.plc_id+"";
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
			model.upd_time = TimeUtil.getYYYYMMDDHHMMSSDate(model.update_date);
			model.machine_code = rs.getString("machine_code");
			return model;
		}
	}

}
