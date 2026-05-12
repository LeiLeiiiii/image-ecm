package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.AddBusiDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lw
 * @since 2023/8/24 13:45
 * @desc 资料复制VO
 */
@Data
public class BusiDocDuplicateTarVO implements Serializable {

    /**
     * 扩展信息（包含动态结构或者多维结构）
     */
    private AddBusiDTO ecmBusExtendDTO;

}
