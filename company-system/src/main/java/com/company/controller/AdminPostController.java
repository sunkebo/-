package com.company.controller;

import com.company.annotation.RequireAdmin;
import com.company.common.Result;
import com.company.model.dto.PostDTO;
import com.company.model.entity.SysPost;
import com.company.model.vo.PageVO;
import com.company.service.SysPostService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@RequireAdmin
public class AdminPostController {

    private final SysPostService postService;

    @GetMapping
    public Result<PageVO<SysPost>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long deptId) {
        Page<SysPost> result = postService.page(page, size, deptId);
        return Result.ok(new PageVO<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/{id}")
    public Result<SysPost> getById(@PathVariable Long id) {
        return Result.ok(postService.getById(id));
    }

    @PostMapping
    public Result<String> create(@Valid @RequestBody PostDTO dto) {
        postService.create(dto);
        return Result.ok("岗位创建成功");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody PostDTO dto) {
        postService.update(id, dto);
        return Result.ok("岗位更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        postService.delete(id);
        return Result.ok("岗位删除成功");
    }
}
