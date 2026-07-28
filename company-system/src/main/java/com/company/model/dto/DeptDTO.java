package com.company.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeptDTO {
    @NotBlank(message = "部门名称不能为空")
    private String name;

    private Long parentId;

    private String leader;

    private String description;

    private Integer sortOrder;
}
