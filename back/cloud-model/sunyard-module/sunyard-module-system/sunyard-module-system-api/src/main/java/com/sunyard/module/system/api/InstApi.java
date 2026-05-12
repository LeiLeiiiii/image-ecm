package com.sunyard.module.system.api;

import cn.hutool.core.lang.tree.Tree;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @description: 调用授权微服务，验证连接是否授权
 * @author: raochangmei
 * @time: 2022-10-12
 */
@FeignClient(value = ApiConstants.NAME)
public interface InstApi {

    String PREFIX = ApiConstants.PREFIX + "/inst/";

    /**
     * 根据机构id 机构详情
     *
     * @param instId 机构id
     * @return Result
     */
    @PostMapping(PREFIX + "getInstByInstId")
    Result<SysInstDTO> getInstByInstId(@RequestParam(value = "instId", required = false) Long instId);

    /**
     * 获取机构集
     * @param instIds 机构id集
     * @return Result 机构集
     */
    @PostMapping(PREFIX + "getInstsByInstIds")
    Result<List<SysInstDTO>> getInstsByInstIds(@RequestParam(value = "instIds", required = false) Long[] instIds);

    /**
     * 获取机构树
     *
     * @param parentId 父级id
     * @return Result
     */
    @PostMapping(PREFIX + "searchInstTree")
    Result<List<SysOrgDTO>> searchInstTree(@RequestParam(value = "parentId", required = false) Long parentId);

    /**
     * 根据机构id获取机构及下级机构
     *
     * @param instId 机构id
     * @return Result
     */
    @PostMapping(PREFIX + "getInstListByInstId")
    Result<List<SysInstDTO>> getInstListByInstId(@RequestParam(value = "instId", required = false) Long instId);

    /**
     * 根据机构号查询部门
     *
     * @param instNo 机构号
     * @return Result
     */
    @PostMapping(PREFIX + "selectByNo")
    Result<List<SysInstDTO>> getInstByNo(@RequestParam(value = "instNo", required = false) String instNo);

    /**
     * 获取机构部门树
     * @return Result
     */
    @PostMapping(PREFIX + "getOrgTree")
    Result<List<Tree<Long>>> getOrgTree();
}
