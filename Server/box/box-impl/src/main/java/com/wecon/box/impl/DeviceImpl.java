package com.wecon.box.impl;

import com.wecon.box.api.*;
import com.wecon.box.entity.*;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.DeviceDir;
import com.wecon.box.filter.DeviceFilter;
import com.wecon.box.filter.DeviceSearchFilter;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @author lanpenghui 2017年8月4日上午10:01:01
 */
@Component
public class DeviceImpl implements DeviceApi {

    @Autowired
    ViewAccountRoleApi viewAccountRoleApi;
    @Autowired
    AlarmCfgApi alarmCfgApi;
    @Autowired
    RealHisCfgApi realHisCfgApi;
    @Autowired
    DevBindUserApi devBindUserApi;
    @Autowired
    AccountDirRelApi accountDirRelApi;
    @Autowired
    public PlatformTransactionManager transactionManager;

    @Autowired
    AccountDirApi accountDirApi;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final String SEL_COL = "device_id,machine_code,`password`,dev_model,`name`,remark,map,state,dir_id,create_date,update_date,max_his_data_count";

    @Override
    public long saveDevice(final Device model) {
        String sql = "select count(1) from device where machine_code = ? ";

        int ret = jdbcTemplate.queryForObject(sql, new Object[]{model.machine_code}, Integer.class);
        if (ret > 0) {
            throw new BusinessException(ErrorCodeOption.Device_Code_Is_Be_Used.key,
                    ErrorCodeOption.Device_Code_Is_Be_Used.value);
        }
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement(
                        "insert into device (machine_code,`password`,dev_model,`name`,remark,map,state,dir_id,create_date,update_date)values(?,?,?,?,?,?,?,?,current_timestamp(),?);",
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
        String sql = "select count(1) from device where machine_code = ? and device_id<>? ";

        int ret = jdbcTemplate.queryForObject(sql, new Object[]{model.machine_code, model.device_id}, Integer.class);
        if (ret > 0) {
            throw new BusinessException(ErrorCodeOption.Device_Code_Is_Be_Used.key,
                    ErrorCodeOption.Device_Code_Is_Be_Used.value);
        }
        sql = "update device set machine_code=?,password=?,dev_model=?,`name`=?,remark=?,map=?,state=?,dir_id=?,max_his_data_count=?,update_date=current_timestamp() where device_id=?";
        jdbcTemplate.update(sql, new Object[]{model.machine_code, model.password, model.dev_model, model.name,
                model.remark, model.map, model.state, model.dir_id,model.max_his_data_count, model.device_id});
        return true;
    }

    public boolean updateDeviceName(Integer deviceId, String deviceName, String remark) {
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();

        if (null == deviceId) {
            throw new BusinessException(ErrorCodeOption.Device_NotFound.key, ErrorCodeOption.Device_NotFound.value);
        }
        if (CommonUtils.isNullOrEmpty(deviceName) && CommonUtils.isNullOrEmpty(remark)) {
            return false;
        }
        if (!CommonUtils.isNullOrEmpty(deviceName)) {
            condition.append(" `name`= ? ");
            params.add(remark);
        }
        if (!CommonUtils.isNullOrEmpty(remark)) {
            condition.append(", remark= ? ");
            params.add(deviceName);
        }

        params.add(deviceId);
        String sql = "UPDATE device  SET " + condition + " update_date=current_timestamp WHERE device_id=?";

        if (jdbcTemplate.update(sql, params) <= 0) {
            return false;
        }
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
        String sql = "select " + SEL_COL + " from device where machine_code=? limit 1";
        List<Device> list = jdbcTemplate.query(sql, new Object[]{machine_code}, new DefaultDeviceRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public DeviceDir getDeviceDir(String machine_code) {
        String sql = "SELECT  "
                + "a.device_id,a.`name`,a.machine_code,a.`password`,a.dev_model,a.state,a.remark,a.create_date,c.username,c.account_id  "
                + "FROM  " + "device a  " + "LEFT JOIN dev_bind_user b ON a.device_id = b.device_id  "
                + "LEFT JOIN account c ON b.account_id=c.account_id   " + " where a.machine_code=?   ";
        Object[] args = new Object[]{machine_code};
        List<DeviceDir> list = jdbcTemplate.query(sql, args, new DefaultDeviceDirRowMapper());
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Device> getDeviceList(long account_id, long account_dir_id) {
        String sql = "select d.device_id,d.machine_code,d.`password`,d.dev_model,d.`name`,d.remark,d.map,d.state,d.dir_id,d.create_date,d.max_his_data_count,d.update_date from  account_dir ad,account_dir_rel adr,device d ,dev_bind_user dbu WHERE 1=1 and ad.`id`=adr.`acc_dir_id`AND ad.`type`=0 AND adr.`ref_id`=d.device_id AND dbu.account_id=ad.`account_id`AND dbu.device_id=d.device_id";
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
    public int getDeviceList(long account_id, int state) {
        String sql = "select count(d.device_id) from device d, dev_bind_user dbu where d.device_id=dbu.device_id and d.state=? and dbu.account_id=?";
        if (account_id < 0 || state < 0) {
            return 0;
        }
        List<Object> params = new ArrayList<Object>();
        params.add(state);
        params.add(account_id);
        int totalRecord = jdbcTemplate.queryForObject(sql, params.toArray(), Integer.class);

        return totalRecord;

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
            model.max_his_data_count = rs.getInt("max_his_data_count");
            return model;
        }
    }

    /*
     * 查询管理账户的盒子
     *
     * @Params acc_id 管理账户id sql:SELECT a.device_id, b.name FROM dev_bind_user a
     * INNER JOIN device b ON a.device_id=b.device_id WHERE
     * a.account_id='1000017';
     */
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

    public List<Map<String, Object>> getDevicesByGroup(long acc_id, int selAlarm) {
        // 获取管理员下的分组列表
        List<String[]> groupLst = jdbcTemplate.query(
                "SELECT ad.id, ad.`name` FROM account_dir ad WHERE ad.type=0 and ad.account_id=?",
                new Object[]{acc_id}, new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int i) throws SQLException {
                        return new String[]{rs.getLong("id") + "", rs.getString("name")};
                    }
                });
        if (null == groupLst || groupLst.size() == 0) {
            return null;
        }
        // 获取管理员下盒子列表
        List<String[]> deviceLst = jdbcTemplate.query(
                "SELECT d.device_id, d.`name`, d.map, d.state,d.machine_code,d.dev_model,d.remark, adr.acc_dir_id "
                        + "FROM dev_bind_user dbu, device d, account_dir_rel adr "
                        + "WHERE adr.ref_id=d.device_id and dbu.device_id=d.device_id and dbu.account_id=?",
                new Object[]{acc_id}, new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int i) throws SQLException {
                        return new String[]{rs.getLong("device_id") + "", rs.getString("name"), rs.getString("map"),
                                rs.getLong("acc_dir_id") + "", rs.getInt("state") + "", rs.getString("machine_code"), rs.getString("dev_model"), rs.getString("remark")};
                    }
                });
        if (null == deviceLst || deviceLst.size() == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String[] ss : deviceLst) {
            sb.append(",").append(ss[0]);
        }
        List<String> deviceIdLst = jdbcTemplate.query(
                "SELECT ac.device_id FROM alarm_cfg ac INNER JOIN alarm_cfg_data acd on ac.alarmcfg_id=acd.alarm_cfg_id where ac.state !=3 and acd.state=1 and ac.device_id in(" + sb.substring(1) + ")", new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int i) throws SQLException {
                        return rs.getLong("device_id") + "";
                    }
                });
        List<String[]> ndeviceLst = new ArrayList<>();
        if (null != deviceIdLst && deviceIdLst.size() > 0) {
            for (String[] device : deviceLst) {
                if (deviceIdLst.contains(device[0])) {
                    ndeviceLst.add(device);
                }
            }
        }

        if (1 == selAlarm) {
            deviceLst = ndeviceLst;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (String[] group : groupLst) {
            Map<String, Object> m = new HashMap<>();
            List<Map> l = new ArrayList<>();
            for (String[] device : deviceLst) {
                if (group[0].equals(device[3])) {
                    Map dm = new HashMap();
                    dm.put("boxId", device[0]);
                    dm.put("boxName", device[1]);
                    dm.put("map", device[2]);
                    dm.put("state", device[4]);
                    dm.put("isAlarm", deviceIdLst.contains(device[0]) ? 1 : 0);
                    dm.put("machineCode", device[5]);
                    dm.put("devModel", device[6]);
                    dm.put("remark", device[7]);
                    l.add(dm);
                }
            }
            m.put("groupName", group[1]);
            m.put("boxList", l);
            result.add(m);
        }
        return result;
    }

    @Override
    public List<String[]> getDeviceIdsAndMaxHisCount(){
        List<String[]> deviceIdLst = jdbcTemplate.query(
                "select device_id,max_his_data_count from device", new RowMapper() {
                    @Override
                    public String[] mapRow(ResultSet rs, int i) throws SQLException {
                        return new String[]{rs.getLong("device_id")+"", rs.getInt("max_his_data_count")+""};
                    }
                });
        return deviceIdLst;
    }

    @Override
    public void boundDevice(final long device_id, final String name, final long acc_dir_id) {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        try {
            tt.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus ts) {

                    Device modelUpdName = getDevice(device_id);
                    modelUpdName.name = name;
                    updateDevice(modelUpdName);

                    DevBindUser model = new DevBindUser();
                    model.account_id = AppContext.getSession().client.userId;
                    model.device_id = device_id;
                    devBindUserApi.saveDevBindUser(model);


		/*
         * 更新实时历史监控点迁移
		 */
                    alarmCfgApi.updatePointAccAndState(model.account_id, model.device_id, 1);
                    realHisCfgApi.updatePointAccAndState(model.account_id, model.device_id, 1);

		/*
		 * 分组信息添加
		 *
		 */
                    AccountDirRel accountDirRel = new AccountDirRel();
                    accountDirRel.ref_id = device_id;
                    accountDirRel.acc_dir_id = acc_dir_id;
                    accountDirRelApi.saveAccountDirRel(accountDirRel);
		/*
		 * 更新监控点分组迁移
		 */
                    accountDirApi.updateAccountBydeviceAndType(AppContext.getSession().client.userId, device_id);


                    return true;
                }
            });
        } catch (Exception e) {
            Logger.getLogger(AccountImpl.class.getName()).log(Level.SEVERE, null, e);
            throw new BusinessException(ErrorCodeOption.PIBox_Bound_False.key, ErrorCodeOption.PIBox_Bound_False.value);
            // throw new RuntimeException(e);
        }
    }

    /*
     * PIBox解除绑定
     */
    public boolean unbindDevice(final Integer accountId, final Integer deviceId) {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        try {
            tt.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus ts) {

					/*
                     * 删除 设备用户关联表
					 */
                    devBindUserApi.delDevBindUser(accountId, deviceId);
					/*
					 * 查询盒子下的监控点
					 */
                    ArrayList<Integer> realHisCfgIds = realHisCfgApi.findRealHisCfgIdSBydevice_id(deviceId);
                    if (realHisCfgIds.size() >= 0) {
						/*
						 * 设置实时历史监控点bound状态为0
						 */
//						realHisCfgApi.setBind_state(toIntArray(realHisCfgIds), 0);
                        realHisCfgApi.updatePointAccAndState(accountId, deviceId, 0);
						/*
						 * 删除视图账户监控点分组
						 */
                        viewAccountRoleApi.deleteViewAccountRoleByCfgId(realHisCfgIds, 1);
						/*
						 * 解除视图账户和监控点分组下的关系
						 */
                        accountDirRelApi.deleteViewAccAndPointRel(1, accountId, realHisCfgIds);
                        accountDirRelApi.deleteViewAccAndPointRel(2, accountId, realHisCfgIds);
                    }

                    ArrayList<Integer> alarmCfgIds = alarmCfgApi.findAlarmCfgIdSBydevice_id(deviceId);
                    if (alarmCfgIds.size() >= 0) {
						/*
						 * 设置实时历史监控点bound状态为0
						 */
                        //alarmCfgApi.setBind_state(deviceId,0);
                        alarmCfgApi.updatePointAccAndState(accountId, deviceId, 0);
						/*
						 * 删除视图账户监控点分组
						 */
                        viewAccountRoleApi.deleteViewAccountRoleByCfgId(alarmCfgIds, 2);
						/*
						 * 解除视图账户和监控点分组下的关系
						 */
                        accountDirRelApi.deleteViewAccAndPointRel(3, accountId, alarmCfgIds);
                    }
					/*
					 * 删除管理员与盒子分组关系 DELETE FROM account_dir_rel WHERE
					 * ref_id=1111 AND acc_dir_id IN (SELECT id FROM account_dir
					 * WHERE type=0 AND account_id=12);
					 */
                    accountDirRelApi.deleteAccDeviceRel(deviceId, accountId);

                    return true;
                }
            });
        } catch (Exception e) {
            Logger.getLogger(AccountImpl.class.getName()).log(Level.SEVERE, null, e);
            throw new BusinessException(ErrorCodeOption.PIBox_Bound_False.key, ErrorCodeOption.PIBox_Bound_False.value);
            // throw new RuntimeException(e);
        }
        return true;
    }

    int[] toIntArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = list.get(i);
        return ret;
    }

    public Page<DeviceDir> showAllDeviceDir(String accountId, Integer state, int pageNum, int pageSize) {
        StringBuffer condition = new StringBuffer("");
        Object[] args = null;
        Object[] argsCount = null;
        if ((!accountId.equals("")) && (accountId != null)) {
            condition.append(" WHERE c.account_id =?  ");
            argsCount = new Object[]{accountId};
        } else if (!state.equals("") && state != -1 && CommonUtils.isNotNull(state)) {
            condition.append("WHERE a.state=? ");
            argsCount = new Object[]{state};
        } else {
            argsCount = new Object[]{};
        }

        String sqlCount = "SELECT  " + "  count(1)  " + "FROM  " + "device a  "
                + "LEFT JOIN dev_bind_user b ON a.device_id = b.device_id  "
                + "LEFT JOIN account c ON b.account_id=c.account_id " + condition;
        Integer count = jdbcTemplate.queryForObject(sqlCount, argsCount, Integer.class);
        Page<DeviceDir> page = new Page<DeviceDir>(pageNum, pageSize, count);
        String sql = "SELECT  "
                + "a.device_id,a.`name`,a.machine_code,a.`password`,a.dev_model,a.state,a.remark,a.create_date,c.username,c.account_id,d.f_id,d.f_name,d.f_ver "
                + "FROM  " + "device a  " + "LEFT JOIN dev_bind_user b ON a.device_id = b.device_id  "
                + "LEFT JOIN account c ON b.account_id=c.account_id   "
                + "LEFT JOIN dev_firm d ON a.device_id=d.device_id  " + condition
                + " order by a.device_id desc  LIMIT ?,?  ";
        if ((!accountId.equals("")) && (accountId != null)) {
            args = new Object[]{accountId, page.getStartIndex(), page.getPageSize()};
        } else if (!state.equals("") && state != -1 && CommonUtils.isNotNull(state)) {
            args = new Object[]{state, page.getStartIndex(), page.getPageSize()};
        } else {
            args = new Object[]{page.getStartIndex(), page.getPageSize()};
        }

        List<DeviceDir> list = jdbcTemplate.query(sql, args, new RowMapper<DeviceDir>() {
            @Override
            public DeviceDir mapRow(ResultSet resultSet, int i) throws SQLException {
                DeviceDir model = new DeviceDir();
                model.create_date = resultSet.getTimestamp("create_date");
                model.dev_model = resultSet.getString("dev_model");
                model.device_id = resultSet.getLong("device_id");
                model.machine_code = resultSet.getString("machine_code");
                model.name = resultSet.getString("name");
                model.remark = resultSet.getString("remark");
                model.password = resultSet.getString("password");
                model.state = resultSet.getInt("state");
                model.accountId = resultSet.getLong("account_id");
                model.username = resultSet.getString("username");
                model.fId = resultSet.getLong("f_id");
                model.fName = resultSet.getString("f_name");
                model.fVer = resultSet.getString("f_ver");
                return model;
            }
        });
        page.setList(list);
        return page;
    }

    @Override
    public Page<DeviceDir> getDeviceByBound(Integer state, Integer pageNum, Integer pageSize) {
        StringBuffer condition = new StringBuffer("");
        Page<DeviceDir> page = null;
        Object[] args = null;
        Object[] argCount = null;
        if (state != -1 && null != state) {
            condition.append(" where a.state=?  ");
            argCount = new Object[]{state};
        }
        String sqlCount = "SELECT count(1) FROM device a INNER JOIN dev_bind_user b ON a.device_id = b.device_id "
                + condition;
        Integer count = jdbcTemplate.queryForObject(sqlCount, argCount, Integer.class);
        if (state != -1 && null != state) {
            page = new Page<DeviceDir>(pageNum, pageSize, count);
            args = new Object[]{state, page.getStartIndex(), page.getPageSize()};
        } else {
            page = new Page<DeviceDir>(pageNum, pageSize, count);
            args = new Object[]{page.getStartIndex(), page.getPageSize()};
        }
        if (count <= 0) {
            page.setList(new ArrayList<DeviceDir>());
            return page;
        }
        String sql = "SELECT a.device_id,a.`name`,a.machine_code,a.`password`,a.dev_model,a.state,a.remark,a.create_date,c.username,c.account_id  FROM device a INNER JOIN dev_bind_user b ON a.device_id = b.device_id  LEFT JOIN account c ON b.account_id=c.account_id  "
                + condition + "  order by a.device_id desc  LIMIT ?,?";
        page.setList(jdbcTemplate.query(sql, args, new DefaultDeviceDirRowMapper()));
        return page;
    }


    @Override
    public Page<DeviceDir> getAllDeviceByFilter(DeviceSearchFilter filter, Integer pageNum, Integer pageSize) {
        String condition = new String("");
        String sqlCount = "SELECT  "
                + " count(0) "
                + "FROM  " + "device a  " + "LEFT JOIN dev_bind_user b ON a.device_id = b.device_id  "
                + "LEFT JOIN account c ON b.account_id=c.account_id   "
                + "LEFT JOIN dev_firm d ON a.device_id=d.device_id  WHERE 1=1 ";
        String sql = "SELECT  "
                + "a.device_id,a.`name`,a.machine_code,a.`password`,a.dev_model,a.state,a.remark,a.create_date,c.username,c.account_id,d.f_id,d.f_name,d.f_ver "
                + "FROM  " + "device a  " + "LEFT JOIN dev_bind_user b ON a.device_id = b.device_id  "
                + "LEFT JOIN account c ON b.account_id=c.account_id   "
                + "LEFT JOIN dev_firm d ON a.device_id=d.device_id  WHERE 1=1";
//                + condition
//                + " order by a.device_id desc  LIMIT ?,?  ";
        List<Object> params = new ArrayList<Object>();
        if (filter.accountId != null && filter.accountId > 0) {
            condition += " and b.account_id=?  ";
            params.add(filter.accountId);
        }
        if (filter.device_id != null && filter.device_id > 0) {
            condition += " and a.device_id=? ";
            params.add(filter.device_id);
        }
        if (filter.machine_code != null && !filter.machine_code.equals("-1")) {
            condition += " and a.machine_code=?  ";
            params.add(filter.machine_code);
        }
        if (filter.bind_state != null && filter.bind_state != -1) {
            if (filter.bind_state == 0) {
                condition += " AND  NOT EXISTS(SELECT device_id FROM dev_bind_user b  WHERE b.device_id=a.device_id) ";
            } else {
                condition += " AND  EXISTS(SELECT device_id FROM dev_bind_user b  WHERE b.device_id=a.device_id) ";
            }
        }
        if (filter.state != -1) {
            condition += " and a.state=? ";
            params.add(filter.state);
        }
        sqlCount += condition.toString();
        int totalRecord = jdbcTemplate.queryForObject(sqlCount,
                params.toArray(),
                Integer.class);
        Page<DeviceDir> page = new Page<DeviceDir>(pageNum, pageSize, totalRecord);
        params.add(page.getStartIndex());
        params.add(page.getPageSize());
        List<DeviceDir> list = jdbcTemplate.query(sql + condition + " limit ?,? ",
                params.toArray(),
                new RowMapper<DeviceDir>() {
                    @Override
                    public DeviceDir mapRow(ResultSet resultSet, int i) throws SQLException {
                        DeviceDir model = new DeviceDir();
                        model.create_date = resultSet.getTimestamp("create_date");
                        model.dev_model = resultSet.getString("dev_model");
                        model.device_id = resultSet.getLong("device_id");
                        model.machine_code = resultSet.getString("machine_code");
                        model.name = resultSet.getString("name");
                        model.remark = resultSet.getString("remark");
                        model.password = resultSet.getString("password");
                        model.state = resultSet.getInt("state");
                        model.accountId = resultSet.getLong("account_id");
                        model.username = resultSet.getString("username");
                        model.fId = resultSet.getLong("f_id");
                        model.fName = resultSet.getString("f_name");
                        model.fVer = resultSet.getString("f_ver");
                        return model;
                    }
                });
        page.setList(list);
        return page;
    }

    @Override
    public void updateDeviceUse(DeviceUse deviceUse) {
        String sql = "insert into device_use(device_id,use_code,use_name,other_use_name,create_date)values(?,?,?,?,current_timestamp)ON DUPLICATE KEY UPDATE update_date=current_timestamp,use_code=?,use_name=?,other_use_name=? ";
        Object[] args = null;
        if (deviceUse.useCode == 999) {
            args = new Object[]{deviceUse.deviceId, deviceUse.useCode, "", deviceUse.otherUseName, deviceUse.useCode, "", deviceUse.otherUseName};
        } else {
            args = new Object[]{deviceUse.deviceId, deviceUse.useCode,deviceUse.useName,"",deviceUse.useCode,deviceUse.useName,""};
        }
        jdbcTemplate.update(sql,args);
    }

    @Override
    public DeviceUse getDeviceUse(long deviceId) {
        String sql= "SELECT device_id,use_code,use_name,other_use_name FROM device_use where device_id =?";
        Object[] args =new Object[]{deviceId};
        List<DeviceUse> deviceUses= jdbcTemplate.query(sql, args, new RowMapper<DeviceUse>() {
            public DeviceUse mapRow(ResultSet resultSet, int i) throws SQLException {
                DeviceUse model = new DeviceUse();
                model.otherUseName = resultSet.getString("other_use_name");
                model.deviceId = resultSet.getLong("device_id");
                model.useCode = resultSet.getInt("use_code");
                model.useName = resultSet.getString("use_name");
                return model;
            }
        });
        if(deviceUses.size()<=0){
            return null;
        }
        return deviceUses.get(0);
    }


    @Override
    public Page<DeviceDir> getDeviceByUnbound(Integer state, Integer pageNum, Integer pageSize) {
        StringBuffer condition = new StringBuffer("");
        Object[] args = null;
        Integer count = null;
        Page<DeviceDir> page = null;
        if (CommonUtils.isNotNull(state) && state != -1) {
            condition.append("and a.state=? ");
            String sqlCount = "SELECT count(1) FROM device a  WHERE not EXISTS(SELECT device_id FROM dev_bind_user b where b.device_id=a.device_id) "
                    + condition;
            Object[] argsCount = new Object[]{state};
            count = jdbcTemplate.queryForObject(sqlCount, argsCount, Integer.class);
            page = new Page<DeviceDir>(pageNum, pageSize, count);
            args = new Object[]{state, page.getStartIndex(), page.getPageSize()};
        } else {
            String sqlCount = "SELECT count(1) FROM device a  WHERE not EXISTS(SELECT device_id FROM dev_bind_user b where b.device_id=a.device_id) "
                    + condition;
            Object[] argsCount = new Object[]{};
            count = jdbcTemplate.queryForObject(sqlCount, argsCount, Integer.class);
            page = new Page<DeviceDir>(pageNum, pageSize, count);
            args = new Object[]{page.getStartIndex(), page.getPageSize()};
        }
        if (count <= 0) {
            page.setList(new ArrayList<DeviceDir>());
            return page;
        }
        String sql = "SELECT a.device_id,a.`name`,a.machine_code,a.`password`,a.dev_model,a.state,a.remark,a.create_date  FROM device a  where NOT EXISTS(SELECT device_id FROM dev_bind_user b  WHERE b.device_id=a.device_id)  "
                + condition + "  order by a.device_id desc  LIMIT ?,?";
        page.setList(jdbcTemplate.query(sql, args, new RowMapper<DeviceDir>() {
            @Override
            public DeviceDir mapRow(ResultSet resultSet, int i) throws SQLException {
                DeviceDir model = new DeviceDir();
                model.create_date = resultSet.getTimestamp("create_date");
                model.dev_model = resultSet.getString("dev_model");
                model.device_id = resultSet.getLong("device_id");
                model.machine_code = resultSet.getString("machine_code");
                model.name = resultSet.getString("name");
                model.remark = resultSet.getString("remark");
                model.password = resultSet.getString("password");
                model.state = resultSet.getInt("state");
                return model;
            }
        }));
        return page;
    }


    public static final class DefaultDeviceDirRowMapper implements RowMapper<DeviceDir> {
        public DeviceDir mapRow(ResultSet resultSet, int i) throws SQLException {
            DeviceDir model = new DeviceDir();
            model.create_date = resultSet.getTimestamp("create_date");
            model.dev_model = resultSet.getString("dev_model");
            model.device_id = resultSet.getLong("device_id");
            model.machine_code = resultSet.getString("machine_code");
            model.name = resultSet.getString("name");
            model.remark = resultSet.getString("remark");
            model.password = resultSet.getString("password");
            model.state = resultSet.getInt("state");
            model.accountId = resultSet.getLong("account_id");
            model.username = resultSet.getString("username");
            return model;
        }
    }

}
