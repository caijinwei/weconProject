package com.wecon.box.impl;

import com.wecon.box.api.DeviceApi;
import com.wecon.box.entity.Device;
import com.wecon.box.entity.Page;
import com.wecon.box.filter.DeviceFilter;
import com.wecon.common.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lanpenghui 2017年8月4日上午10:01:01
 */
@Component
public class DeviceImpl implements DeviceApi {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final String SEL_COL = "device_id,machine_code,`password`,dev_model,name,remark,map,state,dir_id,create_date,update_date";

    @Override
    public long saveDevice(final Device model) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement(
                        "insert into device (machine_code,`password`,dev_model,name,remark,map,state,dir_id,create_date,update_date)values(?,?,?,?,?,?,?,?,current_timestamp(),?);",
                        Statement.RETURN_GENERATED_KEYS);
                preState.setString(1, model.machine_code);
                preState.setString(2, model.password);
                preState.setString(3, model.dev_model);
                preState.setString(4, model.name);
                preState.setString(5, model.remark);
                preState.setString(6, model.map);
                preState.setInt(7, model.state);
                preState.setLong(8, model.dir_id);
                preState.setTimestamp(9, model.update_date);
                return preState;
            }
        }, key);
        // 从主键持有者中获得主键值
        return key.getKey().longValue();

    }

    @Override
    public boolean updateDevice(final Device model) {
        String sql = "update device set machine_code=?,password=?,dev_model=?,name=?,remark=?,map=?,state=?,dir_id=?,update_date=current_timestamp() where device_id=?";

        jdbcTemplate.update(sql, new Object[]{model.machine_code, model.password, model.dev_model, model.name,
                model.remark, model.map, model.state, model.dir_id, model.device_id});
        return true;
    }

    @Override
    public Device getDevice(final long device_id) {
        String sql = "select " + SEL_COL + " from device where device_id=?";
        List<Device> list = jdbcTemplate.query(sql, new Object[]{device_id}, new DefaultDeviceRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Device getDevice(final String machine_code) {
        String sql = "select " + SEL_COL + " from device where machine_code=?";
        List<Device> list = jdbcTemplate.query(sql, new Object[]{machine_code}, new DefaultDeviceRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Device> getDeviceList(long account_id, long account_dir_id) {
        String sql = "select d.device_id,d.machine_code,d.`password`,d.dev_model,d.name,d.remark,d.map,d.state,d.dir_id,d.create_date,d.update_date from  account_dir ad,account_dir_rel adr,device d ,dev_bind_user dbu WHERE 1=1 and ad.`id`=adr.`acc_dir_id`AND ad.`type`=0 AND adr.`ref_id`=d.device_id AND dbu.account_id=ad.`account_id`AND dbu.device_id=d.device_id";
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        if (account_id > 0) {
            condition.append(" and ad.account_id = ? ");
            params.add(account_id);

        }
        if (account_dir_id > 0) {
            condition.append(" and ad.id = ? ");
            params.add(account_dir_id);

        }
        sql += condition;
        List<Device> list = jdbcTemplate.query(sql, params.toArray(), new DefaultDeviceRowMapper());
        if (!list.isEmpty()) {
            return list;
        }
        return null;
    }

    @Override
    public void delDevice(long device_id) {
        String sql = "delete from  device  where device_id=?";
        jdbcTemplate.update(sql, new Object[]{device_id});
    }

    @Override
    public Page<Device> getDeviceList(DeviceFilter filter, int pageIndex, int pageSize) {
        String sqlCount = "select count(0) from device where 1=1";
        String sql = "select " + SEL_COL + " from device where 1=1";
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        if (filter.device_id > 0) {
            condition.append(" and device_id = ? ");
            params.add(filter.device_id);
        }
        if (!CommonUtils.isNullOrEmpty(filter.machine_code)) {
            condition.append(" and machine_code like ? ");
            params.add("%" + filter.machine_code + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.dev_model)) {
            condition.append(" and dev_model like ? ");
            params.add("%" + filter.dev_model + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.name)) {
            condition.append(" and name like ? ");
            params.add("%" + filter.name + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.remark)) {
            condition.append(" and remark like ? ");
            params.add("%" + filter.remark + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.map)) {
            condition.append(" and map like ? ");
            params.add("%" + filter.map + "%");
        }
        if (filter.state > -1) {
            condition.append(" and state = ? ");
            params.add(filter.state);
        }
        if (filter.dir_id > 0) {
            condition.append(" and dir_id = ? ");
            params.add(filter.dir_id);
        }

        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
        Page<Device> page = new Page<Device>(pageIndex, pageSize, totalRecord);
        String sort = " order by device_id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<Device> list = jdbcTemplate.query(sql, params.toArray(), new DefaultDeviceRowMapper());
        page.setList(list);
        return page;
    }

    public static final class DefaultDeviceRowMapper implements RowMapper<Device> {
        @Override
        public Device mapRow(ResultSet rs, int i) throws SQLException {
            Device model = new Device();
            model.device_id = rs.getLong("device_id");
            model.machine_code = rs.getString("machine_code");
            model.password = rs.getString("password");
            model.dev_model = rs.getString("dev_model");
            model.name = rs.getString("name");
            model.remark = rs.getString("remark");
            model.map = rs.getString("map");
            model.state = rs.getInt("state");
            model.dir_id = rs.getLong("dir_id");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");
            return model;
        }
    }

    /*
* 查询管理账户的盒子
* @Params acc_id
*         管理账户id
* sql:SELECT a.device_id, b.`name` FROM dev_bind_user a INNER JOIN device b ON a.device_id=b.device_id WHERE a.account_id='1000017';
* */
    public List<Device> getDeviceNameByAccId(long acc_id) {
        String sql = "SELECT a.device_id, b.`name` FROM dev_bind_user a INNER JOIN device b ON a.device_id=b.device_id WHERE a.account_id=?";
        Object[] args = new Object[]{acc_id};

        List<Device> list = jdbcTemplate.query(sql, args, new RowMapper() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                Device model = new Device();
                model.device_id = resultSet.getLong("device_id");
                model.name = resultSet.getString("name");
                return model;
            }
        });
        return list;
    }

}
