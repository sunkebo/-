package com.company.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 批量生成薪资DTO —— 为所有在职员工批量生成指定月份的薪资
 */
@Data
public class BatchSalaryDTO {

    @NotBlank(message = "薪资月份不能为空")
    private String salaryMonth;

    private BigDecimal baseSalary;
    private BigDecimal performance;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private String remark;
}
