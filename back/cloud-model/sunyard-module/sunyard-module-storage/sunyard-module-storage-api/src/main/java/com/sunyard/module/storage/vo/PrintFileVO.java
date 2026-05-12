package com.sunyard.module.storage.vo;

import lombok.Data;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author： zyl
 * @Description：
 * @create： 2023/6/26 17:03
 */
@Data
public class PrintFileVO {
    private List<Long> fileId;
    private HttpServletResponse response;
}
