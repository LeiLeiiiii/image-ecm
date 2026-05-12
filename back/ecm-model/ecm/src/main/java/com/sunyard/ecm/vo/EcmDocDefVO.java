package com.sunyard.ecm.vo;

import com.sunyard.ecm.po.EcmDocDef;
import lombok.Data;

import java.util.List;

/**
 * @author yzy
 * @desc
 * @since 2026/4/30
 */
@Data
public class EcmDocDefVO extends EcmDocDef {
    private List<EcmDocDefVO> children;
}
