package com.sunyard.mytool.service.ecm;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.sunyard.mytool.dto.EcmAppDefDto;
import com.sunyard.mytool.entity.StFile;
import com.sunyard.mytool.entity.ecm.BatchTemp;
import com.sunyard.mytool.entity.ecm.EcmFileInfo;
import com.sunyard.mytool.entity.ecm.FileTemp;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.concurrent.Future;

public interface NewEcmService {
    @DSTransactional
    void buildMainData(EcmAppDefDto ecmAppDefDto, BatchTemp batchTemp, Long busiId, List<Pair<StFile, FileTemp>> successPairs);

    Future<StFile> asyncUploadFile(EcmAppDefDto ecmAppDefDto, FileTemp fileTemp);
}
