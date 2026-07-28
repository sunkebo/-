package com.company.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalaryVO {
    private Long salaryId;
    private Long userId;
    private String realName;
    private String deptName;
    private String postName;
    private String salaryMonth;
    private BigDecimal baseSalary;
    private BigDecimal performance;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal actualSalary;
    private String auditStatus;
    private String remark;
    private LocalDateTime createTime;
}
