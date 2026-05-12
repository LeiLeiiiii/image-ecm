package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.EcmFileLabel;

import java.util.ArrayList;

public interface EcmFileLabelService extends IService<EcmFileLabel> {

    /**
     * 批量插入
     */
    void saveList(ArrayList<EcmFileLabel> ecmFileLabels);
}
