package com.wecon.box.impl;

import com.wecon.box.api.DriverApi;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.Driver;
import com.wecon.box.entity.DriverDetail;
import com.wecon.box.entity.FirmwareDetail;
import com.wecon.box.entity.Page;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.util.BoxWebConfigContext;
import com.wecon.common.redis.RedisManager;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.core.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by zengzhipeng on 2017/9/16.
 */
@Component
public class DriverImpl implements DriverApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean batchAddDriver(final List<Driver> driverList) {
        /*String sql="insert into driver(file_id,file_md5,`type`,driver,create_date,update_date)" +
                " values(?,?,?,?,current_timestamp(),current_timestamp())";
        jdbcTemplate.batchUpdate(sql,new BatchPreparedStatementSetter() {

            @Override
            public int getBatchSize() {
                return driverList.size();
            }
            @Override
            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                ps.setLong(1, driverList.get(i).file_id);
                ps.setString(2, driverList.get(i).file_md5);
                ps.setString(3, driverList.get(i).type);
                ps.setString(4, driverList.get(i).driver);
            }
        });*/

        for (Driver it : driverList) {
            saveDriver(it);
        }
        return true;
    }

    @Override
    public boolean saveDriver(Driver model) {
        String sql = "update driver set driver=?,file_id=?,file_md5=?,`description`=?,update_date=current_timestamp() where `type` = ? ";
        if (jdbcTemplate.update(sql, new Object[]{model.driver, model.file_id, model.file_md5, model.type,model.description}) == 0) {
            sql = "insert into driver(`driver`,file_id,file_md5,`type`,`description`,create_date,update_date) values (?,?,?,?,?,current_timestamp(),current_timestamp() );";
            jdbcTemplate.update(sql, new Object[]{model.driver, model.file_id, model.file_md5, model.type,model.description});
        }
        return true;
    }

    @Override
    public Page<Driver> getDriverList(String type, String driver, int pageIndex, int pageSize) {
        String sqlCount = "SELECT count(1) from driver where 1=1 ";
        String sql = "SELECT driver_id,`driver`,file_id,file_md5,`type`,create_date,update_date,`description` from driver where 1=1 ";
        String condition = "";
        List<Object> params = new ArrayList<Object>();
        if (!CommonUtils.isNullOrEmpty(type)) {
            condition += " AND `type` = ?";
            params.add(type);
        }
        if (!CommonUtils.isNullOrEmpty(driver)) {
            condition += " AND `driver` = ?";
            params.add(driver);
        }
        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount,
                params.toArray(),
                Integer.class);
        Page<Driver> page = new Page<Driver>(pageIndex, pageSize, totalRecord);
        String sort = " order by driver_id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<Driver> list = jdbcTemplate.query(sql,
                params.toArray(),
                new DefaultDriverRowMapper());
        page.setList(list);
        return page;
    }

    @Override
    public Driver getDriver(Long driver_id) {
        String sql = "SELECT driver_id,`driver`,file_id,file_md5,`type`,create_date,update_date,`description` from driver where driver_id=?";
        List<Driver> list = jdbcTemplate.query(sql,
                new Object[]{driver_id},
                new DefaultDriverRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Driver getDriver(String type) {
        String sql = "SELECT driver_id,`driver`,file_id,file_md5,`type`,create_date,update_date,`description` from driver where `type`=?";
        List<Driver> list = jdbcTemplate.query(sql,
                new Object[]{type},
                new DefaultDriverRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public DriverDetail getDriverDetail(Long driver_id) {
        String sql = "SELECT a.driver_id,a.`driver`,a.file_id,a.file_md5,a.`type`,a.create_date,a.update_date,a.`description`,b.file_name,b.file_type,b.file_size from driver a" +
                " INNER JOIN file_storage b on b.file_id=a.file_id " +
                " where a.driver_id=? ";
        List<DriverDetail> list = jdbcTemplate.query(sql,
                new Object[]{driver_id},
                new DefaultDriverDetailRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public boolean delDriver(Driver model) {
        String sql = "delete from driver where driver_id=?";
        jdbcTemplate.update(sql, new Object[]{model.driver_id});
        sql = "delete from file_storage where file_id=?";
        jdbcTemplate.update(sql, new Object[]{model.file_id});
        return true;
    }

    public static final class DefaultDriverRowMapper implements RowMapper<Driver> {
        @Override
        public Driver mapRow(ResultSet rs, int i) throws SQLException {
            Driver model = new Driver();
            model.driver_id = rs.getLong("driver_id");
            model.driver = rs.getString("driver");
            model.type = rs.getString("type");
            model.file_id = rs.getLong("file_id");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");
            model.file_md5 = rs.getString("file_md5");
            model.description = rs.getString("description");

            return model;
        }
    }

    public static final class DefaultDriverDetailRowMapper implements RowMapper<DriverDetail> {
        @Override
        public DriverDetail mapRow(ResultSet rs, int i) throws SQLException {
            DriverDetail model = new DriverDetail();
            model.driver_id = rs.getLong("driver_id");
            model.driver = rs.getString("driver");
            model.type = rs.getString("type");
            model.file_id = rs.getLong("file_id");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");
            model.file_md5 = rs.getString("file_md5");
            model.description = rs.getString("description");

            model.file_name = rs.getString("file_name");
            model.file_type = rs.getString("file_type");
            model.file_size = rs.getLong("file_size");

            String redisKey = String.format(ConstKey.REDIS_FILE_DOWNLOAD_TOKEN, model.file_id);
            String token = RedisManager.get(ConstKey.REDIS_GROUP_NAME, redisKey);
            if (CommonUtils.isNullOrEmpty(token)) {
                token = UUID.randomUUID().toString().replace("-", "");
                RedisManager.set(ConstKey.REDIS_GROUP_NAME, redisKey, token, ConstKey.FILEDOWNLOAD_EXPIRE_TIME);
            }
            model.file_url = BoxWebConfigContext.boxWebConfig.getFileDownloadUrl() + "?id=" + model.file_id + "&t=" + token;

            return model;
        }
    }
}
