package com.company.model.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CheckinVO {
    private Long id;
    private Long userId;
    private String realName;
    private String deptName;
    private LocalDate checkDate;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private String status;
    private String auditStatus;
    private String remark;
    private LocalDateTime createTime;
}
