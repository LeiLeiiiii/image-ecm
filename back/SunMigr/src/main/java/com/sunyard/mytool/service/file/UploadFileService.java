package com.sunyard.mytool.service.file;

import com.sunyard.mytool.entity.DocTemp;
import com.sunyard.mytool.entity.StEquipment;
import com.sunyard.mytool.entity.StFile;
import com.sunyard.mytool.entity.ecm.FileTemp;

import java.util.concurrent.Future;

public interface UploadFileService {

    /**
     * EDM上传文件
     */
    StFile uploadEDMFile(DocTemp docTemp);

    /**
     * 影像异步上传文件
     */
    Future<StFile> asyncUploadFile(StEquipment stEquipment, FileTemp fileTemp, FileStroageService fileStroageService);
}
