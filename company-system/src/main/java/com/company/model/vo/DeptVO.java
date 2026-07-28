package com.company.model.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeptVO {
    private Long deptId;
    private String name;
    private Long parentId;
    private String leader;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private List<DeptVO> children;
}
