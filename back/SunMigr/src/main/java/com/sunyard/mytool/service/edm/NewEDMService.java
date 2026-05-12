package com.sunyard.mytool.service.edm;

import com.sunyard.mytool.dto.DocBsDocumentDTO;
import com.sunyard.mytool.entity.DocBsDocument;
import com.sunyard.mytool.entity.DocBsTagDocument;
import com.sunyard.mytool.entity.StFile;
import java.util.List;

public interface NewEDMService {
    void buildMainData(List<DocBsDocument> folderList, DocBsDocumentDTO fileDoc, StFile stFile, DocBsTagDocument docBsTagDocument);
}
