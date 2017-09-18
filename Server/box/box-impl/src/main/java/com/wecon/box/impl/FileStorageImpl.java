package com.wecon.box.impl;

import com.wecon.box.api.FileStorageApi;
import com.wecon.box.entity.Device;
import com.wecon.box.entity.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.*;
import java.util.List;

/**
 * Created by zengzhipeng on 2017/9/13.
 */
@Component
public class FileStorageImpl implements FileStorageApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public long addFileStorage(final FileStorage model) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement("insert into file_storage(file_name,file_data,file_md5,file_type,file_size,create_date) values (?,?,?,?,?,current_timestamp() );", Statement.RETURN_GENERATED_KEYS);
                preState.setString(1, model.file_name);
                Blob blob = new SerialBlob(model.file_data);
                preState.setBlob(2, blob);
                preState.setString(3, model.file_md5);
                preState.setString(4, model.file_type);
                preState.setLong(5, model.file_size);

                return preState;
            }
        }, key);
        //从主键持有者中获得主键值
        model.file_id = key.getKey().longValue();
        return model.file_id;
    }

    @Override
    public FileStorage getFileStorage(long file_id) {
        String sql = "select file_id,file_name,file_data,file_md5,file_type,file_size,create_date from file_storage where file_id = ?";
        List<FileStorage> list = jdbcTemplate.query(sql, new Object[]{file_id}, new DefaultFileStorageRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static final class DefaultFileStorageRowMapper implements RowMapper<FileStorage> {
        @Override
        public FileStorage mapRow(ResultSet rs, int i) throws SQLException {
            FileStorage model = new FileStorage();
            model.file_id = rs.getLong("file_id");
            model.file_md5 = rs.getString("file_md5");
            model.file_name = rs.getString("file_name");
            model.file_type = rs.getString("file_type");
            Blob blob = rs.getBlob("file_data");
            int blobLength = (int) blob.length();
            model.file_data = blob.getBytes(1, blobLength);
            blob.free();
            model.create_date = rs.getTimestamp("create_date");
            model.file_size = rs.getLong("file_size");
            return model;
        }
    }
}
