package com.sunyard.mytool.service.ecm;
import com.sunyard.mytool.entity.ecm.BatchTemp;

import java.util.concurrent.Future;

public interface ECMMigrateService {

    Future<String>  asyncMigrate(BatchTemp batchTemp);
}
