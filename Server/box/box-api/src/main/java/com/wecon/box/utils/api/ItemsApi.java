package com.wecon.box.utils.api;

import com.wecon.box.utils.entity.Items;

import java.util.List;

/**
 * Created by zengzp on 2017/7/18.
 */
public interface ItemsApi {
    Items getItemsTop1();

    List<Items> findAll();
}
