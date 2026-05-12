package com.sunyard.framework.mybatis.util;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author zhouleibin
 * 复制类
 */
public class PageCopyListUtils {

    /**
     * 列表对象拷贝
     *
     * @param sources 源列表
     * @param clazz 目标列表对象Class
     * @param <T> 目标列表对象类型
     * @param <M> 源列表对象类型
     * @return Result 目标列表
     */
    public static <T, M> List<T> copyListProperties(List<M> sources, Class<T> clazz) {
        if (CollectionUtils.isEmpty(sources)) {
            return new ArrayList<>();
        } else {
            if (Objects.isNull(sources) || Objects.isNull(clazz) || sources.isEmpty()) {
                throw new IllegalArgumentException();
            }
            List<T> targets = new ArrayList<>(sources.size());
            try {
                for (M source : sources) {
                    T t = clazz.newInstance();
                    BeanUtils.copyProperties(source, t);
                    targets.add(t);
                }
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return targets;
        }
    }

    /**
     * page转换
     *
     * @param page page
     * @param <T> T
     * @param <M> F
     * @return Result
     */
    public static <T, M> PageInfo<T> getPageInfo(PageInfo<M> page, Class<T> clazz) {
        PageInfo<T> pageInfo = new PageInfo<T>();
        BeanUtils.copyProperties(page, pageInfo);
        List<T> ts = copyListProperties(page.getList(), clazz);
        pageInfo.setList(ts);
        return pageInfo;
    }

}
