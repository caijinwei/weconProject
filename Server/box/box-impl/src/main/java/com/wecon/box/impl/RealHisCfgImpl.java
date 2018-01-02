package com.wecon.box.impl;

import com.wecon.box.api.AccountDirApi;
import com.wecon.box.api.AccountDirRelApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.constant.Constant;
import com.wecon.box.entity.*;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.RealHisConfigFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.common.util.CommonUtils;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author lanpenghui 2017年8月2日
 */
@Component
public class RealHisCfgImpl implements RealHisCfgApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccountDirApi accountDirApi;

    @Autowired
    private AccountDirRelApi accountDirRelApi;

    @Autowired
    public PlatformTransactionManager transactionManager;

    private final String SEL_COL = "r.id,r.data_id,r.account_id,r.plc_id,r.name,r.addr,r.addr_type,r.describe,r.digit_count,r.data_limit,r.his_cycle,r.data_type,r.state,r.create_date,r.update_date,r.bind_state,r.device_id,r.rid,r.digit_binary,r.ext_unit";

    @Override
    public long saveRealHisCfg(final RealHisCfg model) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement(
                        "insert into real_his_cfg(data_id,account_id,plc_id,`name`,addr,addr_type,`describe`,digit_count,data_limit,his_cycle,data_type,state,create_date,update_date,bind_state,device_id,rid,digit_binary,ext_unit)values(?,?,?,?,?,?,?,?,?,?,?,?,current_timestamp(),current_timestamp(),1,?,?,?,?);",
                        Statement.RETURN_GENERATED_KEYS);
                preState.setLong(1, model.data_id);
                preState.setLong(2, model.account_id);
                preState.setLong(3, model.plc_id);
                preState.setString(4, model.name);
                preState.setString(5, model.addr);
                preState.setInt(6, model.addr_type);
                preState.setString(7, model.describe);
                preState.setString(8, model.digit_count);
                preState.setString(9, model.data_limit);
                preState.setInt(10, model.his_cycle);
                preState.setInt(11, model.data_type);
                preState.setInt(12, model.state);
                preState.setLong(13, model.device_id);
                preState.setString(14, model.rid);
                preState.setString(15, model.digit_binary);
                preState.setString(16, model.ext_unit);
                return preState;
            }
        }, key);
        // 从主键持有者中获得主键值
        return key.getKey().longValue();
    }

    @Override
    public boolean updateRealHisCfg(RealHisCfg model) {
        String sql = "update real_his_cfg SET data_id=?,account_id=?,plc_id=?,`name`=?,addr=?,addr_type=?,`describe`=?,digit_count=?,data_limit=?,his_cycle=?,data_type=?,state=?,update_date=current_timestamp(),bind_state=?,device_id=?,rid=?,digit_binary=?,ext_unit=? where id=?";
        jdbcTemplate.update(sql,
                new Object[]{model.data_id, model.account_id, model.plc_id, model.name, model.addr, model.addr_type,
                        model.describe, model.digit_count, model.data_limit, model.his_cycle, model.data_type,
                        model.state, model.bind_state, model.device_id, model.rid, model.digit_binary, model.ext_unit,
                        model.id});

        return true;
    }

    @Override
    public RealHisCfg getRealHisCfg(long id) {

        String sql = "select " + SEL_COL + " from real_his_cfg r where r.id=?";
        List<RealHisCfg> list = jdbcTemplate.query(sql, new Object[]{id}, new DefaultRealHisCfgRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<RealHisCfg> getRealHisCfg(long plc_id, int state) {
        List<Object> params = new ArrayList<Object>();
        String sql = "select " + SEL_COL + " from real_his_cfg r where r.plc_id=?";
        params.add(plc_id);

        StringBuffer condition = new StringBuffer("");

        if (state > -1) {
            condition.append("  and r.state=? ");
            params.add(state);
        }

        sql = sql + condition;

        List<RealHisCfg> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealHisCfgRowMapper());
        if (!list.isEmpty()) {
            return list;
        }

        return null;
    }

    @Override
    public RealHisCfg getRealHisCfg(long device_id, String name) {
        List<Object> params = new ArrayList<Object>();
        String sql = "select " + SEL_COL + " from real_his_cfg r where r.device_id=?";
        params.add(device_id);

        StringBuffer condition = new StringBuffer("");

        if (!CommonUtils.isNullOrEmpty(name)) {
            condition.append("  and r.name=? ");
            params.add(name);
        }

        sql = sql + condition;

        List<RealHisCfg> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealHisCfgRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    @Override
    public RealHisCfg getRealHisCfg(long device_id, String name, int data_type) {
        List<Object> params = new ArrayList<Object>();
        String sql = "select " + SEL_COL + " from real_his_cfg r where r.device_id=?";
        params.add(device_id);

        StringBuffer condition = new StringBuffer("");

        if (!CommonUtils.isNullOrEmpty(name)) {
            condition.append("  and r.name=? ");
            params.add(name);
        }
        if (!CommonUtils.isNullOrEmpty(data_type)) {
            condition.append("  and r.data_type=? ");
            params.add(data_type);
        }

        sql = sql + condition;

        List<RealHisCfg> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealHisCfgRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    @Override
    public void delRealHisCfg(long id) {

        String sql = "delete from  real_his_cfg r where r.id=?";
        jdbcTemplate.update(sql, new Object[]{id});
    }

    @Override
    public List<RealHisCfgDevice> getRealHisCfg(RealHisCfgFilter filter) {
        String sql = "select " + SEL_COL + ",d.machine_code"
                + " from  device d,real_his_cfg r where 1=1 and d.`device_id`=r.device_id and r.bind_state=1 ";
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        if (filter.id > 0) {
            condition.append(" and r.id = ? ");
            params.add(filter.id);
        }
        if (filter.device_id > 0) {
            condition.append(" and r.device_id = ? ");
            params.add(filter.device_id);
        }
        if (filter.data_id > 0) {
            condition.append(" and r.data_id = ? ");
            params.add(filter.data_id);
        }
        if (filter.account_id > 0) {
            condition.append(" and r.account_id = ? ");
            params.add(filter.account_id);
        }
        if (filter.plc_id > 0) {
            condition.append(" and r.plc_id = ? ");
            params.add(filter.plc_id);
        }
        if (!CommonUtils.isNullOrEmpty(filter.name)) {
            condition.append(" and r.name like ? ");
            params.add("%" + filter.name + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.addr)) {
            condition.append(" and r.addr like ? ");
            params.add("%" + filter.addr + "%");
        }
        if (filter.addr_type > -1) {
            condition.append(" and r.addr_type = ? ");
            params.add(filter.addr_type);
        }

        if (!CommonUtils.isNullOrEmpty(filter.describe)) {
            condition.append(" and r.describe like ? ");
            params.add("%" + filter.describe + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.digit_count)) {
            condition.append(" and r.digit_count like ? ");
            params.add("%" + filter.digit_count + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.data_limit)) {
            condition.append(" and r.data_limit like ? ");
            params.add("%" + filter.data_limit + "%");
        }

        if (filter.his_cycle > -1) {
            condition.append(" and r.his_cycle = ? ");
            params.add(filter.his_cycle);
        }
        if (filter.data_type > -1) {
            condition.append(" and r.data_type = ? ");
            params.add(filter.data_type);
        }
        if (filter.state > -1) {
            condition.append(" and r.state != ? ");
            params.add(filter.state);

        }
        if (!CommonUtils.isNullOrEmpty(filter.ext_unit)) {
            condition.append(" and r.ext_unit = ? ");
            params.add(filter.ext_unit);

        }

        sql += condition;
        List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultHisCfgDeviceRowMapper());
        return list;
    }

    /**
     * 管理账号给自己分配实时监控点
     */
    @Override
    public Page<RealHisCfgDevice> getRealHisCfg(RealHisCfgFilter filter, int pageIndex, int pageSize) {
        String sqlCount = "select count(0) from  device d,real_his_cfg r where 1=1 and d.`device_id`=r.device_id ";
        String sql = "select " + SEL_COL + ",d.machine_code"
                + " from  device d,real_his_cfg r where 1=1 and d.`device_id`=r.device_id";
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        if (filter.id > 0) {
            condition.append(" and r.id = ? ");
            params.add(filter.id);
        }
        if (filter.device_id > 0) {
            condition.append(" and r.device_id = ? ");
            params.add(filter.device_id);
        }
        if (filter.data_id > 0) {
            condition.append(" and r.data_id = ? ");
            params.add(filter.data_id);
        }
        if (filter.account_id > 0) {
            condition.append(" and r.account_id = ? ");
            params.add(filter.account_id);
        }
        if (filter.plc_id > 0) {
            condition.append(" and r.plc_id = ? ");
            params.add(filter.plc_id);
        }
        if (!CommonUtils.isNullOrEmpty(filter.name)) {
            condition.append(" and r.name like ? ");
            params.add("%" + filter.name + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.addr)) {
            condition.append(" and r.addr like ? ");
            params.add("%" + filter.addr + "%");
        }
        if (filter.addr_type > -1) {
            condition.append(" and r.addr_type = ? ");
            params.add(filter.addr_type);
        }

        if (!CommonUtils.isNullOrEmpty(filter.describe)) {
            condition.append(" and r.describe like ? ");
            params.add("%" + filter.describe + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.digit_count)) {
            condition.append(" and r.digit_count like ? ");
            params.add("%" + filter.digit_count + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.data_limit)) {
            condition.append(" and r.data_limit like ? ");
            params.add("%" + filter.data_limit + "%");
        }

        if (filter.his_cycle > -1) {
            condition.append(" and r.his_cycle = ? ");
            params.add(filter.his_cycle);
        }
        if (filter.bind_state > -1) {
            condition.append(" and r.bind_state = ? ");
            params.add(filter.bind_state);
        }
        if (filter.data_type > -1) {
            condition.append(" and r.data_type = ? ");
            params.add(filter.data_type);
        }
        if (filter.state > -1) {
            condition.append(" and r.state != ? ");
            params.add(filter.state);

        }
        if (!CommonUtils.isNullOrEmpty(filter.rid)) {
            condition.append(" and r.rid like ? ");
            params.add("%" + filter.rid + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.ext_unit)) {
            condition.append(" and r.ext_unit = ? ");
            params.add(filter.ext_unit);

        }
        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
        Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
        String sort = " order by id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultHisCfgDeviceRowMapper());
        page.setList(list);
        return page;
    }

    @Override
    public List<RealHisCfgDevice> getRealHisCfg(ViewAccountRoleFilter filter) {
        String sql = "select " + SEL_COL + ",d.machine_code"
                + " from   device d,real_his_cfg r ,view_account_role v ,account_relation ar where 1=1 and  d.`device_id`=r.device_id and  v.view_id=ar.view_id and ar.manager_id=r.`account_id` and  r.bind_state=1 and r.data_type=1 and  r.`id`=v.cfg_id";
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();

        if (filter.view_id > 0) {
            condition.append(" and v.view_id = ? ");
            params.add(filter.view_id);
        }
        if (filter.role_type > -1) {
            condition.append(" and v.role_type = ? ");
            params.add(filter.role_type);
        }

        sql += condition;
        List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultHisCfgDeviceRowMapper());
        return list;
    }

    /**
     * 视图账号给自己分配实时监控点
     */
    @Override
    public Page<RealHisCfgDevice> getRealHisCfg(ViewAccountRoleFilter filter, int pageIndex, int pageSize) {
        String sqlCount = "select count(0) from   device d,real_his_cfg r ,view_account_role v ,account_relation ar where 1=1 and  d.`device_id`=r.device_id and  v.view_id=ar.view_id and ar.manager_id=r.`account_id` and  r.bind_state=1 and r.data_type=0 and  r.`id`=v.cfg_id";
        String sql = "select " + SEL_COL + ",d.machine_code"
                + " from   device d,real_his_cfg r ,view_account_role v ,account_relation ar where 1=1 and  d.`device_id`=r.device_id and  v.view_id=ar.view_id and ar.manager_id=r.`account_id` and  r.bind_state=1 and r.data_type=0 and  r.`id`=v.cfg_id";
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();

        if (filter.view_id > 0) {
            condition.append(" and v.view_id = ? ");
            params.add(filter.view_id);
        }
        if (filter.role_type > -1) {
            condition.append(" and v.role_type = ? ");
            params.add(filter.role_type);
        }
        if (filter.state > -1) {
            condition.append(" and r.state != ? ");
            params.add(filter.state);

        }

        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
        Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
        String sort = " order by id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultHisCfgDeviceRowMapper());
        page.setList(list);
        return page;

    }

    @Override
    public Page<RealHisCfgDevice> getRealHisCfgList(RealHisCfgFilter filter, int pageIndex, int pageSize) {
        String sqlCount = "select count(0) from account_dir ad,account_dir_rel adr ,real_his_cfg  r,plc_info pli,device d where 1=1 and  ad.`id`=adr.`acc_dir_id` and pli.`plc_id`=r.`plc_id` and pli.`device_id`=d.`device_id` and r.`id`=adr.`ref_id` and ad.`account_id`=r.`account_id`";
        String sql = "select " + SEL_COL + ",d.machine_code,d.state as dstate,adr.ref_alais"
                + " from account_dir ad,account_dir_rel adr ,real_his_cfg  r,plc_info pli,device d where 1=1 and  ad.`id`=adr.`acc_dir_id` and pli.`plc_id`=r.`plc_id` and pli.`device_id`=d.`device_id` and r.`id`=adr.`ref_id` and ad.`account_id`=r.`account_id`";
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        if (filter.id > 0) {
            condition.append(" and r.id = ? ");
            params.add(filter.id);
        }
        if (filter.device_id > 0) {
            condition.append(" and d.device_id = ? ");
            params.add(filter.device_id);
        }
        if (filter.dirId > 0) {
            condition.append(" and ad.id = ? ");
            params.add(filter.dirId);
        }
        if (filter.data_id > 0) {
            condition.append(" and r.data_id = ? ");
            params.add(filter.data_id);
        }
        if (filter.account_id > 0) {
            condition.append(" and r.account_id = ? ");
            params.add(filter.account_id);
        }
        if (filter.plc_id > 0) {
            condition.append(" and r.plc_id = ? ");
            params.add(filter.plc_id);
        }
        if (!CommonUtils.isNullOrEmpty(filter.name)) {
            condition.append(" and r.name like ? ");
            params.add("%" + filter.name + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.addr)) {
            condition.append(" and r.addr like ? ");
            params.add("%" + filter.addr + "%");
        }
        if (filter.addr_type > -1) {
            condition.append(" and r.addr_type = ? ");
            params.add(filter.addr_type);
        }

        if (!CommonUtils.isNullOrEmpty(filter.describe)) {
            condition.append(" and r.describe like ? ");
            params.add("%" + filter.describe + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.digit_count)) {
            condition.append(" and r.digit_count like ? ");
            params.add("%" + filter.digit_count + "%");
        }
        if (!CommonUtils.isNullOrEmpty(filter.data_limit)) {
            condition.append(" and r.data_limit like ? ");
            params.add("%" + filter.data_limit + "%");
        }

        if (filter.his_cycle > -1) {
            condition.append(" and r.his_cycle = ? ");
            params.add(filter.his_cycle);
        }
        if (filter.data_type > -1) {
            condition.append(" and r.data_type = ? ");
            params.add(filter.data_type);
        }
        if (filter.state > -1) {
            condition.append(" and r.state != ? ");
            params.add(filter.state);

        }
        if (filter.bind_state > -1) {
            condition.append(" and r.bind_state = ? ");
            params.add(filter.bind_state);

        }
        if (!CommonUtils.isNullOrEmpty(filter.ext_unit)) {
            condition.append(" and r.ext_unit = ? ");
            params.add(filter.ext_unit);

        }
        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
        Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
        String sort = " order by id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealCfgDeviceRowMapper());
        page.setList(list);
        return page;

    }

    public Page<RealHisCfgDevice> getRealHisCfgList(ViewAccountRoleFilter filter, int pageIndex, int pageSize) {
        String sqlCount = "select count(0) from  real_his_cfg r , device d,plc_info p, view_account_role v,account_dir ad,account_dir_rel adr where 1=1 and  p.plc_id=r.plc_id and r.id=adr.`ref_id` and p.device_id=d.device_id and v.cfg_id=r.id and v.cfg_type=1 and ad.`id`=adr.`acc_dir_id` and ad.`account_id`=v.view_id and r.bind_state=1";
        String sql = "select " + SEL_COL + ",d.machine_code,adr.ref_alais,v.role_type"
                + " from real_his_cfg r , device d,plc_info p, view_account_role v,account_dir ad,account_dir_rel adr where 1=1 and  p.plc_id=r.plc_id and r.id=adr.`ref_id` and p.device_id=d.device_id and v.cfg_id=r.id and v.cfg_type=1 and ad.`id`=adr.`acc_dir_id` and ad.`account_id`=v.view_id and r.bind_state=1";
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        if (filter.view_id > 0) {
            condition.append(" and v.view_id = ? ");
            params.add(filter.view_id);
        }
        if (filter.dirId > 0) {
            condition.append(" and ad.`id` = ? ");
            params.add(filter.dirId);
        }
        if (filter.role_type > -1) {
            condition.append(" and v.role_type = ? ");
            params.add(filter.role_type);
        }
        if (filter.data_type > -1) {
            condition.append(" and r.data_type = ? ");
            params.add(filter.data_type);
        }
        if (filter.state > -1) {
            condition.append(" and r.state != ? ");
            params.add(filter.state);

        }

        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
        Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
        String sort = " order by id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultViewRowMapper());
        page.setList(list);
        return page;
    }

    @Override
    public Page<RealHisCfgDevice> getRealHisCfgDevicePage(RealHisCfgFilter filter, Map<String, Object> bParams,
                                                          int pageIndex, int pageSize) {
        String fromStr = "from real_his_cfg r ,device d, plc_info p, account_dir_rel adr, account_dir ad ";
        StringBuffer condition = new StringBuffer();
        List<Object> params = new ArrayList<Object>();

        if (filter.account_id > 0) {
            condition.append(" and r.account_id = ? ");
            params.add(filter.account_id);
        }

        if (filter.data_type > -1) {
            condition.append(" and r.data_type = ? ");
            params.add(filter.data_type);
        }

        Object boxId = bParams.get("boxId");
        Object groupId = bParams.get("groupId");
        if (null != boxId && !"0".equals(boxId.toString())) {
            condition.append(" and ad.device_id = ? ");
            params.add(boxId);
        }
        if (null != groupId && !"0".equals(groupId.toString())) {
            condition.append(" and adr.acc_dir_id = ?");
            params.add(groupId);
        }
        String sqlCount = "select count(distinct r.id, ad.id) " + fromStr
                + " where adr.acc_dir_id=ad.id and  p.`plc_id`=r.plc_id and p.`device_id`=d.device_id and r.id=adr.ref_id and r.bind_state=1 and r.state != "
                + Constant.State.STATE_DELETE_CONFIG;
        String sql = "select distinct " + SEL_COL + ",d.machine_code, d.state as dstate, adr.ref_alais,adr.acc_dir_id"
                + "  " + fromStr
                + " where adr.acc_dir_id=ad.id and  p.`plc_id`=r.plc_id and p.`device_id`=d.device_id and r.id=adr.ref_id and r.bind_state=1 and r.state != "
                + Constant.State.STATE_DELETE_CONFIG;
        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
        Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
        String sort = " order by id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealCfgDeviceRowMapper());
        page.setList(list);
        return page;

    }

    @Override
    public Page<RealHisCfgDevice> getRealHisCfgDevicePage(ViewAccountRoleFilter filter, Map<String, Object> bParams,
                                                          int pageIndex, int pageSize) {
        String fromStr = "from real_his_cfg r ,device d, plc_info p, view_account_role v, account_dir_rel adr, account_dir ad ";
        StringBuffer condition = new StringBuffer();
        List<Object> params = new ArrayList<Object>();

        if (filter.view_id > 0) {
            condition.append(" and v.view_id = ? ");
            params.add(filter.view_id);
        }

        if (filter.data_type > -1) {
            condition.append(" and r.data_type = ? ");
            params.add(filter.data_type);
        }

        Object groupId = bParams.get("groupId");
        Object boxId = bParams.get("boxId");
        if (null != groupId && !"0".equals(groupId.toString())) {
            condition.append(" and adr.acc_dir_id = ?");
            params.add(groupId);
        }
        if (null != boxId && !"0".equals(boxId.toString())) {
            condition.append(" and ad.device_id = ? ");
            params.add(boxId);
        }
        String sqlCount = "select count(distinct r.id, ad.id) " + fromStr
                + " where adr.acc_dir_id=ad.id and  p.`plc_id`=r.plc_id and p.`device_id`=d.device_id and v.cfg_id=r.id and v.cfg_type=1 and r.id=adr.ref_id and r.bind_state=1";
        String sql = "select distinct " + SEL_COL + ",d.machine_code, d.state as dstate, adr.ref_alais,adr.acc_dir_id"
                + "  " + fromStr
                + " where adr.acc_dir_id=ad.id and  p.`plc_id`=r.plc_id and p.`device_id`=d.device_id and v.cfg_id=r.id and v.cfg_type=1 and r.id=adr.ref_id and r.bind_state=1";
        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
        Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
        String sort = " order by id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealCfgDeviceRowMapper());
        page.setList(list);
        return page;

    }

    @Override
    public Map getRealCfgDetail(long id) {
        String sql = "select " + SEL_COL + ",d.machine_code, p.port from real_his_cfg r ,device d, plc_info p"
                + " where  r.device_id=d.device_id and r.id = ? and p.device_id=r.device_id";
        List<Map> list = jdbcTemplate.query(sql, new Object[]{id}, new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int i) throws SQLException {
                Map model = new HashMap();
                model.put("id", rs.getLong("id"));
                model.put("plc_id", rs.getLong("plc_id"));
                model.put("machine_code", rs.getString("machine_code"));
                model.put("name", rs.getString("name"));
                model.put("port", rs.getString("port"));
                model.put("addr_type", rs.getInt("addr_type"));
                model.put("rid", rs.getString("rid"));
                model.put("addr", rs.getString("addr"));
                model.put("data_id", rs.getLong("data_id"));
                model.put("digit_count", rs.getString("digit_count"));
                model.put("describe", rs.getString("describe"));
                model.put("ext_unit", rs.getString("ext_unit"));
                return model;
            }
        });
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<RealHisCfgExtend> getRealHisCfgListByState(Object... state) {
        String sql = "select " + SEL_COL
                + ",d.machine_code from real_his_cfg r ,device d where d.device_id=r.device_id and d.state=1 ";
        if (null != state && state.length > 0) {
            sql += " and r.state in (";
            StringBuffer inSb = new StringBuffer();
            for (Object o : state) {
                inSb.append(",?");
            }
            sql += inSb.substring(1);
            sql += ")";
        }
        List<RealHisCfgExtend> list = jdbcTemplate.query(sql, state, new DefaultRealCfgExtendRowMapper());
        return list;
    }

    @Override
    public boolean batchUpdateState(final List<String[]> updList) {
        if (null == updList || updList.size() == 0) {
            return false;
        }
        String sql = "update real_his_cfg set state = ? where id = ? and date_format(update_date,'%Y-%m-%d %H:%i:%s') = ?";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return updList.size();
                // 这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
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
    public boolean batchDeleteByPlcId(final List<Long> ids) {
        if (null == ids || ids.size() == 0) {
            return false;
        }

        StringBuilder idSb = new StringBuilder();
        for (long id : ids) {
            idSb.append(",").append(id);
        }
        String sql = "delete from real_his_cfg where plc_id in(" + idSb.substring(1) + ")";
        jdbcTemplate.update(sql);

        return true;
    }

    @Override
    public boolean batchDeleteById(final List<Long> ids) {
        if (null == ids || ids.size() == 0) {
            return false;
        }

        StringBuilder idSb = new StringBuilder();
        for (long id : ids) {
            idSb.append(",").append(id);
        }
        String sql = "delete from real_his_cfg where id in(" + idSb.substring(1) + ")";
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
        List<RealHisCfgExtend> list = jdbcTemplate.query(
                "select id, update_date from real_his_cfg where id in(" + idSb.substring(1) + ")", new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                        RealHisCfgExtend model = new RealHisCfgExtend();
                        model.id = resultSet.getLong("id");
                        model.upd_time = TimeUtil.getYYYYMMDDHHMMSSDate(resultSet.getTimestamp("update_date"));
                        return model;
                    }
                });

        if (null != list) {
            List<Long> ids = new ArrayList<>();
            for (String[] args : delArgList) {
                for (RealHisCfgExtend extend : list) {
                    if (Integer.parseInt(args[0]) == extend.id && args[1].equals(extend.upd_time)) {
                        ids.add(extend.id);
                        break;
                    }
                }
            }

            return ids;
        }

        return null;
    }

	@Override
	public List<RealHisCfg> getRealCfgByIds(final List<Long> ids) {
		if (null == ids || ids.size() == 0) {
			return null;
		}

		StringBuilder idSb = new StringBuilder();
		for (long id : ids) {
			idSb.append(",").append(id);
		}
		String sql = "select distinct " + SEL_COL + " from real_his_cfg where id in(" + idSb.substring(1) + ") order by field(id,"+idSb.substring(1)+");";
		List<RealHisCfg> realCfgList = jdbcTemplate.query(sql, new DefaultRealHisCfgRowMapper());

		return realCfgList;
	}

    @Override
    public List<Long> getRealHisCfgIdsByPlcIds(List<Long> plcIds) {
        if (null == plcIds || plcIds.size() == 0) {
            return null;
        }

        StringBuilder idSb = new StringBuilder();
        for (long plcId : plcIds) {
            idSb.append(",").append(plcId);
        }

        List<Long> cfgIds = jdbcTemplate.query("select id from real_his_cfg where plc_id in(" + idSb.substring(1) + ")",
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                        return resultSet.getLong("plc_id");
                    }
                });

        return cfgIds;
    }

    public static final class DefaultRealHisCfgRowMapper implements RowMapper<RealHisCfg> {

        @Override
        public RealHisCfg mapRow(ResultSet rs, int i) throws SQLException {
            RealHisCfg model = new RealHisCfg();
            model.id = rs.getLong("id");
            model.data_id = rs.getLong("data_id");
            model.account_id = rs.getLong("account_id");
            model.plc_id = rs.getLong("plc_id");
            model.name = rs.getString("name");
            model.addr = rs.getString("addr");
            model.addr_type = rs.getInt("addr_type");
            model.describe = rs.getString("describe");
            model.digit_count = rs.getString("digit_count");
            model.digit_binary = rs.getString("digit_binary");
            model.data_limit = rs.getString("data_limit");
            model.his_cycle = rs.getInt("his_cycle");
            model.data_type = rs.getInt("data_type");
            model.bind_state = rs.getInt("bind_state");
            model.state = rs.getInt("state");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");
            model.rid = rs.getString("rid");
            model.device_id = rs.getLong("device_id");
            model.bind_state = rs.getInt("bind_state");
            model.ext_unit = rs.getString("ext_unit");

            return model;
        }
    }

    public static final class DefaultRealCfgDeviceRowMapper implements RowMapper<RealHisCfgDevice> {

        @Override
        public RealHisCfgDevice mapRow(ResultSet rs, int i) throws SQLException {
            RealHisCfgDevice model = new RealHisCfgDevice();
            model.id = rs.getLong("id");
            model.data_id = rs.getLong("data_id");
            model.account_id = rs.getLong("account_id");
            model.plc_id = rs.getLong("plc_id");
            model.name = rs.getString("name");
            model.addr = rs.getString("addr");
            model.addr_type = rs.getInt("addr_type");
            model.describe = rs.getString("describe");
            model.digit_count = rs.getString("digit_count");
            model.data_limit = rs.getString("data_limit");
            model.digit_binary = rs.getString("digit_binary");
            model.his_cycle = rs.getInt("his_cycle");
            model.data_type = rs.getInt("data_type");
            model.state = rs.getInt("state");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");
            model.machine_code = rs.getString("machine_code");
            model.ref_alais = rs.getString("ref_alais");
            model.rid = rs.getString("rid");
            model.device_id = rs.getLong("device_id");
            model.bind_state = rs.getInt("bind_state");
            model.dstate = rs.getInt("dstate");
            model.ext_unit = rs.getString("ext_unit");
            try {
                model.dir_id = rs.getLong("acc_dir_id");
            } catch (Exception e) {
            }
            return model;
        }
    }

    public static final class DefaultRealCfgExtendRowMapper implements RowMapper<RealHisCfgExtend> {

        @Override
        public RealHisCfgExtend mapRow(ResultSet rs, int i) throws SQLException {
            RealHisCfgExtend model = new RealHisCfgExtend();
            model.id = rs.getLong("id");
            model.addr_id = model.id;
            model.data_id = rs.getLong("data_id");
            model.account_id = rs.getLong("account_id");
            model.plc_id = rs.getLong("plc_id");
            model.com = model.plc_id + "";
            model.name = rs.getString("name");
            model.addr = rs.getString("addr");
            model.addr_type = rs.getInt("addr_type");
            model.describe = rs.getString("describe");
            model.digit_count = rs.getString("digit_count");
            model.digit_binary = rs.getString("digit_binary");
            model.data_limit = rs.getString("data_limit");
            model.his_cycle = rs.getInt("his_cycle");
            model.data_type = rs.getInt("data_type");
            model.state = rs.getInt("state");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");
            model.upd_time = TimeUtil.getYYYYMMDDHHMMSSDate(model.update_date);
            model.machine_code = rs.getString("machine_code");
            model.rid = rs.getString("rid");
            model.ext_unit = rs.getString("ext_unit");
            // model.ref_alais = rs.getString("ref_alais");

            return model;
        }
    }

    public static final class DefaultViewRowMapper implements RowMapper<RealHisCfgDevice> {

        @Override
        public RealHisCfgDevice mapRow(ResultSet rs, int i) throws SQLException {
            RealHisCfgDevice model = new RealHisCfgDevice();
            model.id = rs.getLong("id");
            model.data_id = rs.getLong("data_id");
            model.account_id = rs.getLong("account_id");
            model.plc_id = rs.getLong("plc_id");
            model.name = rs.getString("name");
            model.addr = rs.getString("addr");
            model.addr_type = rs.getInt("addr_type");
            model.describe = rs.getString("describe");
            model.digit_count = rs.getString("digit_count");
            model.digit_binary = rs.getString("digit_binary");
            model.data_limit = rs.getString("data_limit");
            model.his_cycle = rs.getInt("his_cycle");
            model.data_type = rs.getInt("data_type");
            model.state = rs.getInt("state");
            model.rid = rs.getString("rid");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");
            model.machine_code = rs.getString("machine_code");
            model.ref_alais = rs.getString("ref_alais");
            model.role_type = rs.getInt("role_type");
            model.device_id = rs.getLong("device_id");
            model.bind_state = rs.getInt("bind_state");
            model.ext_unit = rs.getString("ext_unit");
            return model;
        }
    }

    public static final class DefaultHisCfgDeviceRowMapper implements RowMapper<RealHisCfgDevice> {

        @Override
        public RealHisCfgDevice mapRow(ResultSet rs, int i) throws SQLException {
            RealHisCfgDevice model = new RealHisCfgDevice();
            model.id = rs.getLong("id");
            model.data_id = rs.getLong("data_id");
            model.account_id = rs.getLong("account_id");
            model.device_id = rs.getLong("device_id");
            model.plc_id = rs.getLong("plc_id");
            model.name = rs.getString("name");
            model.addr = rs.getString("addr");
            model.addr_type = rs.getInt("addr_type");
            model.describe = rs.getString("describe");
            model.digit_count = rs.getString("digit_count");
            model.data_limit = rs.getString("data_limit");
            model.digit_binary = rs.getString("digit_binary");
            model.his_cycle = rs.getInt("his_cycle");
            model.data_type = rs.getInt("data_type");
            model.state = rs.getInt("state");
            model.rid = rs.getString("rid");
            model.bind_state = rs.getInt("bind_state");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");
            model.machine_code = rs.getString("machine_code");
            model.ext_unit = rs.getString("ext_unit");
            return model;
        }
    }

    /*
     * 查询盒子下的监控点
     */
    public ArrayList<Integer> findRealHisCfgIdSBydevice_id(Integer device_id) {
        Object[] args = new Object[]{device_id};
        String sql = "SELECT id FROM real_his_cfg WHERE device_id=?";
        ArrayList<Integer> list = null;
        list = (ArrayList<Integer>) jdbcTemplate.query(sql, args, new RowMapper<Integer>() {
            public Integer mapRow(ResultSet resultSet, int i) throws SQLException {
                Integer model = resultSet.getInt("id");
                return model;
            }
        });
        return list;

    }


    /*
     * 设置bind_state=0 解绑
     */
    public void setBind_state(int[] realHisCfg, Integer state) {
        String sql = "UPDATE real_his_cfg SET bind_state=? where id =?";
        for (int i = 0; i < realHisCfg.length; i++) {
            Object[] args = new Object[]{state, realHisCfg[i]};
            jdbcTemplate.update(sql, args);
        }
    }

    /*
     * 盒子用户改变 监控点迁移
     */
    public boolean updatePointAccAndState(long accountId, long deviceId, int state) {

        String sql = "UPDATE real_his_cfg a SET a.account_id=?,a.bind_state=? WHERE a.device_id=?;";
        Object[] args = new Object[]{accountId, state, deviceId};
        Integer count = jdbcTemplate.update(sql, args);
        if (count <= 0) {
            return false;
        }
        return true;

    }

    @Override
    public void updateRealHisState(long plc_id, int state) {
        if (plc_id > 0) {
            String sql = "UPDATE real_his_cfg a SET a.state=? WHERE a.plc_id=?";
            jdbcTemplate.update(sql, new Object[]{state, plc_id});
        }
    }

    @Override
    public List<RealHisCfg> findRealHisCfgsByPlcId(long plc_id) {
        if (plc_id < 0) {
            return null;
        }
        String sql = "select " + SEL_COL + " from real_his_cfg r where r.plc_id=?";
        List<RealHisCfg> list = jdbcTemplate.query(sql, new Object[]{plc_id}, new DefaultRealHisCfgRowMapper());
        if (!list.isEmpty()) {
            return list;
        }
        return null;
    }

    public List<RealHisCfg> findRealHisCfgs(RealHisConfigFilter filter) {
        StringBuffer condition = new StringBuffer();
        String sql = "select " + SEL_COL + " from real_his_cfg r where 1=1 ";
        ArrayList<Object> params = new ArrayList<Object>();

        if (CommonUtils.isNotNull(filter.id)) {
            params.add(filter.id);
            condition.append(" and id = ? ");
        }
        if (CommonUtils.isNotNull(filter.data_id)) {
            params.add(filter.data_id);
            condition.append(" and data_id = ? ");
        }
        if (CommonUtils.isNotNull(filter.account_id)) {
            params.add(filter.account_id);
            condition.append(" and account_id = ? ");
        }
        if (CommonUtils.isNotNull(filter.plc_id)) {
            params.add(filter.plc_id);
            condition.append(" and plc_id = ? ");
        }
        if (CommonUtils.isNotNull(filter.name)) {
            params.add(filter.name);
            condition.append(" and name = ? ");
        }
        if (CommonUtils.isNotNull(filter.addr)) {
            params.add(filter.addr);
            condition.append(" and addr = ? ");
        }
        if (CommonUtils.isNotNull(filter.addr_type)) {
            params.add(filter.addr_type);
            condition.append(" and addr_type = ? ");
        }
        if (CommonUtils.isNotNull(filter.describe)) {
            params.add(filter.describe);
            condition.append(" and describe = ?");
        }
        if (CommonUtils.isNotNull(filter.digit_count)) {
            params.add(filter.digit_count);
            condition.append(" and digit_count = ?");
        }
        if (CommonUtils.isNotNull(filter.data_limit)) {
            params.add(filter.data_limit);
            condition.append(" and data_limit = ?");
        }
        if (CommonUtils.isNotNull(filter.his_cycle)) {
            params.add(filter.his_cycle);
            condition.append(" and his_cycle = ?");
        }
        if (CommonUtils.isNotNull(filter.state)) {
            params.add(filter.state);
            condition.append(" and state = ?");
        }
        if (CommonUtils.isNotNull(filter.bind_state)) {
            params.add(filter.bind_state);
            condition.append(" and bind_state = ?");
        }
        if (CommonUtils.isNotNull(filter.device_id)) {
            params.add(filter.device_id);
            condition.append(" and device_id = ?");
        }
        if (CommonUtils.isNotNull(filter.data_type)) {
            params.add(filter.data_type);
            condition.append(" and data_type = ?");
        }

        if (CommonUtils.isNotNull(filter.rid)) {
            params.add(filter.rid);
            condition.append(" and rid = ?");
        }
        if (CommonUtils.isNotNull(filter.dirId)) {
            params.add(filter.dirId);
            condition.append(" and dirId = ?");
        }
        if (CommonUtils.isNotNull(filter.ext_unit)) {
            params.add(filter.ext_unit);
            condition.append(" and ext_unit = ?");
        }
        sql += condition;
        List<RealHisCfg> resultList = jdbcTemplate.query(sql, params.toArray(), new DefaultRealHisCfgRowMapper());
        if (resultList.size() <= 0) {
            return null;
        }
        return resultList;
    }

    /*
    *
    *
    * */

    /*
    *
    *    dataType   0 实时配置    1 历史配置
    * */
    @Override
    public Map<Long, Long> copyRealHisCfg(Map<Long, Long> fromtoPlcId, int dataType, long toDeviceId) {
        /*
        * 判断目标device是否已经存在里 实时历史配置
        * */
        RealHisConfigFilter realHisConfigFilter = new RealHisConfigFilter();
        realHisConfigFilter.data_type = dataType;
        realHisConfigFilter.device_id = toDeviceId;
        List<RealHisCfg> toCfgList = findRealHisCfgs(realHisConfigFilter);
        if (toCfgList != null) {
            for (RealHisCfg c : toCfgList) {
                if (c.state != 3) {
                    throw new BusinessException(ErrorCodeOption.RealHisConfig_Is_Exist.key, ErrorCodeOption.RealHisConfig_Is_Exist.value);
                }
            }
        }


        /*
        * 新增  配置
        * */
        Map<Long, Long> resultFromToRealHisCfgMap = new HashMap<Long, Long>();
        realHisConfigFilter = new RealHisConfigFilter();
        for (Map.Entry<Long, Long> entry : fromtoPlcId.entrySet()) {
            realHisConfigFilter.plc_id = entry.getKey();
            realHisConfigFilter.bind_state = 1;
            realHisConfigFilter.data_type = dataType;
            List<RealHisCfg> realHisCfgs = findRealHisCfgs(realHisConfigFilter);

            if(realHisCfgs == null|| realHisCfgs.size()<=0){
                if(dataType ==0) {
                    throw new BusinessException(ErrorCodeOption.RealCfg_Is_Empty.key,ErrorCodeOption.RealCfg_Is_Empty.value);
                }else{
                    throw new BusinessException(ErrorCodeOption.HisCfg_Is_Empty.key,ErrorCodeOption.HisCfg_Is_Empty.value);
                }
            }
            for (RealHisCfg realHisCfg : realHisCfgs) {
                if (realHisCfg.state != 3) {
                    realHisCfg.device_id = toDeviceId;
                    realHisCfg.plc_id = entry.getValue();
                    resultFromToRealHisCfgMap.put(realHisCfg.id, saveRealHisCfg(realHisCfg));
                }
            }
        }
        if (resultFromToRealHisCfgMap.size() > 0) {
            return resultFromToRealHisCfgMap;
        }
        return null;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void copyRealHis(final Long accountId, final Long fromDeviceId, final Long toDeviceId, int dataType, final Map<Long, Long> fromtoPlcMap) {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
         /*
            * 先删除 目标设备的默认分组
            * */
        List<AccountDir> toAccountDir=accountDirApi.getAccountDirList(accountId,1,toDeviceId);
        for(AccountDir accountDir :toAccountDir){
            if(accountDir.name.equals("默认组")){
                accountDirApi.delAccountDir(accountDir.id);
                break;
            }
        }

        try {
            tt.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus ts) {
                    Map<Long, Long> fromtoRealHisCfgMap = copyRealHisCfg(fromtoPlcMap, 0, toDeviceId);
                    Map<Long, Long> fromtoAccountDirMap = accountDirApi.copyAccountDir(accountId, fromDeviceId, toDeviceId, 1);
                    accountDirRelApi.copyAccDeviceRel(fromtoRealHisCfgMap, fromtoAccountDirMap);
                    return true;
                }
            });
        }catch (BusinessException e){
            throw e;
        } catch (Exception e) {
            Logger.getLogger(AccountImpl.class.getName()).log(Level.SEVERE, null, e);
            throw new BusinessException(ErrorCodeOption.RealCfg_Copy_Faile.key,ErrorCodeOption.RealCfg_Copy_Faile.value);
        }
    }


}
