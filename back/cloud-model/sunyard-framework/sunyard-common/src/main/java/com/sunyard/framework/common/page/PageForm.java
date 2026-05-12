package com.sunyard.framework.common.page;
/*
 * Project: com.sunyard.am.page
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.io.Serializable;

/**
 * @author zhouleibin
 * @date 2021/6/30 8:28
 */
public class PageForm implements Serializable {

    /**
     * 当前页
     */
    private Integer pageNum = 1;
    /**
     * 分页数量
     */
    private Integer pageSize = 20;

    public PageForm() {}

    public PageForm(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        if (null == this.pageNum) {
            return 1;
        } else {
            return pageNum;
        }
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
