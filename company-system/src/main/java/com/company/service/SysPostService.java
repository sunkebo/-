package com.company.service;

import com.company.model.dto.PostDTO;
import com.company.model.entity.SysPost;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SysPostService {
    Page<SysPost> page(Integer page, Integer size, Long deptId);
    SysPost getById(Long id);
    void create(PostDTO dto);
    void update(Long id, PostDTO dto);
    void delete(Long id);
}
