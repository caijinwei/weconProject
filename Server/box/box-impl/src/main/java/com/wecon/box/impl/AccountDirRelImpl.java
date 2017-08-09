package com.wecon.box.impl;

import com.wecon.box.api.AccountDirRelApi;
import com.wecon.box.entity.AccountDirRel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by caijinw on 2017/8/9.
 */
@Component
public class AccountDirRelImpl implements AccountDirRelApi
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public long saveAccountDirRel(AccountDirRel model) {
        String sql="INSERT INTO account_dir_rel (acc_dir_id,ref_id,create_date) VALUES(?,?,NOW())";
        Object[] args=new Object[]{model.acc_dir_id,model.ref_id};
        return jdbcTemplate.update(sql,args);
    }

    @Override
    public boolean updateAccountDirRel(AccountDirRel model) {
        return false;
    }

    @Override
    public AccountDirRel getAccountDirRel(long acc_dir_id, long ref_id) {
        return null;
    }

    @Override
    public void delAccountDir(long acc_dir_id, long ref_id) {
    }

    @Override
    public int findRecordingByAccDirid(long acc_dir_id)
    {
        Object[] args=new Object[]{acc_dir_id};
        String sql="SELECT COUNT(*) FROM account_dir_rel WHERE acc_dir_id=?";
        return jdbcTemplate.queryForObject(sql,args,Integer.class);

    }


}
