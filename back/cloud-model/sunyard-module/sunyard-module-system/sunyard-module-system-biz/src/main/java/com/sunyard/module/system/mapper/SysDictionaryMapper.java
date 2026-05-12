package com.sunyard.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.config.SysDictStyleMybatisCache;
import com.sunyard.module.system.po.SysDictionary;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * <p>
 * 字典表 Mapper 接口
 * </p>
 *
 * @author raochangmei
 * @since 2022-05-25
 */
/**
 * 用户Mapper接口：启用二级缓存
 */
@CacheNamespace(implementation = SysDictStyleMybatisCache.class)
public interface SysDictionaryMapper extends BaseMapper<SysDictionary> {

}
