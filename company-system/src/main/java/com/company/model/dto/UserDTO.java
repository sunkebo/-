package com.company.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserDTO {
    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String phone;
    private String email;
    private Integer gender;
    private Long deptId;
    private Long postId;
    private LocalDate hireDate;
    private String userType;
}
