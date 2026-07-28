package com.company.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.common.BusinessException;
import com.company.mapper.*;
import com.company.model.dto.BatchSalaryDTO;
import com.company.model.dto.CheckinAuditDTO;
import com.company.model.dto.SalaryDTO;
import com.company.model.entity.*;
import com.company.model.vo.PageVO;
import com.company.model.vo.SalaryVO;
import com.company.service.SysSalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysSalaryServiceImpl implements SysSalaryService {

    private final SysSalaryMapper salaryMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysPostMapper postMapper;

    @Override
    public PageVO<SalaryVO> page(Integer page, Integer size, String salaryMonth, Long deptId, Long userId, String auditStatus) {
        LambdaQueryWrapper<SysSalary> wrapper = new LambdaQueryWrapper<SysSalary>()
                .eq(salaryMonth != null, SysSalary::getSalaryMonth, salaryMonth)
                .eq(userId != null, SysSalary::getUserId, userId)
                .eq(auditStatus != null, SysSalary::getAuditStatus, auditStatus)
                .orderByDesc(SysSalary::getSalaryMonth)
                .orderByAsc(SysSalary::getSalaryId);

        if (deptId != null) {
            List<Long> userIds = userMapper.selectList(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId)
            ).stream().map(SysUser::getUserId).toList();
            if (userIds.isEmpty()) {
                return new PageVO<>(List.of(), 0, page, size);
            }
            wrapper.in(SysSalary::getUserId, userIds);
        }

        Page<SysSalary> salaryPage = salaryMapper.selectPage(new Page<>(page, size), wrapper);
        List<SalaryVO> vos = salaryPage.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(vos, salaryPage.getTotal(), salaryPage.getCurrent(), salaryPage.getSize());
    }

    @Override
    public SalaryVO getById(Long id) {
        SysSalary s = salaryMapper.selectById(id);
        if (s == null) throw new BusinessException("薪资记录不存在");
        return toVO(s);
    }

    @Override
    @Transactional
    public void create(SalaryDTO dto) {
        // 检查是否已有该月薪资
        Long exists = salaryMapper.selectCount(
                new LambdaQueryWrapper<SysSalary>()
                        .eq(SysSalary::getUserId, dto.getUserId())
                        .eq(SysSalary::getSalaryMonth, dto.getSalaryMonth())
        );
        if (exists > 0) {
            throw new BusinessException("该用户本月薪资已录入");
        }

        SysSalary salary = new SysSalary();
        BeanUtils.copyProperties(dto, salary);
        salary.setBaseSalary(nvl(dto.getBaseSalary()));
        salary.setPerformance(nvl(dto.getPerformance()));
        salary.setBonus(nvl(dto.getBonus()));
        salary.setDeduction(nvl(dto.getDeduction()));
        salary.setActualSalary(salary.getBaseSalary()
                .add(salary.getPerformance())
                .add(salary.getBonus())
                .subtract(salary.getDeduction()));
        salary.setAuditStatus("pending");
        salaryMapper.insert(salary);
    }

    @Override
    @Transactional
    public void batchCreate(List<SalaryDTO> list) {
        for (SalaryDTO dto : list) {
            create(dto);
        }
    }

    @Override
    @Transactional
    public void batchGenerate(BatchSalaryDTO dto) {
        // 查询所有在职员工
        List<SysUser> employees = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 1)
                        .eq(SysUser::getUserType, "employee")
        );
        if (employees.isEmpty()) {
            throw new BusinessException("没有在职员工可生成薪资");
        }

        BigDecimal base = nvl(dto.getBaseSalary());
        BigDecimal perf = nvl(dto.getPerformance());
        BigDecimal bonus = nvl(dto.getBonus());
        BigDecimal deduct = nvl(dto.getDeduction());
        BigDecimal actual = base.add(perf).add(bonus).subtract(deduct);

        int created = 0;
        for (SysUser emp : employees) {
            // 检查是否已有该月薪资
            Long exists = salaryMapper.selectCount(
                    new LambdaQueryWrapper<SysSalary>()
                            .eq(SysSalary::getUserId, emp.getUserId())
                            .eq(SysSalary::getSalaryMonth, dto.getSalaryMonth())
            );
            if (exists > 0) continue;

            SysSalary salary = new SysSalary();
            salary.setUserId(emp.getUserId());
            salary.setSalaryMonth(dto.getSalaryMonth());
            salary.setBaseSalary(base);
            salary.setPerformance(perf);
            salary.setBonus(bonus);
            salary.setDeduction(deduct);
            salary.setActualSalary(actual);
            salary.setAuditStatus("pending");
            salary.setRemark(dto.getRemark());
            salaryMapper.insert(salary);
            created++;
        }
        if (created == 0) {
            throw new BusinessException("所有员工本月薪资已存在，无需重复生成");
        }
    }

    @Override
    @Transactional
    public void update(Long id, SalaryDTO dto) {
        SysSalary salary = salaryMapper.selectById(id);
        if (salary == null) throw new BusinessException("薪资记录不存在");
        BeanUtils.copyProperties(dto, salary);
        salary.setSalaryId(id);
        salary.setActualSalary(nvl(salary.getBaseSalary())
                .add(nvl(salary.getPerformance()))
                .add(nvl(salary.getBonus()))
                .subtract(nvl(salary.getDeduction())));
        salaryMapper.updateById(salary);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        salaryMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void audit(Long id, CheckinAuditDTO dto) {
        SysSalary salary = salaryMapper.selectById(id);
        if (salary == null) throw new BusinessException("薪资记录不存在");
        salary.setAuditStatus(dto.getAuditStatus());
        salary.setRemark(dto.getRemark());
        salaryMapper.updateById(salary);
    }

    @Override
    public PageVO<SalaryVO> myPage(Long userId, Integer page, Integer size, String month) {
        LambdaQueryWrapper<SysSalary> wrapper = new LambdaQueryWrapper<SysSalary>()
                .eq(SysSalary::getUserId, userId)
                .eq(SysSalary::getAuditStatus, "approved")
                .eq(month != null, SysSalary::getSalaryMonth, month)
                .orderByDesc(SysSalary::getSalaryMonth);

        Page<SysSalary> salaryPage = salaryMapper.selectPage(new Page<>(page, size), wrapper);
        List<SalaryVO> vos = salaryPage.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(vos, salaryPage.getTotal(), salaryPage.getCurrent(), salaryPage.getSize());
    }

    @Override
    public Map<String, Object> stats(String salaryMonth) {
        LambdaQueryWrapper<SysSalary> wrapper = new LambdaQueryWrapper<>();
        if (salaryMonth != null) {
            wrapper.eq(SysSalary::getSalaryMonth, salaryMonth);
        }
        List<SysSalary> records = salaryMapper.selectList(wrapper);

        BigDecimal total = records.stream().map(SysSalary::getActualSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
        long count = records.size();
        BigDecimal avg = count > 0 ? total.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSalary", total);
        result.put("count", count);
        result.put("avgSalary", avg);
        return result;
    }

    private SalaryVO toVO(SysSalary s) {
        SalaryVO vo = new SalaryVO();
        BeanUtils.copyProperties(s, vo);
        SysUser user = userMapper.selectById(s.getUserId());
        if (user != null) {
            vo.setRealName(user.getRealName());
            if (user.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(user.getDeptId());
                if (dept != null) vo.setDeptName(dept.getName());
            }
            if (user.getPostId() != null) {
                SysPost post = postMapper.selectById(user.getPostId());
                if (post != null) vo.setPostName(post.getName());
            }
        }
        return vo;
    }

    private BigDecimal nvl(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
