package com.wecon.box.impl;

import com.wecon.box.api.AppVersionApi;
import com.wecon.box.entity.AppVerInfo;
import com.wecon.common.enums.MobileOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * Created by whp on 2017/10/11.
 */
@Component
public class AppVersionImpl implements AppVersionApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void saveOrUpdate(final AppVerInfo appVerInfo){
        int updRst = jdbcTemplate.update("update app_ver_latest set version_code=?, update_date=current_timestamp() where platform=?",
                new Object[]{appVerInfo.version_code, appVerInfo.platform});
        if(0 == updRst){
            jdbcTemplate.update("insert into app_ver_latest (platform,version_code,url,create_date,update_date) values(?,?,?,current_timestamp(),current_timestamp())",
                    new Object[]{appVerInfo.platform, appVerInfo.version_code, appVerInfo.url});
        }
    }

    @Override
    public String[] getVersions(){
        String sql = "select version_code from app_ver_latest  WHERE platform=? or platform=?";
        List<AppVerInfo> list = jdbcTemplate.query(sql, new Object[]{MobileOption.ANDROID.value, MobileOption.IPHONE.value}, new RowMapper() {
            @Override
            public AppVerInfo mapRow(ResultSet resultSet, int i) throws SQLException {
                AppVerInfo appVerInfo = new AppVerInfo();
                appVerInfo.version_code = resultSet.getString("version_code");
                return appVerInfo;
            }
        });
        if (!list.isEmpty()) {
            return new String[]{list.get(0).version_code, list.get(1).version_code};
        }
        return null;
    }
}
