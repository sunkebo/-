package com.company.service;

import com.company.model.dto.DeptDTO;
import com.company.model.entity.SysDept;
import com.company.model.vo.DeptVO;

import java.util.List;

public interface SysDeptService {
    List<SysDept> listAll();
    List<DeptVO> buildTree();
    SysDept getById(Long id);
    void create(DeptDTO dto);
    void update(Long id, DeptDTO dto);
    void delete(Long id);
}
