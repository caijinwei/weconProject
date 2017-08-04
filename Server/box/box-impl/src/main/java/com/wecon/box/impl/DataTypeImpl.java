package com.wecon.box.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import com.wecon.box.api.DataTypeApi;
import com.wecon.box.entity.DataType;

/**
 * @author lanpenghui 2017年8月2日
 */
@Component
public class DataTypeImpl implements DataTypeApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL = "data_id,data_name,data_value,create_date,update_date";

	@Override
	public long saveDataType(final DataType model) {
		KeyHolder key = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement preState = con.prepareStatement(
						"insert into data_type (data_name,data_value,create_date,update_date)values(?,?,current_timestamp(),current_timestamp());",
						Statement.RETURN_GENERATED_KEYS);

				preState.setString(1, model.data_name);
				preState.setInt(2, model.data_value);

				return preState;
			}
		}, key);
		// 从主键持有者中获得主键值
		return key.getKey().longValue();
	}

	@Override
	public boolean updateDataType(DataType model) {
		String sql = "update data_type set data_name=?,data_value=?,update_date=current_timestamp() where data_id=?";

		jdbcTemplate.update(sql, new Object[] { model.data_name, model.data_value, model.data_id });
		return true;
	}

	@Override
	public DataType getDataType(long data_id) {
		String sql = "select " + SEL_COL + " from data_type where data_id=?";
		List<DataType> list = jdbcTemplate.query(sql, new Object[] { data_id }, new DefaultDataTypeRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public void delDataType(long data_id) {
		String sql = "delete from  data_type  where data_id=?";
		jdbcTemplate.update(sql, new Object[] { data_id });

	}

	public static final class DefaultDataTypeRowMapper implements RowMapper<DataType> {

		@Override
		public DataType mapRow(ResultSet rs, int i) throws SQLException {
			DataType model = new DataType();
			model.data_id = rs.getLong("data_id");
			model.data_name = rs.getString("data_name");
			model.data_value = rs.getInt("data_value");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");
			return model;
		}
	}
}
