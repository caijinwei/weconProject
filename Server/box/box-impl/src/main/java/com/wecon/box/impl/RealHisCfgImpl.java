package com.wecon.box.impl;

import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.filter.RealHisCfgFilter;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author lanpenghui 2017年8月2日
 */
@Component
public class RealHisCfgImpl implements RealHisCfgApi {
	@Override
	public long saveRealHisCfg(RealHisCfg model) {
		return 0;
	}

	@Override
	public boolean updateRealHisCfg(RealHisCfg model) {
		return false;
	}

	@Override
	public RealHisCfg getRealHisCfg(long id) {
		return null;
	}

	@Override
	public List<RealHisCfg> getRealHisCfg(long plc_id, int state) {
		return null;
	}

	@Override
	public void delRealHisCfg(long id) {

	}

	@Override
	public Page<RealHisCfg> getRealHisCfgList(RealHisCfgFilter filter, int pageIndex, int pageSize) {
		return null;
	}
//	@Autowired
//	private JdbcTemplate jdbcTemplate;
//	private final String SEL_COL = "id,data_id,account_id,plc_id,`name`,addr,addr_type,`describe`,digit_count,data_limit,his_cycle,data_type,state,create_date,update_date";
//
//	@Override
//	public long saveRealHisCfg(final RealHisCfg model) {
//		KeyHolder key = new GeneratedKeyHolder();
//		jdbcTemplate.update(new PreparedStatementCreator() {
//			@Override
//			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
//				PreparedStatement preState = con.prepareStatement(
//						"insert into real_his_cfg(data_id,account_id,plc_id,`name`,addr,addr_type,`describe`,digit_count,data_limit,his_cycle,data_type,state,create_date,update_date)values(?,?,?,?,?,?,?,?,?,?,?,?,current_timestamp(),current_timestamp());",
//						Statement.RETURN_GENERATED_KEYS);
//				preState.setLong(1, model.data_id);
//				preState.setLong(2, model.account_id);
//				preState.setLong(3, model.plc_id);
//				preState.setString(4, model.name);
//				preState.setString(5, model.addr);
//				preState.setInt(6, model.addr_type);
//				preState.setString(7, model.describe);
//				preState.setString(8, model.digit_count);
//				preState.setString(9, model.data_limit);
//				preState.setInt(10, model.his_cycle);
//				preState.setInt(11, model.data_type);
//				preState.setInt(12, model.state);
//
//				return preState;
//			}
//		}, key);
//		// 从主键持有者中获得主键值
//		return key.getKey().longValue();
//	}
//
//	@Override
//	public boolean updateRealHisCfg(RealHisCfg model) {
//		String sql = "update real_his_cfg SET data_id=?,account_id=?,plc_id=?,`name`=?,addr=?,addr_type=?,`describe`=?,digit_count=?,data_limit=?,his_cycle=?,data_type=?,state=?,update_date=current_timestamp() where id=?";
//		jdbcTemplate.update(sql,
//				new Object[] { model.data_id, model.account_id, model.plc_id, model.name, model.addr, model.addr_type,
//						model.describe, model.digit_count, model.data_limit, model.his_cycle, model.data_type,
//						model.state });
//
//		return true;
//	}
//
//	@Override
//	public RealHisCfg getRealHisCfg(long id) {
//
//		String sql = "select " + SEL_COL + " from real_his_cfg where id=?";
//		List<RealHisCfg> list = jdbcTemplate.query(sql, new Object[] { id }, new DefaultRealHisCfgRowMapper());
//		if (!list.isEmpty()) {
//			return list.get(0);
//		}
//
//		return null;
//	}
//
//	@Override
//	public List<RealHisCfg> getRealHisCfg(long plc_id, int state) {
//		String sql = "select " + SEL_COL + " from real_his_cfg where plc_id=? and state=?";
//		List<RealHisCfg> list = jdbcTemplate.query(sql, new Object[] { plc_id, state },
//				new DefaultRealHisCfgRowMapper());
//		if (!list.isEmpty()) {
//			return list;
//		}
//
//		return null;
//	}
//
//	@Override
//	public void delRealHisCfg(long id) {
//		String sql = "delete from  real_his_cfg  where id=?";
//		jdbcTemplate.update(sql, new Object[] { id });
//	}
//
//	@Override
//	public Page<RealHisCfg> getRealHisCfgList(RealHisCfgFilter filter, int pageIndex, int pageSize) {
//		String sqlCount = "select count(0) from real_his_cfg where 1=1";
//		String sql = "select " + SEL_COL + " from real_his_cfg where 1=1";
//		StringBuffer condition = new StringBuffer("");
//		List<Object> params = new ArrayList<Object>();
//		if (!CommonUtils.isNullOrEmpty(filter.id)) {
//			condition.append(" and id = ? ");
//			params.add(filter.id);
//		}
//		if (!CommonUtils.isNullOrEmpty(filter.data_id)) {
//			condition.append(" and data_id = ? ");
//			params.add(filter.data_id);
//		}
//		if (!CommonUtils.isNullOrEmpty(filter.account_id)) {
//			condition.append(" and account_id = ? ");
//			params.add(filter.account_id);
//		}
//		if (!CommonUtils.isNullOrEmpty(filter.plc_id)) {
//			condition.append(" and plc_id = ? ");
//			params.add(filter.plc_id);
//		}
//		if (!CommonUtils.isNullOrEmpty(filter.name)) {
//			condition.append(" and name like ? ");
//			params.add("%" + filter.name + "%");
//		}
//		if (!CommonUtils.isNullOrEmpty(filter.addr)) {
//			condition.append(" and addr like ? ");
//			params.add("%" + filter.addr + "%");
//		}
//		if (!CommonUtils.isNullOrEmpty(filter.addr_type)) {
//			condition.append(" and addr_type = ? ");
//			params.add(filter.addr_type);
//		}
//
//		if (!CommonUtils.isNullOrEmpty(filter.describe)) {
//			condition.append(" and describe like ? ");
//			params.add("%" + filter.describe + "%");
//		}
//		if (!CommonUtils.isNullOrEmpty(filter.digit_count)) {
//			condition.append(" and digit_count like ? ");
//			params.add("%" + filter.digit_count + "%");
//		}
//		if (!CommonUtils.isNullOrEmpty(filter.data_limit)) {
//			condition.append(" and data_limit like ? ");
//			params.add("%" + filter.data_limit + "%");
//		}
//
//		if (!CommonUtils.isNullOrEmpty(filter.his_cycle)) {
//			condition.append(" and his_cycle = ? ");
//			params.add(filter.his_cycle);
//		}
//		if (!CommonUtils.isNullOrEmpty(filter.data_type)) {
//			condition.append(" and data_type = ? ");
//			params.add(filter.data_type);
//		}
//		if (!CommonUtils.isNullOrEmpty(filter.state)) {
//			condition.append(" and state = ? ");
//			params.add(filter.state);
//		}
//		sqlCount += condition;
//		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
//		Page<RealHisCfg> page = new Page<RealHisCfg>(pageIndex, pageSize, totalRecord);
//		String sort = " order by id desc";
//		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
//		List<RealHisCfg> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealHisCfgRowMapper());
//		page.setList(list);
//		return page;
//
//	}
//
//	public static final class DefaultRealHisCfgRowMapper implements RowMapper<RealHisCfg> {
//
//		@Override
//		public RealHisCfg mapRow(ResultSet rs, int i) throws SQLException {
//			RealHisCfg model = new RealHisCfg();
//			model.id = rs.getLong("id");
//			model.data_id = rs.getLong("data_id");
//			model.account_id = rs.getLong("account_id");
//			model.plc_id = rs.getLong("plc_id");
//			model.name = rs.getString("name");
//			model.addr = rs.getString("addr");
//			model.addr_type = rs.getInt("addr_type");
//			model.describe = rs.getString("describe");
//			model.digit_count = rs.getString("digit_count");
//			model.data_limit = rs.getString("data_limit");
//			model.his_cycle = rs.getInt("his_cycle");
//			model.data_type = rs.getInt("data_type");
//			model.state = rs.getInt("state");
//			model.create_date = rs.getTimestamp("create_date");
//			model.update_date = rs.getTimestamp("update_date");
//
//			return model;
//		}
//	}

}
