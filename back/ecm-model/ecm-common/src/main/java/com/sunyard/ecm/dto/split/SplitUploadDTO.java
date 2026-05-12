package com.sunyard.ecm.dto.split;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/24 17:01
 * @desc: 文件上传DTO
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SplitUploadDTO implements Serializable {
    /**
     * 是否完成上传（是否已经合并分片）
     */
    private Boolean finished;

    /**
     * 文件地址
     */
    private String path;

    /**
     * 已上传的分片记录
     */
    private SplitUploadRecordDTO taskRecord;

    /**
     * 所有分片的上传地址
     */
    private List<String> urlList;
}
