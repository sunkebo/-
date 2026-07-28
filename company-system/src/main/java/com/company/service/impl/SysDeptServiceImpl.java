package com.company.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.common.BusinessException;
import com.company.mapper.SysDeptMapper;
import com.company.mapper.SysUserMapper;
import com.company.model.dto.DeptDTO;
import com.company.model.entity.SysDept;
import com.company.model.entity.SysUser;
import com.company.model.vo.DeptVO;
import com.company.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements SysDeptService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    @Override
    public List<SysDept> listAll() {
        return deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getStatus, 1)
                        .orderByAsc(SysDept::getSortOrder)
        );
    }

    @Override
    public List<DeptVO> buildTree() {
        List<SysDept> all = listAll();
        Map<Long, List<SysDept>> grouped = all.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId));

        return buildChildren(0L, grouped);
    }

    private List<DeptVO> buildChildren(Long parentId, Map<Long, List<SysDept>> grouped) {
        List<SysDept> children = grouped.getOrDefault(parentId, new ArrayList<>());
        if (children.isEmpty()) return new ArrayList<>();

        return children.stream()
                .sorted(Comparator.comparingInt(SysDept::getSortOrder))
                .map(dept -> {
                    DeptVO vo = new DeptVO();
                    BeanUtils.copyProperties(dept, vo);
                    vo.setChildren(buildChildren(dept.getDeptId(), grouped));
                    return vo;
                }).collect(Collectors.toList());
    }

    @Override
    public SysDept getById(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) throw new BusinessException("部门不存在");
        return dept;
    }

    @Override
    @Transactional
    public void create(DeptDTO dto) {
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        if (dto.getParentId() == null) dept.setParentId(0L);
        dept.setStatus(1);
        deptMapper.insert(dept);
    }

    @Override
    @Transactional
    public void update(Long id, DeptDTO dto) {
        SysDept dept = getById(id);
        BeanUtils.copyProperties(dto, dept);
        dept.setDeptId(id);
        deptMapper.updateById(dept);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 检查是否有子部门
        Long childCount = deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id).eq(SysDept::getStatus, 1)
        );
        if (childCount > 0) {
            throw new BusinessException("该部门下存在子部门，无法删除");
        }
        // 检查是否有员工
        Long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, id).eq(SysUser::getStatus, 1)
        );
        if (userCount > 0) {
            throw new BusinessException("该部门下存在员工，无法删除");
        }
        deptMapper.deleteById(id);
    }
}
