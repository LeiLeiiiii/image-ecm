package com.sunyard.ecm.util;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * pagehelper 手动分页
 *
 * @author zyl
 */
public class PageInfoUtils {

    /**
     * @param currentPage 当前页数
     * @param pageSize    每页条数
     * @param list        要执行分页的数组
     * @param <T>
     * @return
     */
    public static <T> PageInfo<T> getPageInfo(int currentPage, int pageSize, List<T> list) {
        int total = list.size();
        if (total > pageSize) {
            int toIndex = pageSize * currentPage;
            if (toIndex > total) {
                toIndex = total;
            }
            int totalPage = total % pageSize == 0 ? (total / pageSize) : (total / pageSize) + 1;
            if (totalPage < currentPage) {
                list = new ArrayList<>();
            } else {
                list = list.subList(pageSize * (currentPage - 1), toIndex);
            }
        } else {
            list = currentPage == 1 ? list : new ArrayList<>();
        }
        Page<T> page = new Page<>(currentPage, pageSize);
        page.addAll(list);
        page.setPages((total + pageSize - 1) / pageSize);
        page.setTotal(total);

        return new PageInfo<>(page);
    }

}
