package com.sunyard.ecm.util;

import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.po.EcmDocrightDef;

import java.util.HashMap;
import java.util.Map;

public final class RightCheckers {

    private RightCheckers() {} // 工具类，不允许实例化

    public interface RightChecker {
        boolean check(EcmDocrightDef def);
    }

    public static final Map<String, RightChecker> RIGHT_CHECKER_MAP = new HashMap<>();

    static {
        // 查看权限
        RIGHT_CHECKER_MAP.put("read", def ->
                StateConstants.YES.toString().equals(def.getReadRight()) ||
                        StateConstants.YES.toString().equals(def.getDownloadRight())
        );
        // 修改权限
        RIGHT_CHECKER_MAP.put("update", def ->
                StateConstants.YES.toString().equals(def.getUpdateRight())
        );
        // 删除权限
        RIGHT_CHECKER_MAP.put("delete", def ->
                StateConstants.YES.toString().equals(def.getDeleteRight())
        );
        // 下载权限
        RIGHT_CHECKER_MAP.put("download", def ->
                StateConstants.YES.toString().equals(def.getDownloadRight())
        );
        // 打印权限
        RIGHT_CHECKER_MAP.put("print", def ->
                StateConstants.YES.toString().equals(def.getPrintRight())
        );
        // 新增权限
        RIGHT_CHECKER_MAP.put("add", def ->
                StateConstants.YES.toString().equals(def.getAddRight())
        );
    }
}