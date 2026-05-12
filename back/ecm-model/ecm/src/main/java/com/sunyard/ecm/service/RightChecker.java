package com.sunyard.ecm.service;

import com.sunyard.ecm.po.EcmDocrightDef;

/**
 * @author yzy
 * @desc
 * @since 2025/12/30
 */
@FunctionalInterface
public interface RightChecker {
    boolean check(EcmDocrightDef def);
}
