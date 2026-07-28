package com.company.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.common.BusinessException;
import com.company.mapper.*;
import com.company.model.dto.CheckinAuditDTO;
import com.company.model.dto.CheckinQueryDTO;
import com.company.model.dto.SupplementDTO;
import com.company.model.entity.*;
import com.company.model.vo.CheckinVO;
import com.company.model.vo.PageVO;
import com.company.model.vo.StatsVO;
import com.company.service.SysCheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SysCheckinServiceImpl implements SysCheckinService {

    private final SysCheckinMapper checkinMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    // 上班时间 9:00，下班时间 18:00，迟到宽限 5 分钟
    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);
    private static final int LATE_GRACE_MINUTES = 5;

    @Override
    @Transactional
    public Map<String, Object> punch(Long userId, String type) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> result = new HashMap<>();

        SysCheckin record = checkinMapper.selectOne(
                new LambdaQueryWrapper<SysCheckin>().eq(SysCheckin::getUserId, userId).eq(SysCheckin::getCheckDate, today)
        );

        if ("in".equals(type)) {
            if (record != null) {
                throw new BusinessException("今天已经打过上班卡了");
            }
            record = new SysCheckin();
            record.setUserId(userId);
            record.setCheckDate(today);
            record.setCheckinTime(now);

            // 判断是否迟到
            if (now.toLocalTime().isAfter(WORK_START.plusMinutes(LATE_GRACE_MINUTES))) {
                record.setStatus("late");
            } else {
                record.setStatus("normal");
            }
            record.setAuditStatus("approved");
            checkinMapper.insert(record);
            result.put("message", "上班打卡成功");
            result.put("status", record.getStatus());
        } else if ("out".equals(type)) {
            if (record == null) {
                throw new BusinessException("请先打上班卡");
            }
            if (record.getCheckoutTime() != null) {
                throw new BusinessException("今天已经打过下班卡了");
            }
            record.setCheckoutTime(now);

            // 判断是否早退
            if (now.toLocalTime().isBefore(WORK_END)) {
                record.setStatus("early");
            }
            checkinMapper.updateById(record);
            result.put("message", "下班打卡成功");
            result.put("status", record.getStatus());
        }
        result.put("time", now);
        return result;
    }

    @Override
    public Map<String, Object> getTodayStatus(Long userId) {
        LocalDate today = LocalDate.now();
        Map<String, Object> result = new HashMap<>();

        SysCheckin record = checkinMapper.selectOne(
                new LambdaQueryWrapper<SysCheckin>().eq(SysCheckin::getUserId, userId).eq(SysCheckin::getCheckDate, today)
        );

        result.put("punchedIn", record != null && record.getCheckinTime() != null);
        result.put("punchedOut", record != null && record.getCheckoutTime() != null);
        result.put("checkinTime", record != null ? record.getCheckinTime() : null);
        result.put("checkoutTime", record != null ? record.getCheckoutTime() : null);
        result.put("status", record != null ? record.getStatus() : "absent");
        result.put("date", today);
        return result;
    }

    @Override
    public PageVO<CheckinVO> page(CheckinQueryDTO dto) {
        LambdaQueryWrapper<SysCheckin> wrapper = new LambdaQueryWrapper<SysCheckin>()
                .eq(dto.getUserId() != null, SysCheckin::getUserId, dto.getUserId())
                .like(dto.getCheckDate() != null, SysCheckin::getCheckDate, dto.getCheckDate())
                .eq(dto.getStatus() != null, SysCheckin::getStatus, dto.getStatus())
                .orderByDesc(SysCheckin::getCheckDate);

        // 按月份筛选
        if (dto.getMonth() != null) {
            wrapper.apply("DATE_FORMAT(check_date, '%Y-%m') = {0}", dto.getMonth());
        }
        // 按部门筛选
        if (dto.getDeptId() != null) {
            List<Long> userIds = userMapper.selectList(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, dto.getDeptId())
            ).stream().map(SysUser::getUserId).toList();
            if (userIds.isEmpty()) {
                return new PageVO<>(List.of(), 0, dto.getPage(), dto.getSize());
            }
            wrapper.in(SysCheckin::getUserId, userIds);
        }

        Page<SysCheckin> page = checkinMapper.selectPage(new Page<>(dto.getPage(), dto.getSize()), wrapper);
        List<CheckinVO> vos = page.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(vos, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageVO<CheckinVO> myPage(Long userId, Integer page, Integer size, String month) {
        CheckinQueryDTO dto = new CheckinQueryDTO();
        dto.setUserId(userId);
        dto.setPage(page);
        dto.setSize(size);
        dto.setMonth(month);
        return page(dto);
    }

    @Override
    @Transactional
    public void audit(Long id, CheckinAuditDTO dto) {
        SysCheckin record = checkinMapper.selectById(id);
        if (record == null) throw new BusinessException("打卡记录不存在");
        record.setAuditStatus(dto.getAuditStatus());
        record.setRemark(dto.getRemark());
        checkinMapper.updateById(record);
    }

    @Override
    @Transactional
    public void supplement(Long userId, SupplementDTO dto) {
        LocalDate date = LocalDate.parse(dto.getCheckDate());
        SysCheckin record = checkinMapper.selectOne(
                new LambdaQueryWrapper<SysCheckin>().eq(SysCheckin::getUserId, userId).eq(SysCheckin::getCheckDate, date)
        );

        if (record == null) {
            record = new SysCheckin();
            record.setUserId(userId);
            record.setCheckDate(date);
            record.setStatus("absent");
            record.setRemark("补卡申请: " + dto.getReason());
        }
        record.setAuditStatus("pending");
        record.setRemark((record.getRemark() != null ? record.getRemark() + "; " : "") + "补卡申请: " + dto.getReason());

        if (record.getId() == null) {
            checkinMapper.insert(record);
        } else {
            checkinMapper.updateById(record);
        }
    }

    @Override
    public Map<String, Object> stats(String month, Long deptId) {
        LambdaQueryWrapper<SysCheckin> wrapper = new LambdaQueryWrapper<>();
        if (month != null) {
            wrapper.apply("DATE_FORMAT(check_date, '%Y-%m') = {0}", month);
        }
        if (deptId != null) {
            List<Long> userIds = userMapper.selectList(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId)
            ).stream().map(SysUser::getUserId).toList();
            if (userIds.isEmpty()) {
                Map<String, Object> empty = new LinkedHashMap<>();
                empty.put("normal", 0L); empty.put("late", 0L);
                empty.put("early", 0L); empty.put("absent", 0L);
                return empty;
            }
            wrapper.in(SysCheckin::getUserId, userIds);
        }

        List<SysCheckin> records = checkinMapper.selectList(wrapper);
        long normal = records.stream().filter(r -> "normal".equals(r.getStatus())).count();
        long late = records.stream().filter(r -> "late".equals(r.getStatus())).count();
        long early = records.stream().filter(r -> "early".equals(r.getStatus())).count();
        long absent = records.stream().filter(r -> "absent".equals(r.getStatus())).count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("normal", normal);
        result.put("late", late);
        result.put("early", early);
        result.put("absent", absent);
        return result;
    }

    @Override
    @Transactional
    public void correct(Long id, CheckinAuditDTO dto) {
        audit(id, dto);
    }

    private CheckinVO toVO(SysCheckin record) {
        CheckinVO vo = new CheckinVO();
        BeanUtils.copyProperties(record, vo);
        SysUser user = userMapper.selectById(record.getUserId());
        if (user != null) {
            vo.setRealName(user.getRealName());
            if (user.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(user.getDeptId());
                if (dept != null) vo.setDeptName(dept.getName());
            }
        }
        return vo;
    }
}
