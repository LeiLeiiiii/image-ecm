package com.sunyard.module.system.api;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.PasswordUtils;
import com.sunyard.framework.common.util.UUIDUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.config.properties.SystemProperties;
import com.sunyard.module.system.constant.RoleConstants;
import com.sunyard.module.system.constant.StateConstants;
import com.sunyard.module.system.enums.table.UserStateEnum;
import com.sunyard.module.system.mapper.SysDeptMapper;
import com.sunyard.module.system.mapper.SysInstMapper;
import com.sunyard.module.system.mapper.SysRoleMapper;
import com.sunyard.module.system.mapper.SysRoleUserMapper;
import com.sunyard.module.system.mapper.SysUserMapper;
import com.sunyard.module.system.po.SysDept;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.po.SysParam;
import com.sunyard.module.system.po.SysRole;
import com.sunyard.module.system.po.SysRoleUser;
import com.sunyard.module.system.po.SysUser;
import com.sunyard.module.system.service.OrgUserService;
import com.sunyard.module.system.service.SysParamService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * user模块对内提供的controller
 *
 * @Author PJW 2023/2/15 15:35
 */
@RestController
public class UserApiImpl implements UserApi {

    @Resource
    private SystemProperties systemProperties;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private OrgUserService service;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysInstMapper sysInstMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysParamService sysParamService;

    @Override
    public Result<Object> getUserByDeptId(String code, String name, Long deptId, PageForm pageForm) {
        return Result.success(service.getUserByDeptId(code, name, deptId, pageForm));
    }

    @Override
    public Result<Object> searchUserByConditions(String code, String name, Long deptId, String postName, PageForm pageForm) {
        return Result.success(service.searchUserByConditions(code, name, deptId, postName, pageForm));
    }

    @Override
    public Result<SysUserDTO> getUserByUserId(Long userId) {
        return Result.success(service.getUserByUserId(userId));
    }

    @Override
    public Result<List<SysUserDTO>> getUserListByUserIds(Long[] userIds) {
        List<SysUser> poList = service.getUserListByUserIds(Arrays.asList(userIds));
        List<SysUserDTO> dtoList = poList.stream().map(po -> {
            SysUserDTO dto = new SysUserDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dtoList)) {
            service.setPostInfo(dtoList);
        }
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysUserDTO>> getUserListByUsernames(String[] userIds) {
        List<SysUser> poList = service.getUserListByUsernames(Arrays.asList(userIds));
        List<SysUserDTO> dtoList = poList.stream().map(po -> {
            SysUserDTO dto = new SysUserDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dtoList)) {
            service.setPostInfo(dtoList);
        }
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysUserDTO>> getTransUserByDeptId(String code, String name, Long deptId, Long instId) {
        return Result.success(service.getTransUserByDeptId(code, name, deptId, instId));
    }

    @Override
    public Result<List<SysUserDTO>> getUsersByInstId(Long instId) {
        return Result.success(service.getUsersByInstId(instId));
    }

    @Override
    public Result<List<SysUserDTO>> getUsersByInstIdList(List<Long> instId) {
        return Result.success(service.getUsersByInstIdList(instId));
    }

    @Override
    public Result<List<SysUserDTO>> getUsersByInstIdAndName(Long instId, String name) {
        return Result.success(service.getUsersByInstIdAndName(instId, name));
    }

    @Override
    public Result<SysUserDTO> getUserDetail(String userName) {
        return Result.success(service.getUserDetail(userName));
    }

    @Override
    public Result<SysUserDTO> getUserDetailById(Long userId) {
        return Result.success(service.getUserDetailById(userId));
    }

    @Override
    public Result<List<SysUserDTO>> getUserByLoginName(List<String> loginNameList) {
        List<SysUser> sysUsers = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getLoginName, loginNameList));
        List<SysUserDTO> list = PageCopyListUtils.copyListProperties(sysUsers, SysUserDTO.class);
        return Result.success(list);
    }

    @Override
    public Result<List<SysUserDTO>> getUserDetailByName(String name) {
        List<SysUserDTO> poList = service.getUserDetailByName(name);
        return Result.success(poList);
    }

    @Override
    public Result<List<SysUserDTO>> getUserByDeptIdAndRoleId(Long deptId, Long roleId) {
        List<SysUserDTO> userByDeptIdAndRoleId = service.getUserByDeptIdAndRoleId(deptId, roleId);
        return Result.success(userByDeptIdAndRoleId);
    }

    @Override
    public Result<List<SysUserDTO>> getUserList(SysRoleDTO sysRoleDTO) {
        List<SysUser> sysUsersByInst = sysUserMapper
                .selectList(new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getUserId, SysUser::getLoginName, SysUser::getInstId, SysUser::getDeptId)
                        .eq(StringUtils.hasText(sysRoleDTO.getUserName()), SysUser::getLoginName, sysRoleDTO.getUserName())
                        .eq(!CollectionUtils.isEmpty(sysRoleDTO.getOrgIds()), SysUser::getDeptId, RoleConstants.ZEROLONG)
                        .in(!CollectionUtils.isEmpty(sysRoleDTO.getOrgIds()), SysUser::getInstId, sysRoleDTO.getOrgIds()));
        List<SysUser> sysUsersByDept = sysUserMapper
                .selectList(new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getUserId, SysUser::getLoginName, SysUser::getInstId, SysUser::getDeptId)
                        .eq(StringUtils.hasText(sysRoleDTO.getUserName()), SysUser::getLoginName, sysRoleDTO.getUserName())
                        .in(!CollectionUtils.isEmpty(sysRoleDTO.getOrgIds()), SysUser::getDeptId, sysRoleDTO.getOrgIds()));
        sysUsersByInst.addAll(sysUsersByDept);
        sysUsersByInst = sysUsersByInst.stream().distinct().collect(Collectors.toList());
        List<SysUserDTO> sysUserExtends = PageCopyListUtils.copyListProperties(sysUsersByInst, SysUserDTO.class);
        if (CollectionUtils.isEmpty(sysUserExtends)) {
            return Result.success(sysUserExtends);
        }
        Set<Long> instIds = sysUsersByInst.stream()
                .filter(p -> !ObjectUtils.isEmpty(p.getInstId()))
                .map(SysUser::getInstId)
                .collect(Collectors.toSet());
        Set<Long> deptIds = sysUsersByInst.stream()
                .filter(p -> !RoleConstants.ZEROLONG.equals(p.getDeptId()))
                .map(SysUser::getDeptId).collect(Collectors.toSet());
        addOrgName(instIds, deptIds, sysUserExtends);
        if (!CollectionUtils.isEmpty(sysUserExtends)) {
            service.setPostInfo(sysUserExtends);
        }
        return Result.success(sysUserExtends);
    }

    @Override
    public Result<List<Long>> searchUserListByName(String userName) {
        List<SysUser> sysUserList = sysUserMapper
                .selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getState,1)
                        .likeRight(SysUser::getName, userName));
        if (CollectionUtils.isEmpty(sysUserList)) {
            return Result.success(Collections.emptyList());
        }
        List<Long> userIds = sysUserList.stream()
                .map(SysUser::getUserId)
                .collect(Collectors.toList());
        return Result.success(userIds);
    }

    @Override
    public Result<List<Long>> getRoleListByUser(Long userId) {
        List<SysRoleUser> roleUsers = sysRoleUserMapper
                .selectList(new LambdaQueryWrapper<SysRoleUser>()
                        .eq(SysRoleUser::getUserId, userId));
        List<Long> roleIds = roleUsers.stream()
                .map(SysRoleUser::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        return Result.success(roleIds);
    }

    @Override
    public Result<List<Long>> getRoleListByUsername(String userId) {
        List<SysUser> sysUsers = sysUserMapper
                .selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getLoginName, userId));
        if (!CollectionUtils.isEmpty(sysUsers)) {
            return getRoleListByUser(sysUsers.get(0).getUserId());
        }
        return Result.error(ResultCode.PARAM_ERROR);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SysUserDTO> registerUserInfo(SysUserDTO sysUserDTO) {
        //入参校验
        checkUserParam(sysUserDTO);
        List<SysUser> userlist = sysUserMapper
                .selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getLoginName, sysUserDTO.getLoginName()));
        SysUserDTO resultSysUserDTO = new SysUserDTO();
        if (CollectionUtils.isEmpty(userlist)) {
            //用户不存在，注册用户
            SysUser sysUser = new SysUser();
            sysUser.setInstId(0L);
            sysUser.setDeptId(0L);
            sysUser.setLoginName(sysUserDTO.getLoginName());
            sysUser.setName(sysUserDTO.getName());
            String salt = UUIDUtils.generateUUID();
            sysUser.setPwd(PasswordUtils.getEncryptionPassword(salt, systemProperties.getUserInitPassword()));
            sysUser.setSalt(salt);
            sysUser.setState(UserStateEnum.ENABLED.getCode());
            sysUser.setType(StateConstants.ZERO);
            sysUserMapper.insert(sysUser);
            //根据角色代码查找角色id
            // todo 由于于FIND_IN_SET适配问题，先改为先查后代码筛选
            List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                    .in(SysRole::getRoleCode, sysUserDTO.getRoleCode())
            );
            if (!CollectionUtils.isEmpty(sysRoles)) {
                // 筛选
                sysRoles = sysRoles.stream()
                        .filter(role ->
                                role.getSystemCode() != null &&
                                        Arrays.asList(role.getSystemCode().split(","))
                                                .contains(StateConstants.COMMON_ONE)
                        )
                        .collect(Collectors.toList());
            }
            if (!CollectionUtils.isEmpty(sysRoles)) {
                //用户关联角色
                List<SysRoleUser> sysRoleUserList=new ArrayList<>();
                for (SysRole role : sysRoles) {
                    SysRoleUser sysRoleUser = new SysRoleUser();
                    sysRoleUser.setUserId(sysUser.getUserId());
                    sysRoleUser.setRoleId(role.getRoleId());
                    sysRoleUserList.add(sysRoleUser);
                }
                MybatisBatch<SysRoleUser> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, sysRoleUserList);
                MybatisBatch.Method<SysRoleUser> method = new MybatisBatch.Method<>(SysRoleUserMapper.class);
                mybatisBatch.execute(method.insert());
            }

            BeanUtils.copyProperties(sysUser, resultSysUserDTO);
            return Result.success(sysUserDTO);
        } else {
            //用户存在
            BeanUtils.copyProperties(userlist.get(0), resultSysUserDTO);
            return Result.success(resultSysUserDTO);
        }
    }

    @Override
    public Result<List<SysUserDTO>> getAllUserInfo() {
        List<SysUserDTO> allUserInfo = sysUserMapper.getAllUserInfo();
        if (!CollectionUtils.isEmpty(allUserInfo)) {
            service.setPostInfo(allUserInfo);
        }
        return Result.success(allUserInfo);
    }

    @Override
    public Result<Integer> deleteUserByUserIds(List<Long> userIds) {
        AssertUtils.isNull(userIds, "参数错误");
        int i = sysUserMapper.delete(new LambdaQueryWrapper<SysUser>().in(SysUser::getUserId, userIds));
        return Result.success(i);
    }

    @Override
    public Result<Integer> updateSysUserDTO(SysUserDTO sysUserDTO) {
        AssertUtils.isNull(sysUserDTO.getUserId(), "参数错误");
        AssertUtils.isNull(sysUserDTO.getLoginName(), "登陆名不能为空");
        AssertUtils.isNull(sysUserDTO.getName(), "用户名称不能为空");
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserDTO, sysUser);
        int i = sysUserMapper.updateById(sysUser);
        return Result.success(i);
    }

    @Override
    public Result<Integer> addSysUserDTO(SysUserDTO sysUserDTO) {
        AssertUtils.isNull(sysUserDTO.getUserId(), "参数错误");
        AssertUtils.isNull(sysUserDTO.getLoginName(), "登陆名不能为空");
        AssertUtils.isNull(sysUserDTO.getName(), "用户名称不能为空");
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserDTO, sysUser);
        int i = sysUserMapper.insert(sysUser);
        return Result.success(i);
    }

    @Override
    public List<SysUserDTO> queryInstCodeByUsername(List<String> userIds) {
        List<SysUser> sysUsers = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().in(!CollectionUtils.isEmpty(userIds),SysUser::getLoginName, userIds));
        if(CollectionUtils.isEmpty(sysUsers)){
            return null;
        }
        List<Long> inst = sysUsers.stream().map(SysUser::getInstId).collect(Collectors.toList());
        List<SysInst> sysDepts = sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>().in(SysInst::getInstId, inst));
        Map<Long, List<SysInst>> collect = sysDepts.stream().collect(Collectors.groupingBy(SysInst::getInstId));
        List<SysUserDTO> sysUserDTOS = new ArrayList<>();
        for(SysUser user: sysUsers){
            SysUserDTO sysUserDTO = new SysUserDTO();
            BeanUtils.copyProperties(user,sysUserDTO);
            List<SysInst> sysInsts = collect.get(user.getInstId());
            if(!CollectionUtils.isEmpty(sysInsts)){
                SysInst sysDept = sysInsts.get(0);
                sysUserDTO.setInstNo(sysDept.getInstNo());
            }
            sysUserDTOS.add(sysUserDTO);

        }
        if (!CollectionUtils.isEmpty(sysUserDTOS)) {
            service.setPostInfo(sysUserDTOS);
        }
        return sysUserDTOS;
    }

    @Override
    public Result<SysUserDTO> checkUser(SysUserDTO sysUserDTO) {
        AssertUtils.isNull(sysUserDTO.getInstNo(), "机构号不能为空");
        AssertUtils.isNull(sysUserDTO.getLoginName(), "用户不能为空");
        AssertUtils.isNull(sysUserDTO.getRoleCodeList(), "用户角色code不能为空");

        List<SysInst> sysInsts = sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>().eq(SysInst::getNewlevel,StateConstants.ZERO).eq(SysInst::getInstNo, sysUserDTO.getInstNo()));
        AssertUtils.isNull(sysInsts,"机构号校验失败，当前机构号不存在");
        SysInst sysInst = sysInsts.get(0);
        SysUser sysUser=sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getLoginName,sysUserDTO.getLoginName())
        );
        AssertUtils.isNull(sysUser, "用户校验失败，当前用户号不存在");

        List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleCode, sysUserDTO.getRoleCodeList())
        );
        AssertUtils.isNull(sysRoles, "角色校验失败，当前角色号不存在。");
        List<Long> roleId = sysRoles.stream().map(SysRole::getRoleId).collect(Collectors.toList());
        List<String> roleCode = sysRoles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        SysUserDTO sysUserDTO1 = new SysUserDTO();
        sysUserDTO1.setRoleIdList(roleId);
        sysUserDTO1.setRoleCodeList(roleCode);
        sysUserDTO1.setLoginName(sysUser.getLoginName());
        sysUserDTO1.setName(sysUser.getName());
        sysUserDTO1.setInstNo(sysInst.getInstNo());
        sysUserDTO1.setInstName(sysInst.getName());
        sysUserDTO1.setInstId(sysInst.getInstId());
        sysUserDTO1.setEmail(sysUser.getEmail());
        sysUserDTO1.setUserId(sysUser.getUserId());
        List<SysUserDTO> list = Collections.singletonList(sysUserDTO1);
        if (!CollectionUtils.isEmpty(list)) {
            service.setPostInfo(list);
        }
        return Result.success(list.get(0));
    }

    @Override
    public Result<SysUserDTO> checkUserSpecial(SysUserDTO sysUserDTO) {
        //角色关联特殊配置机构，不取传来的机构
        SysParam po = sysParamService.searchValueByKey("OPEN_API_INST_NUMBER");
        if(po==null){
            return checkUser(sysUserDTO);
        }else{
            AssertUtils.isNull(sysUserDTO.getInstNo(), "机构号不能为空");
            AssertUtils.isNull(sysUserDTO.getLoginName(), "用户不能为空");
            AssertUtils.isNull(sysUserDTO.getRoleCodeList(), "用户角色code不能为空");

            List<SysInst> sysInsts = sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>().eq(SysInst::getNewlevel,StateConstants.ZERO).eq(SysInst::getInstNo, sysUserDTO.getInstNo()));
            AssertUtils.isNull(sysInsts,"机构号校验失败，当前机构号不存在");
            SysInst sysInst = sysInsts.get(0);
            SysUser sysUser=sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getLoginName,sysUserDTO.getLoginName())
            );
            AssertUtils.isNull(sysUser, "用户校验失败，当前用户号不存在");
            SysInst specialInst = sysInstMapper.selectOne(new LambdaQueryWrapper<SysInst>().eq(SysInst::getInstNo, po.getValue()));
            AssertUtils.isNull(specialInst,"特殊机构号校验失败，当前机构号不存在:"+po.getValue());
            Long instId=specialInst.getInstId();
            List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                    .in(SysRole::getRoleCode, sysUserDTO.getRoleCodeList())
            );
            AssertUtils.isNull(sysRoles, "角色校验失败，当前角色号不存在。");
            List<Long> roleId = sysRoles.stream().map(SysRole::getRoleId).collect(Collectors.toList());
            List<String> roleCode = sysRoles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
            SysUserDTO sysUserDTO1 = new SysUserDTO();
            sysUserDTO1.setRoleIdList(roleId);
            sysUserDTO1.setRoleCodeList(roleCode);
            sysUserDTO1.setLoginName(sysUser.getLoginName());
            sysUserDTO1.setName(sysUser.getName());
            sysUserDTO1.setInstNo(sysInst.getInstNo());
            sysUserDTO1.setInstName(sysInst.getName());
            sysUserDTO1.setInstId(sysInst.getInstId());
            sysUserDTO1.setEmail(sysUser.getEmail());
            sysUserDTO1.setUserId(sysUser.getUserId());
            List<SysUserDTO> list = Collections.singletonList(sysUserDTO1);
            if (!CollectionUtils.isEmpty(list)) {
                service.setPostInfo(list);
            }
            return Result.success(list.get(0));
        }

    }

    private void checkUserParam(SysUserDTO sysUserDTO) {
        AssertUtils.isNull(sysUserDTO.getLoginName(), "登陆名不能为空");
    }

    private void addOrgName(Set<Long> instIds, Set<Long> deptIds, List<SysUserDTO> sysUserExtends) {
        if (CollectionUtils.isEmpty(sysUserExtends)) {
            return;
        }
        List<SysInst> insts = sysInstMapper
                .selectList(new LambdaQueryWrapper<SysInst>()
                        .in(!CollectionUtils.isEmpty(instIds), SysInst::getInstId, instIds));
        List<SysDept> depts = sysDeptMapper
                .selectList(new LambdaQueryWrapper<SysDept>()
                        .in(!CollectionUtils.isEmpty(deptIds), SysDept::getDeptId, deptIds));
        Map<Long, List<SysInst>> collectByInstId = insts.stream()
                .collect(Collectors.groupingBy(SysInst::getInstId));
        Map<Long, List<SysDept>> collectByDeptId = depts.stream()
                .collect(Collectors.groupingBy(SysDept::getDeptId));
        for (SysUserDTO userExtend : sysUserExtends) {
            String orgName = "";
            if (!CollectionUtils.isEmpty(collectByInstId.get(userExtend.getInstId()))) {
                orgName = collectByInstId.get(userExtend.getInstId()).get(0).getNameLevel();
            }
            if (!RoleConstants.ZEROLONG.equals(userExtend.getDeptId())) {
                if (!CollectionUtils.isEmpty(collectByDeptId.get(userExtend.getDeptId()))) {
                    orgName = orgName + "-" + collectByDeptId.get(userExtend.getDeptId()).get(0).getNameLevel();
                }
            }
            userExtend.setOrganization(orgName.replace("-", "/"));
        }
    }

}
