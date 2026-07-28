package com.company.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_salary")
public class SysSalary {

    @TableId(type = IdType.AUTO)
    private Long salaryId;

    private Long userId;

    private String salaryMonth;

    private BigDecimal baseSalary;

    private BigDecimal performance;

    private BigDecimal bonus;

    private BigDecimal deduction;

    private BigDecimal actualSalary;

    private String auditStatus;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
