package com.wecon.box.api;

import com.wecon.box.entity.Items;

import java.util.List;

/**
 * Created by zengzp on 2017/7/18.
 */
public interface ItemsApi {
    Items getItemsTop1();

    List<Items> findAll();
}
