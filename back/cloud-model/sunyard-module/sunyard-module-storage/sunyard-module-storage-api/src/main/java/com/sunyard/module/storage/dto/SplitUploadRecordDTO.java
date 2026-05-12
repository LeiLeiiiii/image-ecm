package com.sunyard.module.storage.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import software.amazon.awssdk.services.s3.model.Part;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/24 18:13
 */

@Data
@ToString
@Accessors(chain = true)
public class SplitUploadRecordDTO extends SysFileDTO implements Serializable {
    /**
     * 已上传完的分片
     */
    private List<Part> exitPartList;
}
