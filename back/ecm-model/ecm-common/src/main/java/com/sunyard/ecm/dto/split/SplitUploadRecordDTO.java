package com.sunyard.ecm.dto.split;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/24 18:13
 * @desc: 文件记录DTO
 */

@Data
@ToString
@Accessors(chain = true)
public class SplitUploadRecordDTO extends SysFileApiDTO implements Serializable {
    /**
     * 已上传完的分片
     */
    private List<PartSummaryDTO> exitPartList;
}
