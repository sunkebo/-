package com.company.controller;

import com.company.annotation.RequireAdmin;
import com.company.common.Result;
import com.company.model.dto.BatchSalaryDTO;
import com.company.model.dto.CheckinAuditDTO;
import com.company.model.dto.SalaryDTO;
import com.company.model.vo.PageVO;
import com.company.model.vo.SalaryVO;
import com.company.model.vo.StatsVO;
import com.company.service.SysSalaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/salaries")
@RequiredArgsConstructor
@RequireAdmin
public class AdminSalaryController {

    private final SysSalaryService salaryService;

    @GetMapping
    public Result<PageVO<SalaryVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String salaryMonth,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String auditStatus) {
        return Result.ok(salaryService.page(page, size, salaryMonth, deptId, userId, auditStatus));
    }

    @GetMapping("/{id}")
    public Result<SalaryVO> getById(@PathVariable Long id) {
        return Result.ok(salaryService.getById(id));
    }

    @PostMapping
    public Result<String> create(@Valid @RequestBody SalaryDTO dto) {
        salaryService.create(dto);
        return Result.ok("薪资录入成功");
    }

    @PostMapping("/batch")
    public Result<String> batchCreate(@Valid @RequestBody List<SalaryDTO> list) {
        salaryService.batchCreate(list);
        return Result.ok("批量录入成功");
    }

    @PostMapping("/batch-generate")
    public Result<String> batchGenerate(@Valid @RequestBody BatchSalaryDTO dto) {
        salaryService.batchGenerate(dto);
        return Result.ok("批量生成成功");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody SalaryDTO dto) {
        salaryService.update(id, dto);
        return Result.ok("薪资更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        salaryService.delete(id);
        return Result.ok("薪资记录删除成功");
    }

    @PutMapping("/{id}/audit")
    public Result<String> audit(@PathVariable Long id, @Valid @RequestBody CheckinAuditDTO dto) {
        salaryService.audit(id, dto);
        return Result.ok("薪资审核完成");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestParam(required = false) String salaryMonth) {
        return Result.ok(salaryService.stats(salaryMonth));
    }
}
