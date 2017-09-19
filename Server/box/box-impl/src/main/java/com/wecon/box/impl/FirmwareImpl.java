package com.wecon.box.impl;

import com.wecon.box.api.FirmwareApi;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.Firmware;
import com.wecon.box.entity.FirmwareDetail;
import com.wecon.box.entity.Page;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.util.BoxWebConfigContext;
import com.wecon.common.redis.RedisManager;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.core.BusinessException;
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
import java.util.UUID;

/**
 * Created by zengzhipeng on 2017/9/12.
 */
@Component
public class FirmwareImpl implements FirmwareApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Firmware getFirmware(long firmware_id) {
        String sql = getFirmwareSql() + " WHERE firmware_id=?";
        List<Firmware> list = jdbcTemplate.query(sql,
                new Object[]{firmware_id},
                new DefaultFirmwareRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public FirmwareDetail getFirmwareDetail(long firmware_id) {
        String sql = getFirmwareDetailSql() + " WHERE a.firmware_id=?";
        List<FirmwareDetail> list = jdbcTemplate.query(sql,
                new Object[]{firmware_id},
                new DefaultFirmwareDetailRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Page<FirmwareDetail> getFirmwareDetailList(String dev_model, String firm_info, int pageIndex, int pageSize) {
        String sqlCount = "SELECT count(1) " +
                "FROM firmware a " +
                "INNER JOIN file_storage b on b.file_id=a.file_id ";
        String sql = getFirmwareDetailSql();
        String condition = " WHERE 1=1 ";
        List<Object> params = new ArrayList<Object>();
        if (!CommonUtils.isNullOrEmpty(dev_model)) {
            condition += " AND a.dev_model = ?";
            params.add(dev_model);
        }
        if (!CommonUtils.isNullOrEmpty(firm_info)) {
            condition += " AND a.firm_info = ?";
            params.add(firm_info);
        }
        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount,
                params.toArray(),
                Integer.class);
        Page<FirmwareDetail> page = new Page<FirmwareDetail>(pageIndex, pageSize, totalRecord);
        String sort = " order by a.firmware_id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<FirmwareDetail> list = jdbcTemplate.query(sql,
                params.toArray(),
                new DefaultFirmwareDetailRowMapper());
        page.setList(list);
        return page;
    }

    @Override
    public long addFirmware(final Firmware model) {
        String sql = "select count(1) from firmware where firm_info = ? and version_code = ?  and version_name = ?  ";
        int ret = jdbcTemplate.queryForObject(sql,
                new Object[]{model.firm_info, model.version_code, model.version_name},
                Integer.class);
        if (ret > 0) {
            throw new BusinessException(ErrorCodeOption.FirmwareExisted.key, ErrorCodeOption.FirmwareExisted.value);
        }
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement("insert into firmware(`description`,dev_model,file_id,firm_info,version_code,version_name,create_date,update_date) values (?,?,?,?,?,?,current_timestamp(),current_timestamp() );", Statement.RETURN_GENERATED_KEYS);
                preState.setString(1, model.description);
                preState.setString(2, model.dev_model);
                preState.setLong(3, model.file_id);
                preState.setString(4, model.firm_info);
                preState.setString(5, model.version_code);
                preState.setString(6, model.version_name);

                return preState;
            }
        }, key);
        //从主键持有者中获得主键值
        model.firmware_id = key.getKey().longValue();
        return model.firmware_id;
    }

    @Override
    public boolean updateFirmware(Firmware model) {
        String sql = "select count(1) from firmware where firmware_id<>? and firm_info = ? and version_code = ?  and version_name = ?  ";
        int ret = jdbcTemplate.queryForObject(sql,
                new Object[]{model.firmware_id, model.firm_info, model.version_code, model.version_name},
                Integer.class);
        if (ret > 0) {
            throw new BusinessException(ErrorCodeOption.FirmwareExisted.key, ErrorCodeOption.FirmwareExisted.value);
        }
        sql = "update firmware set `description`=?,dev_model=?,file_id=?,firm_info=?,version_code=?,version_name=?,update_date=current_timestamp()  where firmware_id=?";
        jdbcTemplate.update(sql, new Object[]{model.description, model.dev_model, model.file_id, model.firm_info, model.version_code, model.version_name, model.firmware_id});
        return true;
    }

    @Override
    public boolean deleteFirmware(Firmware model) {
        String sql = "delete from firmware where firmware_id=?";
        jdbcTemplate.update(sql, new Object[]{model.firmware_id});
        sql = "delete from file_storage where file_id=?";
        jdbcTemplate.update(sql, new Object[]{model.file_id});
        return true;
    }

    @Override
    public List<FirmwareDetail> getFirmwareDetailList(String dev_model) {
        String sql = getFirmwareDetailSql() + "WHERE a.dev_model=?";
        List<FirmwareDetail> list = jdbcTemplate.query(sql,
                new Object[]{dev_model},
                new DefaultFirmwareDetailRowMapper());
        return list;
    }

    public static final class DefaultFirmwareDetailRowMapper implements RowMapper<FirmwareDetail> {
        @Override
        public FirmwareDetail mapRow(ResultSet rs, int i) throws SQLException {
            FirmwareDetail model = new FirmwareDetail();
            model.firmware_id = rs.getLong("firmware_id");
            model.version_code = rs.getString("version_code");
            model.version_name = rs.getString("version_name");
            model.dev_model = rs.getString("dev_model");
            model.description = rs.getString("description");
            model.file_id = rs.getLong("file_id");
            model.firm_info = rs.getString("firm_info");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");

            model.file_md5 = rs.getString("file_md5");
            model.file_name = rs.getString("file_name");
            model.file_size = rs.getLong("file_size");
            model.file_type = rs.getString("file_type");

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

    public static final class DefaultFirmwareRowMapper implements RowMapper<Firmware> {
        @Override
        public Firmware mapRow(ResultSet rs, int i) throws SQLException {
            Firmware model = new Firmware();
            model.firmware_id = rs.getLong("firmware_id");
            model.version_code = rs.getString("version_code");
            model.version_name = rs.getString("version_name");
            model.dev_model = rs.getString("dev_model");
            model.description = rs.getString("description");
            model.file_id = rs.getLong("file_id");
            model.firm_info = rs.getString("firm_info");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");

            return model;
        }
    }

    private String getFirmwareDetailSql() {
        String sql = "SELECT a.firmware_id,a.`description`,a.dev_model,a.file_id,a.firm_info,a.version_code,a.version_name,a.create_date,a.update_date, " +
                "b.file_name,b.file_md5,b.file_size,b.file_type " +
                "FROM firmware a " +
                "INNER JOIN file_storage b on b.file_id=a.file_id ";
        return sql;
    }

    private String getFirmwareSql() {
        String sql = "SELECT firmware_id,`description`,dev_model,file_id,firm_info,version_code,version_name,create_date,update_date " +
                "FROM firmware ";
        return sql;
    }
}
