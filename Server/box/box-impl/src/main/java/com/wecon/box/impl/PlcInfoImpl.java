package com.wecon.box.impl;

import com.wecon.box.api.PlcInfoApi;
import com.wecon.box.entity.PlcExtend;
import com.wecon.box.entity.PlcInfo;
import com.wecon.box.entity.PlcInfoDetail;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.common.util.TimeUtil;
import com.wecon.restful.core.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caijinw on 2017/8/5.
 */
@Component
public class PlcInfoImpl implements PlcInfoApi {

    @Autowired
    public PlatformTransactionManager transactionManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final String SEL_COL = "plc_id,device_id,type,driver,box_stat_no,plc_stat_no,port,comtype,baudrate,stop_bit,data_length,check_bit,retry_times,wait_timeout,rev_timeout,com_stepinterval,com_iodelaytime,retry_timeout,net_port,net_type,net_isbroadcast,net_broadcastaddr,net_ipaddr,state,create_date,update_date,is_sync";

    /*
    通讯口配置
    * */
    @Override
    public long savePlcInfo(final PlcInfo model) {

        /*
        * 进行通讯接口唯一性判断
        * */

        if (!model.port.equals("Ethernet")) {
            List<PlcInfo> plc_InfoList = getPortState(model.device_id, model.port);
            if (plc_InfoList.size() > 0) {
                if (plc_InfoList.get(0).state != 3) {
                    throw new BusinessException(ErrorCodeOption.Is_Exist_PlcPort.key, ErrorCodeOption.Is_Exist_PlcPort.value);
                }
            }
        }
        /*
        * INSERT INTO plc_info (device_id,type,driver,box_stat_no,plc_stat_no,port,comtype,baudrate,stop_bit,
            data_length,check_bit,retry_times,wait_timeout,rev_timeout,com_stepinterval,com_iodelaytime,retry_timeout,net_port,net_type,net_isbroadcast,net_broadcastaddr
            ,net_ipaddr,state,create_date,update_date) VALUES("1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1",
            "1","1","1",NOW(),NOW());
*/

//
//        Object args[] = {model.device_id, model.type, model.driver, model.box_stat_no, model.plc_stat_no, model.port, model.comtype, model.baudrate, model.stop_bit, model.data_length, model.check_bit,
//                model.retry_times, model.wait_timeout, model.rev_timeout, model.com_stepinterval, model.com_iodelaytime, model.rev_timeout, model.net_port, model.net_type, model.net_isbroadcast, model.net_broadcastaddr,
//                model.net_ipaddr, model.state};
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                String sql = "INSERT INTO plc_info (device_id,type,driver,box_stat_no,plc_stat_no,port,comtype,baudrate,stop_bit, " +
                        "data_length,check_bit,retry_times,wait_timeout,rev_timeout,com_stepinterval,com_iodelaytime,retry_timeout,net_port,net_type,net_isbroadcast,net_broadcastaddr  " +
                        ",net_ipaddr,state,is_sync,create_date,update_date) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP())";
                PreparedStatement preState = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                preState.setLong(1, model.device_id);
                preState.setString(2, model.type);
                preState.setString(3, model.driver);
                preState.setInt(4, model.box_stat_no);
                preState.setInt(5, model.plc_stat_no);
                preState.setString(6, model.port);
                preState.setInt(7, model.comtype);
                preState.setString(8, model.baudrate);
                preState.setInt(9, model.stop_bit);
                preState.setInt(10, model.data_length);
                preState.setString(11, model.check_bit);

                preState.setInt(12, model.retry_times);
                preState.setInt(13, model.wait_timeout);
                preState.setInt(14, model.rev_timeout);
                preState.setInt(15, model.com_stepinterval);
                preState.setInt(16, model.com_iodelaytime);
                preState.setInt(17, model.retry_timeout);
                preState.setInt(18, model.net_port);
                preState.setInt(19, model.net_type);
                preState.setInt(20, model.net_isbroadcast);
                preState.setInt(21, model.net_broadcastaddr);
                preState.setString(22, model.net_ipaddr);
                preState.setInt(23, model.state);
                preState.setInt(24, model.is_sync);
                return preState;
            }
        }, key);
        return key.getKey().longValue();
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
                model.state = resultSet.getInt("state");
                return model;
            }
        });

    }

    @Override
    public boolean updatePlcInfo(PlcInfo model) {

        String sql = "UPDATE plc_info SET  " +
                "device_id=?,type=?,driver=?,box_stat_no=?,plc_stat_no=?,port=?,comtype=?,baudrate=?,stop_bit=?, " +
                "data_length=?,check_bit=?,retry_times=?,wait_timeout=?,rev_timeout=?,com_stepinterval=?,com_iodelaytime=?, " +
                "retry_timeout=?,net_port=?,net_type=?,net_isbroadcast=?,net_broadcastaddr=?,net_ipaddr=?,state=?,update_date=CURRENT_TIMESTAMP()" +
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
        List<PlcInfo> list = jdbcTemplate.query(sql, new Object[]{plc_id}, new DefaultPlcInfoRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public PlcInfoDetail getPlcInfoDetail(long plc_id) {
        String sql = "select " + SEL_COL + ",file_md5 " + " from plc_info where plc_id=?";
        List<PlcInfoDetail> list = jdbcTemplate.query(sql, new Object[]{plc_id}, new DefaultPlcInfoDtailRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<PlcInfoDetail> getActiveListPlcInfoDetail(long device_id) {
        String sql = "select " + SEL_COL + " ,file_md5 " + " from plc_info where device_id=? AND state!=3";
        List<PlcInfoDetail> list = jdbcTemplate.query(sql, new Object[]{device_id}, new DefaultPlcInfoDtailRowMapper());
        if (!list.isEmpty()) {
            return list;
        }
        return null;
    }

    @Override
    public Map<Long, Long> copyDeviceCom(long fromDeviceId, long toDeviceId) {
        Map<Long, Long> resultMap = new HashMap<Long, Long>();

        /*
        * 判断是否已经存在通讯口配置
        * */
        List<PlcInfo> toinfoList = getListPlcInfo(toDeviceId);
        if (toinfoList != null && toinfoList.size() > 0) {
            for (PlcInfo plcInfo : toinfoList) {
                if (plcInfo.state != 3) {
                    throw new BusinessException(ErrorCodeOption.ComConfig_Is_Exist.key, ErrorCodeOption.ComConfig_Is_Exist.value);
                } else {
                    throw new BusinessException(ErrorCodeOption.ComConfig_IsNot_Del.key, ErrorCodeOption.ComConfig_IsNot_Del.value);
                }
            }
        }


        List<PlcInfo> frominfoList = getListPlcInfo(fromDeviceId);
        if (frominfoList != null && frominfoList.size() >= 0) {
            for (PlcInfo p : frominfoList) {
                if (p.state != 3) {
                    p.device_id = toDeviceId;
                    p.state = 1;
                    p.is_sync = 0;
                    resultMap.put(p.plc_id, savePlcInfo(p));
                }
            }
        }
        return resultMap;
    }


    @Override
    public void delPlcInfo(long plc_id) {
        String sql = "delete from plc_info where plc_id = ? ";

        jdbcTemplate.update(sql, new Object[]{plc_id});
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
    public PlcInfo findPlcInfoByPlcId(long plcId) {
        String sql = "select " + SEL_COL + "  FROM plc_info where plc_id=?";
        PlcInfo info = jdbcTemplate.queryForObject(sql, new Object[]{plcId}, new DefaultPlcInfoRowMapper());
        return info;
    }

    @Override
    public List<PlcInfoDetail> getListPlcInfoDetail(long device_id) {
        String sql = "select " + SEL_COL + " ,file_md5 " + " from plc_info where device_id=?";
        List<PlcInfoDetail> list = jdbcTemplate.query(sql, new Object[]{device_id}, new DefaultPlcInfoDtailRowMapper());
        if (!list.isEmpty()) {
            return list;
        }
        return null;
    }

    @Override
    public void savePlcInfoDetail(PlcInfoDetail model) {

        String sql = "UPDATE plc_info SET  " +
                "device_id=?,type=?,driver=?,box_stat_no=?,plc_stat_no=?,port=?,comtype=?,baudrate=?,stop_bit=?, " +
                "data_length=?,check_bit=?,retry_times=?,wait_timeout=?,rev_timeout=?,com_stepinterval=?,com_iodelaytime=?, " +
                "retry_timeout=?,net_port=?,net_type=?,net_isbroadcast=?,net_broadcastaddr=?,net_ipaddr=?,state=?,file_md5=?,update_date=NOW()" +
                "WHERE plc_id=?";
        Object args[] = {model.device_id, model.type, model.driver, model.box_stat_no, model.plc_stat_no, model.port, model.comtype, model.baudrate, model.stop_bit, model.data_length, model.check_bit,
                model.retry_times, model.wait_timeout, model.rev_timeout, model.com_stepinterval, model.com_iodelaytime, model.rev_timeout, model.net_port, model.net_type, model.net_isbroadcast, model.net_broadcastaddr,
                model.net_ipaddr, model.state, model.file_md5, model.plc_id};
        jdbcTemplate.update(sql, args);

    }

    @Override
    public List<PlcExtend> getPlcExtendListById(long id) {
        String sql = "select p.*, d.machine_code, r.file_md5 as f_md5  from device d, plc_info p left join  driver r on p.driver = r.driver " +
                "where p.device_id = d.device_id and d.state=1 and p.plc_id = ? order by p.update_date";
        List<PlcExtend> list = jdbcTemplate.query(sql, new Object[]{id}, new DefaultPlcExtendRowMapper());
        if (!list.isEmpty()) {
            return list;
        }
        return null;
    }

    @Override
    public List<PlcExtend> getPlcExtendListByState(Object... state) {
        String sql = "select p.*, d.machine_code, r.file_md5 as f_md5  from device d, plc_info p left join  driver r on p.driver = r.driver where p.device_id = d.device_id and d.state=1 ";
        if (null != state && state.length > 0) {
            sql += " and p.state in (";
            StringBuffer inSb = new StringBuffer();
            for (Object o : state) {
                inSb.append(",?");
            }
            sql += inSb.substring(1);
            sql += ")";
        }
        sql += " order by p.update_date";
        List<PlcExtend> list = jdbcTemplate.query(sql, state, new DefaultPlcExtendRowMapper());
        if (!list.isEmpty()) {
            return list;
        }
        return null;
    }

    @Override
    public boolean batchUpdateState(final List<String[]> updList) {
        if (null == updList || updList.size() == 0) {
            return false;
        }
        String sql = "update plc_info set state = ? where plc_id = ? and date_format(update_date,'%Y-%m-%d %H:%i:%s') = ?";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return updList.size();
                //这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
            }

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                try {
                    String[] arg = updList.get(i);
                    ps.setInt(1, Integer.parseInt(arg[0]));
                    ps.setInt(2, Integer.parseInt(arg[1]));
                    ps.setString(3, arg[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    @Override
    public boolean batchUpdateStateAndSync(final List<String[]> updList) {
        if (null == updList || updList.size() == 0) {
            return false;
        }
        String sql = "update plc_info set state = ?, is_sync = ? where plc_id = ? and date_format(update_date,'%Y-%m-%d %H:%i:%s') = ?";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return updList.size();
                //这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
            }

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                try {
                    String[] arg = updList.get(i);
                    ps.setInt(1, Integer.parseInt(arg[0]));
                    ps.setInt(2, 1);
                    ps.setInt(3, Integer.parseInt(arg[1]));
                    ps.setString(4, arg[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    @Override
    public boolean batchUpdateFileMd5(final List<String[]> updList) {
        if (null == updList || updList.size() == 0) {
            return false;
        }

        StringBuffer inSb = new StringBuffer();
        for (String[] ss : updList) {
            inSb.append(",").append(ss[1]);
        }

        final List<String> fileMd5List = jdbcTemplate.query("select r.file_md5 from driver r, plc_info p where r.driver=p.driver and p.plc_id in(" + inSb.substring(1) + ")", new RowMapper() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("file_md5");
            }
        });

        String sql = "update plc_info set file_md5 = ? where plc_id = ? and date_format(update_date,'%Y-%m-%d %H:%i:%s') = ?";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return updList.size();
                //这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
            }

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                try {
                    String[] arg = updList.get(i);
                    ps.setString(1, fileMd5List.get(i));
                    ps.setInt(2, Integer.parseInt(arg[1]));
                    ps.setString(3, arg[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    @Override
    public boolean batchDeletePlc(final List<Long> ids) {
        if (null == ids || ids.size() == 0) {
            return false;
        }
        StringBuilder idSb = new StringBuilder();
        for (long id : ids) {
            idSb.append(",").append(id);
        }
        String sql = "delete from plc_info where plc_id in(" + idSb.substring(1) + ")";
        jdbcTemplate.update(sql);

        return true;
    }

    @Override
    public List<Long> getDeleteIdsByUpdTime(List<String[]> delArgList) {
        if (null == delArgList || delArgList.size() == 0) {
            return null;
        }

        StringBuilder idSb = new StringBuilder();
        for (String[] args : delArgList) {
            idSb.append(",").append(args[0]);
        }
        List<PlcExtend> plcInfoList = jdbcTemplate.query("select plc_id, update_date from plc_info where plc_id in(" + idSb.substring(1) + ")", new RowMapper() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                PlcExtend model = new PlcExtend();
                model.plc_id = resultSet.getLong("plc_id");
                model.upd_time = TimeUtil.getYYYYMMDDHHMMSSDate(resultSet.getTimestamp("update_date"));
                return model;
            }
        });

        if (null != plcInfoList) {
            List<Long> plcIds = new ArrayList<>();
            for (String[] args : delArgList) {
                for (PlcExtend plcExtend : plcInfoList) {
                    if (Integer.parseInt(args[0]) == plcExtend.plc_id
                            && args[1].equals(plcExtend.upd_time)) {
                        plcIds.add(plcExtend.plc_id);
                        break;
                    }
                }
            }

            return plcIds;
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
            model.is_sync = rs.getInt("is_sync");
            return model;
        }
    }

    public static final class DefaultPlcInfoDtailRowMapper implements RowMapper<PlcInfoDetail> {
        @Override
        public PlcInfoDetail mapRow(ResultSet rs, int i) throws SQLException {
            PlcInfoDetail model = new PlcInfoDetail();
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
            model.file_md5 = rs.getString("file_md5");
            return model;
        }
    }


    @Override
    public List<PlcInfo> getPortState(long device_id, String port) {
        List<Integer> resultList = new ArrayList<Integer>();

        Object args[] = {device_id, port};
        String sql = "SELECT "+ SEL_COL +" FROM plc_info WHERE device_id=? AND port=? ";
        List<PlcInfo> result = jdbcTemplate.query(sql, args, new DefaultPlcInfoRowMapper());

        return result;
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
                               jdbcTemplate.update(" UPDATE alarm_cfg a set   a.bind_state=0 ,a.state=3 WHERE a.plc_id=?", new Object[]{plcId});
                               jdbcTemplate.update("  UPDATE real_his_cfg a set a.bind_state=0 ,a.state=3 WHERE  a.plc_id=?", new Object[]{plcId});
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
            model.com = model.plc_id + "";
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
            model.file_md5 = rs.getString("f_md5");
            return model;
        }
    }

}
