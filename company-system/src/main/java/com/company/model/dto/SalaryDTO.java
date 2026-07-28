package com.company.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalaryDTO {
    @NotNull(message = "用户不能为空")
    private Long userId;

    @NotBlank(message = "薪资月份不能为空")
    private String salaryMonth;

    private BigDecimal baseSalary;
    private BigDecimal performance;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private String remark;
}
