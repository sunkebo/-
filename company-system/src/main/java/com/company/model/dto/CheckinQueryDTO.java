package com.company.model.dto;

import lombok.Data;

@Data
public class CheckinQueryDTO {
    private Long userId;
    private Long deptId;
    private String checkDate;
    private String status;
    private String month;
    private Integer page = 1;
    private Integer size = 10;
}
