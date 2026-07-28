package com.company.controller;

import com.company.annotation.RequireAdmin;
import com.company.common.Result;
import com.company.model.dto.DeptDTO;
import com.company.model.entity.SysDept;
import com.company.model.vo.DeptVO;
import com.company.service.SysDeptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/depts")
@RequiredArgsConstructor
@RequireAdmin
public class AdminDeptController {

    private final SysDeptService deptService;

    @GetMapping
    public Result<List<SysDept>> list() {
        return Result.ok(deptService.listAll());
    }

    @GetMapping("/tree")
    public Result<List<DeptVO>> tree() {
        return Result.ok(deptService.buildTree());
    }

    @GetMapping("/{id}")
    public Result<SysDept> getById(@PathVariable Long id) {
        return Result.ok(deptService.getById(id));
    }

    @PostMapping
    public Result<String> create(@Valid @RequestBody DeptDTO dto) {
        deptService.create(dto);
        return Result.ok("部门创建成功");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody DeptDTO dto) {
        deptService.update(id, dto);
        return Result.ok("部门更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.ok("部门删除成功");
    }
}
