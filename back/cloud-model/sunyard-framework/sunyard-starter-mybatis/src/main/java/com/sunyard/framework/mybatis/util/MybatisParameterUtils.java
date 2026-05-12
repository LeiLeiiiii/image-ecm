package com.sunyard.framework.mybatis.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zhouleibin
 * mybatis in 1000条数目报错
 */
public class MybatisParameterUtils {

    /**
     * in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */
    public static <T, F> void cutInParameterUpdate(UpdateWrapper<T> wrapper, String column, List<F> coll) throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.in(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().in(column, objects);
            }
        });
    }


    /**
     * in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */
    public static <T, F> void cutInParameter(LambdaQueryWrapper<T> wrapper, SFunction<T, ?> column, List<F> coll)
        throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.in(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().in(column, objects);
            }
        });
    }

    /**
     * not in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */
    public static <T, F> void cutNotInParameter(LambdaQueryWrapper<T> wrapper, SFunction<T, ?> column, List<F> coll)
        throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.notIn(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().notIn(column, objects);
            }
        });
    }

    /**
     * in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */
    public static <T, F> void cutInParameter(LambdaQueryChainWrapper<T> wrapper, SFunction<T, ?> column, List<F> coll)
        throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.in(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().in(column, objects);
            }
        });
    }

    /**
     * in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */
    public static <T, F> void cutInParameter(QueryWrapper<T> wrapper, String column, List<F> coll) throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.in(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().in(column, objects);
            }
        });
    }

    /**
     * not in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */
    public static <T, F> void cutNotInParameter(QueryWrapper<T> wrapper, String column, List<F> coll) throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.notIn(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().notIn(column, objects);
            }
        });
    }

    /**
     * not in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */
    public static <T, F> void cutNotInParameter(LambdaQueryChainWrapper<T> wrapper, SFunction<T, ?> column,
        List<F> coll) throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.notIn(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().notIn(column, objects);
            }
        });
    }

    /**
     * in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */
    public static <T, F> void cutInParameter(LambdaUpdateWrapper<T> wrapper, SFunction<T, ?> column, List<F> coll)
        throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.in(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().in(column, objects);
            }
        });
    }

    /**
     * not in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */
    public static <T, F> void cutNotInParameter(LambdaUpdateWrapper<T> wrapper, SFunction<T, ?> column, List<F> coll)
        throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.notIn(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().notIn(column, objects);
            }
        });
    }

    /**
     * in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */

    public static <T, F> void cutInParameter(LambdaUpdateChainWrapper<T> wrapper, SFunction<T, ?> column, List<F> coll)
        throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.in(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().in(column, objects);
            }
        });
    }

    /**
     * not in
     *
     * @param wrapper wrapper
     * @param column column
     * @param coll coll
     * @param <T> T
     * @param <F> F
     * @throws Exception Exception
     */
    public static <T, F> void cutNotInParameter(LambdaUpdateChainWrapper<T> wrapper, SFunction<T, ?> column,
        List<F> coll) throws Exception {
        List<List<F>> newList = splitList(coll, 900);
        if (ObjectUtils.isEmpty(newList)) {
            throw new Exception("参数错误");
        } else if (newList.size() == 1) {
            wrapper.notIn(column, newList.get(0));
            return;
        }

        wrapper.and(i -> {
            i.in(column, newList.get(0));
            newList.remove(0);
            for (List<F> objects : newList) {
                i.or().notIn(column, objects);
            }
        });
    }

    /**
     * 集合 按固定数量 分割方法
     * 
     * @param list list
     * @param groupSize groupSize
     * @return Result
     * @param <F> F
     */

    public static <F> List<List<F>> splitList(List<F> list, int groupSize) {
        int length = list.size();
        // 计算可以分成多少组
        int num = (length + groupSize - 1) / groupSize;
        List<List<F>> newList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            // 开始位置
            int fromIndex = i * groupSize;
            // 结束位置
            int toIndex = Math.min((i + 1) * groupSize, length);
            newList.add(list.subList(fromIndex, toIndex));
        }
        return newList;
    }

}
