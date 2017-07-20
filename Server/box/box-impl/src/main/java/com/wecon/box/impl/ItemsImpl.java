package com.wecon.box.impl;

import com.wecon.box.utils.api.ItemsApi;
import com.wecon.box.utils.entity.Items;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zengzp on 2017/7/18.
 */
@Component
public class ItemsImpl implements ItemsApi {
    @Override
    public Items getItemsTop1() {
        Items it = new Items();
        it.setId(1);
        it.setName("test_name");
        return it;
    }

    @Override
    public List<Items> findAll() {
        return null;
    }
}
