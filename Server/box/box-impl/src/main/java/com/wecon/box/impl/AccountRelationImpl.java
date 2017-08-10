package com.wecon.box.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import com.wecon.box.api.AccountRelationApi;
import com.wecon.box.entity.AccountRelation;

/**
 * @author lanpenghui 2017年8月10日上午9:01:13
 */
@Component
public class AccountRelationImpl implements AccountRelationApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL = "manager_id,view_id";

	@Override
	public AccountRelation getAccountRelation(long view_id) {
		String sql = "select" + SEL_COL + "from account_relation where view_id=?";
		List<AccountRelation> list = jdbcTemplate.query(sql, new Object[] { view_id },
				new DefaultAccountRelationRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public static final class DefaultAccountRelationRowMapper implements RowMapper<AccountRelation> {

		@Override
		public AccountRelation mapRow(ResultSet rs, int i) throws SQLException {
			AccountRelation model = new AccountRelation();
			model.manager_id = rs.getLong("manager_id");
			model.view_id = rs.getLong("view_id");

			return model;
		}
	}

}
