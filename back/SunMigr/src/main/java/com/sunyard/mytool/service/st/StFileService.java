package com.sunyard.mytool.service.st;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.StFile;

import java.util.ArrayList;


public interface StFileService extends IService<StFile> {

    /**
     * 批量插入StFile
     * @param stFiles
     */
    void saveList(ArrayList<StFile> stFiles);
}
