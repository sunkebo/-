package com.company.controller;

import com.company.common.Result;
import com.company.model.dto.CheckinQueryDTO;
import com.company.model.dto.PunchDTO;
import com.company.model.dto.SupplementDTO;
import com.company.model.vo.CheckinVO;
import com.company.model.vo.PageVO;
import com.company.service.SysCheckinService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeCheckinController {

    private final SysCheckinService checkinService;

    @PostMapping("/checkin/punch")
    public Result<Map<String, Object>> punch(@RequestBody PunchDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(checkinService.punch(userId, dto.getType()));
    }

    @GetMapping("/checkin/today")
    public Result<Map<String, Object>> today(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(checkinService.getTodayStatus(userId));
    }

    @GetMapping("/checkins")
    public Result<PageVO<CheckinVO>> myCheckins(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String month,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(checkinService.myPage(userId, page, size, month));
    }

    @PostMapping("/checkins/supplement")
    public Result<String> supplement(@Valid @RequestBody SupplementDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        checkinService.supplement(userId, dto);
        return Result.ok("补卡申请已提交，请等待审核");
    }
}
