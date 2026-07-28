package com.company.service;

import com.company.model.dto.UserDTO;
import com.company.model.entity.SysUser;
import com.company.model.vo.UserInfoVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SysUserService {
    Page<UserInfoVO> page(Integer page, Integer size, String realName, Long deptId, Long postId, String userType);
    UserInfoVO getById(Long id);
    void create(UserDTO dto);
    void update(Long id, UserDTO dto);
    void delete(Long id);
    void resetPassword(Long id);
    void updateStatus(Long id, Integer status);
}
