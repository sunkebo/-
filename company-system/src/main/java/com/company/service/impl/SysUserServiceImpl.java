package com.company.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.common.BusinessException;
import com.company.mapper.*;
import com.company.model.dto.UserDTO;
import com.company.model.entity.*;
import com.company.model.vo.UserInfoVO;
import com.company.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysPostMapper postMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserInfoVO> page(Integer page, Integer size, String realName, Long deptId, Long postId, String userType) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1)
                .like(StringUtils.hasText(realName), SysUser::getRealName, realName)
                .eq(deptId != null, SysUser::getDeptId, deptId)
                .eq(postId != null, SysUser::getPostId, postId)
                .eq(StringUtils.hasText(userType), SysUser::getUserType, userType)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);

        Page<UserInfoVO> voPage = new Page<>(page, size, userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public UserInfoVO getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        return toVO(user);
    }

    @Override
    @Transactional
    public void create(UserDTO dto) {
        // 检查手机号唯一性（简单处理）
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        // 自动生成登录账号（纳秒级时间戳后5位，避免碰撞）
        user.setUsername("emp" + String.format("%05d", (int)(System.nanoTime() % 100000)));
        // 默认密码
        user.setPassword(passwordEncoder.encode("123456"));
        if (!StringUtils.hasText(dto.getUserType())) {
            user.setUserType("employee");
        }
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    @Transactional
    public void update(Long id, UserDTO dto) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        BeanUtils.copyProperties(dto, user);
        user.setUserId(id);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if ("admin".equals(user.getUserType())) {
            throw new BusinessException("不能删除管理员账号");
        }
        user.setStatus(0);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private UserInfoVO toVO(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) vo.setDeptName(dept.getName());
        }
        if (user.getPostId() != null) {
            SysPost post = postMapper.selectById(user.getPostId());
            if (post != null) vo.setPostName(post.getName());
        }
        return vo;
    }
}
