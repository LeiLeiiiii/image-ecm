package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.AddBusiDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lw
 * @since 2023/8/24 13:45
 * @desc 资料复制VO
 */
@Data
public class BusiDocDuplicateVO implements Serializable {

    /**
     * 选中要复用的资料节点信息
     */
    private List<BusiDocDuplicateTarVO> busiDocDuplicateVos;

    /**
     * 扩展信息（包含动态结构或者多维结构）
     */
    private AddBusiDTO ecmBusExtendDTO;

}
