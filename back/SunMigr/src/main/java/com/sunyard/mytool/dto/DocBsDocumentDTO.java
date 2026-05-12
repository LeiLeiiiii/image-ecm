package com.sunyard.mytool.dto;

import com.sunyard.mytool.entity.DocBsDocument;
import com.sunyard.mytool.entity.SysUser;
import lombok.Data;

@Data
public class DocBsDocumentDTO {

    private DocBsDocument doc;
    private String userName;
    private String docUrl;
    private String sourceFileMD5;

}
