package com.wecon.box.impl;

import com.wecon.box.api.DevBindUserApi;
import com.wecon.box.entity.DevBindUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/8/9.
 */
@Component
public class DevBindUserImpl implements DevBindUserApi {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public long saveDevBindUser(DevBindUser model) {
        Object[] args=new Object[]{model.account_id,model.device_id};
      String sql= "INSERT INTO dev_bind_user (account_id,device_id,create_date) VALUES(?,?,NOW())";
        return  jdbcTemplate.update(sql,args);

    }

    @Override
    public boolean updateDevBindUser(DevBindUser model) {
        return false;
    }

    @Override
    public DevBindUser getDevBindUser(long account_id, long device_id) {
        return null;
    }

    @Override
    public void delDevBindUser(long account_id, long device_id) {

    }

    @Override
    public int findByDevId(long device_id) {
        Object[] args=new Object[]{device_id};
        String sql="select count(*) from dev_bind_user where device_id=?";
        return jdbcTemplate.queryForObject(sql,args,Integer.class);

    }
}
