package com.company.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplementDTO {
    @NotBlank(message = "补卡日期不能为空")
    private String checkDate;

    @NotBlank(message = "补卡类型不能为空")
    private String type;  // in / out

    private String reason;
}
