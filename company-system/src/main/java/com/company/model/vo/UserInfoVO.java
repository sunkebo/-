package com.company.model.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserInfoVO {
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;
    private Long deptId;
    private String deptName;
    private Long postId;
    private String postName;
    private LocalDate hireDate;
    private String userType;
    private Integer status;
}
