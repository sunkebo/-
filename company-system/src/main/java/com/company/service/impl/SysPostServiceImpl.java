package com.company.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.common.BusinessException;
import com.company.mapper.SysPostMapper;
import com.company.mapper.SysUserMapper;
import com.company.model.dto.PostDTO;
import com.company.model.entity.SysPost;
import com.company.model.entity.SysUser;
import com.company.service.SysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysPostServiceImpl implements SysPostService {

    private final SysPostMapper postMapper;
    private final SysUserMapper userMapper;

    @Override
    public Page<SysPost> page(Integer page, Integer size, Long deptId) {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getStatus, 1)
                .eq(deptId != null, SysPost::getDeptId, deptId)
                .orderByAsc(SysPost::getSortOrder);
        return postMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public SysPost getById(Long id) {
        SysPost post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("岗位不存在");
        return post;
    }

    @Override
    @Transactional
    public void create(PostDTO dto) {
        SysPost post = new SysPost();
        BeanUtils.copyProperties(dto, post);
        post.setStatus(1);
        postMapper.insert(post);
    }

    @Override
    @Transactional
    public void update(Long id, PostDTO dto) {
        SysPost post = getById(id);
        BeanUtils.copyProperties(dto, post);
        post.setPostId(id);
        postMapper.updateById(post);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPostId, id).eq(SysUser::getStatus, 1)
        );
        if (userCount > 0) {
            throw new BusinessException("该岗位下存在员工，无法删除");
        }
        postMapper.deleteById(id);
    }
}
