package com.company.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostDTO {
    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    @NotBlank(message = "岗位名称不能为空")
    private String name;

    private String description;

    private String salaryRange;

    private Integer sortOrder;
}
