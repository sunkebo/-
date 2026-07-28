package com.company.service;

import com.company.model.dto.BatchSalaryDTO;
import com.company.model.dto.CheckinAuditDTO;
import com.company.model.dto.SalaryDTO;
import com.company.model.vo.PageVO;
import com.company.model.vo.SalaryVO;

import java.util.List;
import java.util.Map;

public interface SysSalaryService {
    PageVO<SalaryVO> page(Integer page, Integer size, String salaryMonth, Long deptId, Long userId, String auditStatus);
    SalaryVO getById(Long id);
    void create(SalaryDTO dto);
    void batchCreate(List<SalaryDTO> list);
    void batchGenerate(BatchSalaryDTO dto);
    void update(Long id, SalaryDTO dto);
    void delete(Long id);
    void audit(Long id, CheckinAuditDTO dto);
    PageVO<SalaryVO> myPage(Long userId, Integer page, Integer size, String month);
    Map<String, Object> stats(String salaryMonth);
}
