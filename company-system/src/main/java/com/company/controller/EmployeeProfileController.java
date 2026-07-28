package com.company.controller;

import com.company.common.Result;
import com.company.model.vo.UserInfoVO;
import com.company.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeProfileController {

    private final SysUserService userService;

    @GetMapping("/profile")
    public Result<UserInfoVO> profile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.getById(userId));
    }
}
