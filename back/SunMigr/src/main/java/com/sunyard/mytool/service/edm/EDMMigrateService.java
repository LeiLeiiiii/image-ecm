package com.sunyard.mytool.service.edm;

import com.sunyard.mytool.entity.DocTemp;

import java.util.concurrent.Future;

public interface EDMMigrateService {

    Future<String>  asyncMigrate(DocTemp docTemp);
}
