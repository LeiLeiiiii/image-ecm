package com.sunyard.module.system.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SysPostUserDTO
 * @Author wml
 * @Date 2025/9/5 11:27
 **/
@Data
public class SysPostUserDTO {

    private List<Long> userIds;

    private List<Long> postIds;

}
