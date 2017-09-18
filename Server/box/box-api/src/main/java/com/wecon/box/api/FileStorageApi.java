package com.wecon.box.api;

import com.wecon.box.entity.FileStorage;
import org.springframework.stereotype.Component;

/**
 * Created by zengzhipeng on 2017/9/13.
 */
@Component
public interface FileStorageApi {

    /**
     * 新增文件
     *
     * @param model
     * @return
     */
    long addFileStorage(FileStorage model);

    /**
     * 获取文件详情
     *
     * @param file_id
     * @return
     */
    FileStorage getFileStorage(long file_id);
}
