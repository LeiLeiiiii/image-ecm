package com.sunyard.edm.controller;

import com.sunyard.edm.constant.DocElasticsearchQueryTypeConstants;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.service.CenterQueryService;
import com.sunyard.edm.vo.DocBsDocumentSearchVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author huronghao
 * @Type
 * @Desc 首页-文档检索
 * @DATE 2022-12-12 15:34
 */
@RestController
@RequestMapping("center/query")
public class CenterQueryController extends BaseController {

    @Resource
    private CenterQueryService centerQueryService;

    /**
     * 全文检索。
     */
    @PostMapping("search")
    @OperationLog(DocLogsConstants.HOME + "文档检索")
    public Result search(DocElasticsearchQueryTypeConstants type, DocBsDocumentSearchVO docVo, PageForm pageForm) {

        return centerQueryService.search(type, docVo, getToken(), pageForm);
    }

}
