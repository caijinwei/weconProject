package com.wecon.box.impl;

import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.ViewAccountRole;
import com.wecon.box.entity.ViewAccountRoleView;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.restful.core.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by caijinw on 2017/8/10.
 */
@Component
public class ViewAccountRoleImpl implements ViewAccountRoleApi {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    public PlatformTransactionManager transactionManager;

    @Override
    public long saveViewAccountRole(ViewAccountRole model) {
        return 0;
    }

    @Override
    public boolean updateViewAccountRole(ViewAccountRole model) {
        return false;
    }

    @Override
    public void delViewAccountRole(long view_id) {
    }

    @Override
    public Page<ViewAccountRole> getViewAccountRoleList(ViewAccountRoleFilter filter, int pageIndex, int pageSize) {
        return null;
    }

    /*
    * @Param view_id  account_id
    * 		视图账号ID  管理员账户ID
    *
    *  获取 该视图账户中未有的监控点
    *  	需要分页
    *  	id   state  name  digit_count addr  addr_type desceibe
    * */
    @Override
    public Page<RealHisCfg> getViewRealHisCfgByViewAndAccId(long view_id, long account_id, Integer type, Integer pageIndex, Integer pageSize) {
        Object[] args0 = new Object[]{account_id, type, view_id};
        String sqlCount = "select count(*) from real_his_cfg a WHERE account_id=? AND a.data_type=? AND id NOT IN(SELECT cfg_id FROM view_account_role where view_id=?);";
        int totalRecord = jdbcTemplate.queryForObject(sqlCount, args0, Integer.class);
        Page<RealHisCfg> page = new Page<RealHisCfg>(pageIndex, pageSize, totalRecord);

        Object[] args = new Object[]{account_id, type, view_id, page.getStartIndex(),page.getPageSize()};
        String sql = "select a.id, a.state,a.name , a.digit_count,a.addr , a.addr_type,a.describe from real_his_cfg a WHERE account_id=?  AND a.data_type=? AND id NOT IN(SELECT cfg_id FROM view_account_role where view_id=?)LIMIT ?,?";
        List<RealHisCfg> list = jdbcTemplate.query(sql, args, new RowMapper() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                RealHisCfg model = new RealHisCfg();
                model.id = resultSet.getLong("id");
                model.state = resultSet.getInt("state");
                model.name = resultSet.getString("name");
                model.digit_count = resultSet.getString("digit_count");
                model.addr = resultSet.getString("addr");
                model.addr_type = resultSet.getInt("addr_type");
                model.describe = resultSet.getString("describe");
                return model;
            }
        });
        page.setList(list);
        System.out.println(list);
        return page;
    }

    /*
        * SELECT * FROM view_account_role a,real_his_cfg b WHERE a.cfg_id=b.id AND a.view_id="1" AND a.cfg_type='1'AND b.data_type='0';
        * cgf_type ：
        *           1实时历史监控点
        *           2报警监控点
        * data_type
        *           0：实时监控点
        *           1：历史监控点
        * */
    @Override
    public Page<ViewAccountRoleView> getViewAccountRoleViewByViewID(Integer data_type, long view_id,Integer pageIndex,Integer pageSize) {
        if (data_type == null || data_type < 0 || data_type > 1) {
            return null;
        }
        Object[] args = new Object[]{data_type, view_id};
        String sqlCount="SELECT COUNT(*) FROM view_account_role a INNER JOIN real_his_cfg b ON a.cfg_id = b.id WHERE a.cfg_type = '1' AND b.data_type = ? AND a.view_id =?";
        int totalCount=jdbcTemplate.queryForObject(sqlCount,args,Integer.class);
        Page<ViewAccountRoleView> page =new Page<ViewAccountRoleView>(pageIndex,pageSize,totalCount);
        Object[] args1=new Object[]{data_type,view_id,page.getStartIndex(),page.getPageSize()};
        String sql = "SELECT a.cfg_id,a.role_type,b.name,b.addr,b.state FROM view_account_role a INNER JOIN real_his_cfg b ON a.cfg_id = b.id WHERE a.cfg_type = '1' AND b.data_type = ? AND a.view_id =? LIMIT ?,?";
        List<ViewAccountRoleView> list = new ArrayList<ViewAccountRoleView>();
        list=jdbcTemplate.query(sql, args1, new RowMapper() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                ViewAccountRoleView model = new ViewAccountRoleView();
                model.id = resultSet.getInt("cfg_id");
                model.role_type = resultSet.getInt("role_type");
                model.name = resultSet.getString("name");
                model.addr = resultSet.getString("addr");
                model.state = resultSet.getInt("state");
                return model;
            }
        });
        page.setList(list);
        System.out.println("这边显示"+list);
        return page;
    }

    /*
    * 为视图账号分配监控监控点
    * */
    public void setViewPoint(Integer viewId, String[] ids, String[] rights) {
        if (ids.length <= 0 || viewId == null || rights.length == 0) {
            return;
        }
        String[] sqls = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            sqls[i] = "INSERT into view_account_role  (view_id,cfg_type,cfg_id,role_type,create_date,update_date) VALUES(" + viewId + ",1," + ids[i] + "," + rights[i] + ",NOW(),NOW());";
            jdbcTemplate.update(sqls[i]);
        }
    }

    /*
    * 视图用户监控点解绑
    * @param    viewId ，   roleType      pointId
    *         视图账号ID   监控点类型    监控点ID
    *
    *  sql:DELETE FROM account_dir_rel  WHERE  ref_id=10 AND acc_dir_id IN (SELECT id FROM account_dir WHERE account_id=123 AND type=1);
    * */
    public void deletePoint(final Integer viewId, final Integer roleType, final Integer pointId) {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        try {
            tt.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus ts) {
                    String sql_deleteSqlView_Account_role = "DELETE FROM view_account_role WHERE view_id=? AND cfg_id=?";
                    String sql_delect_ref = "DELETE FROM account_dir_rel  WHERE  ref_id=? AND acc_dir_id IN (SELECT id FROM account_dir WHERE account_id=? AND type=?)";
                    Object[] args0 = new Object[]{viewId, pointId};
                    jdbcTemplate.update(sql_deleteSqlView_Account_role, args0);
                    Object[] args1 = new Object[]{pointId, viewId, roleType};
                    jdbcTemplate.update(sql_delect_ref, args1);
                    return true;
                }
            });
        } catch (Exception e) {
            Logger.getLogger(AccountImpl.class.getName()).log(Level.SEVERE, null, e);
            throw new BusinessException(ErrorCodeOption.Viewpoint_Dlete_False.key, ErrorCodeOption.Viewpoint_Dlete_False.value);
        }
    }
    /*
    * 视图账户监控点权限设置
    * @param viewId       pointId      roleType
    *       视图账户ID  监控点ID      权限：0无权限  1只读  3读写
    * UPDATE view_account_role SET role_type="1" WHERE view_id=111 AND cfg_id=12 ;
    * */
    public void updateViewPointRoleType( Integer viewId,Integer pointId, Integer roleType)
    {
        String sql="UPDATE view_account_role SET role_type=? WHERE view_id=? AND cfg_id=?";
        Object[] args=new Object[]{roleType,viewId,pointId};
        jdbcTemplate.update(sql,args);
    }
}