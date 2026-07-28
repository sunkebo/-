package com.company.controller;

import com.company.annotation.RequireAdmin;
import com.company.common.Result;
import com.company.model.dto.UserDTO;
import com.company.model.vo.PageVO;
import com.company.model.vo.UserInfoVO;
import com.company.service.SysUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@RequireAdmin
public class AdminUserController {

    private final SysUserService userService;

    @GetMapping
    public Result<PageVO<UserInfoVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) String userType) {
        Page<UserInfoVO> result = userService.page(page, size, realName, deptId, postId, userType);
        return Result.ok(new PageVO<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/{id}")
    public Result<UserInfoVO> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    public Result<String> create(@Valid @RequestBody UserDTO dto) {
        userService.create(dto);
        return Result.ok("员工创建成功，默认密码: 123456");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        userService.update(id, dto);
        return Result.ok("员工信息更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok("员工删除成功");
    }

    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.ok("密码已重置为: 123456");
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.ok("状态更新成功");
    }
}
