package com.company.controller;

import com.company.annotation.RequireAdmin;
import com.company.common.Result;
import com.company.model.dto.CheckinAuditDTO;
import com.company.model.dto.CheckinQueryDTO;
import com.company.model.vo.CheckinVO;
import com.company.model.vo.PageVO;
import com.company.model.vo.StatsVO;
import com.company.service.SysCheckinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/checkins")
@RequiredArgsConstructor
@RequireAdmin
public class AdminCheckinController {

    private final SysCheckinService checkinService;

    @GetMapping
    public Result<PageVO<CheckinVO>> list(CheckinQueryDTO dto) {
        return Result.ok(checkinService.page(dto));
    }

    @PutMapping("/{id}/audit")
    public Result<String> audit(@PathVariable Long id, @Valid @RequestBody CheckinAuditDTO dto) {
        checkinService.audit(id, dto);
        return Result.ok("审核完成");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestParam(required = false) String month,
                                        @RequestParam(required = false) Long deptId) {
        return Result.ok(checkinService.stats(month, deptId));
    }

    @PutMapping("/{id}/correct")
    public Result<String> correct(@PathVariable Long id, @Valid @RequestBody CheckinAuditDTO dto) {
        checkinService.correct(id, dto);
        return Result.ok("修正成功");
    }
}
