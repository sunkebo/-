package com.company.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.common.BusinessException;
import com.company.common.Result;
import com.company.mapper.SysUserMapper;
import com.company.model.dto.LoginDTO;
import com.company.model.dto.PasswordDTO;
import com.company.model.entity.SysDept;
import com.company.model.entity.SysPost;
import com.company.model.entity.SysUser;
import com.company.model.vo.LoginVO;
import com.company.model.vo.UserInfoVO;
import com.company.security.JwtUtils;
import com.company.mapper.SysDeptMapper;
import com.company.mapper.SysPostMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysPostMapper postMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getUserType());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserInfo(buildUserInfo(user));
        return Result.ok("登录成功", vo);
    }

    @GetMapping("/info")
    public Result<UserInfoVO> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        return Result.ok(buildUserInfo(user));
    }

    @PutMapping("/password")
    public Result<String> changePassword(@Valid @RequestBody PasswordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SysUser user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
        return Result.ok("密码修改成功");
    }

    private UserInfoVO buildUserInfo(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setDeptId(user.getDeptId());
        vo.setPostId(user.getPostId());
        vo.setHireDate(user.getHireDate());
        vo.setUserType(user.getUserType());
        vo.setStatus(user.getStatus());

        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) vo.setDeptName(dept.getName());
        }
        if (user.getPostId() != null) {
            SysPost post = postMapper.selectById(user.getPostId());
            if (post != null) vo.setPostName(post.getName());
        }
        return vo;
    }
}
