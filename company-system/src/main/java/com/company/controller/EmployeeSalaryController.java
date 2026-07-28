package com.company.controller;

import com.company.common.Result;
import com.company.model.vo.PageVO;
import com.company.model.vo.SalaryVO;
import com.company.service.SysSalaryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeSalaryController {

    private final SysSalaryService salaryService;

    @GetMapping("/salaries")
    public Result<PageVO<SalaryVO>> mySalaries(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String month,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(salaryService.myPage(userId, page, size, month));
    }
}
