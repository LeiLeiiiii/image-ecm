package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

/**
 * @Author 朱山成
 * @time 2023/5/31 10:03
 **/
@Data
public class EcmFileRequestBody {
    private EcmRoot ecmRoot;
    private byte[] uploadFilebytes;
}
