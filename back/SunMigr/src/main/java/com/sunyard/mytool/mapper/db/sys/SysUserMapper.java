package com.sunyard.mytool.mapper.db.sys;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.mytool.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("systemDataSource")
public interface SysUserMapper extends BaseMapper<SysUser> {
}
