-- ============================================================
-- 公司综合管理系统 - 数据库初始化脚本
-- Target: MySQL 9.7
-- ============================================================

CREATE DATABASE IF NOT EXISTS company_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE company_system;

-- ============================================================
-- 1. 部门表 (sys_dept)
-- ============================================================
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    dept_id     BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '部门ID',
    name        VARCHAR(100)    NOT NULL                  COMMENT '部门名称',
    parent_id   BIGINT          NOT NULL DEFAULT 0        COMMENT '上级部门ID，0表示顶级',
    leader      VARCHAR(50)     DEFAULT NULL              COMMENT '部门负责人',
    description VARCHAR(500)    DEFAULT NULL              COMMENT '部门简介',
    sort_order  INT             NOT NULL DEFAULT 0        COMMENT '排序号',
    status      TINYINT         NOT NULL DEFAULT 1        COMMENT '状态: 1-正常 0-停用',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (dept_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ============================================================
-- 2. 岗位表 (sys_post)
-- ============================================================
DROP TABLE IF EXISTS sys_post;
CREATE TABLE sys_post (
    post_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '岗位ID',
    dept_id      BIGINT          NOT NULL                  COMMENT '所属部门ID',
    name         VARCHAR(100)    NOT NULL                  COMMENT '岗位名称',
    description  VARCHAR(500)    DEFAULT NULL              COMMENT '岗位职责描述',
    salary_range VARCHAR(100)    DEFAULT NULL              COMMENT '薪资范围描述',
    sort_order   INT             NOT NULL DEFAULT 0        COMMENT '排序号',
    status       TINYINT         NOT NULL DEFAULT 1        COMMENT '状态: 1-正常 0-停用',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (post_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位表';

-- ============================================================
-- 3. 用户表 (sys_user)
-- ============================================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    user_id     BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    username    VARCHAR(50)     NOT NULL                  COMMENT '登录账号',
    password    VARCHAR(200)    NOT NULL                  COMMENT '密码(BCrypt加密)',
    real_name   VARCHAR(50)     NOT NULL                  COMMENT '真实姓名',
    phone       VARCHAR(20)     DEFAULT NULL              COMMENT '手机号',
    avatar      VARCHAR(500)    DEFAULT NULL              COMMENT '头像URL',
    email       VARCHAR(100)    DEFAULT NULL              COMMENT '邮箱',
    gender      TINYINT         NOT NULL DEFAULT 0        COMMENT '性别: 0-未知 1-男 2-女',
    dept_id     BIGINT          DEFAULT NULL              COMMENT '所属部门ID',
    post_id     BIGINT          DEFAULT NULL              COMMENT '所属岗位ID',
    hire_date   DATE            DEFAULT NULL              COMMENT '入职日期',
    user_type   VARCHAR(20)     NOT NULL DEFAULT 'employee' COMMENT '用户类型: admin-管理员 employee-员工',
    status      TINYINT         NOT NULL DEFAULT 1        COMMENT '状态: 1-正常 0-停用',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (user_id),
    UNIQUE INDEX uk_username (username),
    INDEX idx_dept_id (dept_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_type (user_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 4. 考勤打卡表 (sys_checkin)
-- ============================================================
DROP TABLE IF EXISTS sys_checkin;
CREATE TABLE sys_checkin (
    id            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '打卡记录ID',
    user_id       BIGINT          NOT NULL                  COMMENT '用户ID',
    check_date    DATE            NOT NULL                  COMMENT '打卡日期',
    checkin_time  DATETIME        DEFAULT NULL              COMMENT '上班打卡时间',
    checkout_time DATETIME        DEFAULT NULL              COMMENT '下班打卡时间',
    status        VARCHAR(20)     NOT NULL DEFAULT 'normal' COMMENT '状态: normal-正常 late-迟到 early-早退 absent-缺卡',
    audit_status  VARCHAR(20)     NOT NULL DEFAULT 'pending' COMMENT '审核状态: pending-待审核 approved-已通过 rejected-已驳回',
    remark        VARCHAR(500)    DEFAULT NULL              COMMENT '备注说明',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_check_date (check_date),
    INDEX idx_user_date (user_id, check_date),
    INDEX idx_status (status),
    INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤打卡表';

-- ============================================================
-- 5. 薪资表 (sys_salary)
-- ============================================================
DROP TABLE IF EXISTS sys_salary;
CREATE TABLE sys_salary (
    salary_id     BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '薪资记录ID',
    user_id       BIGINT          NOT NULL                  COMMENT '用户ID',
    salary_month  VARCHAR(7)      NOT NULL                  COMMENT '薪资月份，格式YYYY-MM',
    base_salary   DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '基本工资',
    performance   DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '绩效工资',
    bonus         DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '奖金/补贴',
    deduction     DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '扣款',
    actual_salary DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '实发工资',
    audit_status  VARCHAR(20)     NOT NULL DEFAULT 'pending' COMMENT '审核状态: pending-待审核 approved-已通过 rejected-已驳回',
    remark        VARCHAR(500)    DEFAULT NULL              COMMENT '备注',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (salary_id),
    INDEX idx_user_id (user_id),
    INDEX idx_salary_month (salary_month),
    INDEX idx_user_month (user_id, salary_month),
    INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='薪资表';

-- ============================================================
-- 初始数据
-- ============================================================

-- 管理员账号: admin / admin123
INSERT INTO sys_user (username, password, real_name, user_type, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKJzVNGq', '系统管理员', 'admin', 1);

-- 示例部门
INSERT INTO sys_dept (dept_id, name, parent_id, leader, description, sort_order) VALUES
(1, '总公司',   0, '张总',   '公司总部',                  1),
(2, '技术部',   1, '李经理', '负责产品研发与技术支持',    2),
(3, '人事部',   1, '王经理', '负责人力资源管理',          3),
(4, '财务部',   1, '赵经理', '负责财务管理与审计',        4),
(5, '市场部',   1, '陈经理', '负责市场推广与销售',        5);

-- 示例岗位
INSERT INTO sys_post (dept_id, name, description, salary_range, sort_order) VALUES
(2, '高级工程师', '负责核心系统架构设计与开发',     '15K-25K', 1),
(2, '初级工程师', '负责功能模块开发与维护',         '8K-12K',  2),
(3, '人事主管',   '负责招聘、培训、绩效管理',       '10K-15K', 1),
(3, '人事专员',   '负责日常人事事务处理',           '6K-8K',   2),
(4, '财务主管',   '负责财务规划与审计',             '12K-18K', 1),
(5, '市场主管',   '负责市场策略与品牌推广',         '12K-18K', 1);

-- 示例员工 (密码统一为 123456)
-- BCrypt:$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKJzVNGq = 123456
INSERT INTO sys_user (username, password, real_name, phone, email, gender, dept_id, post_id, hire_date, user_type, status) VALUES
('emp10001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKJzVNGq', '张三', '13800001001', 'zhangsan@company.com', 1, 2, 1, '2024-01-15', 'employee', 1),
('emp10002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKJzVNGq', '李四', '13800001002', 'lisi@company.com',     2, 2, 2, '2024-03-20', 'employee', 1),
('emp10003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKJzVNGq', '王五', '13800001003', 'wangwu@company.com',   1, 3, 3, '2024-06-01', 'employee', 1),
('emp10004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKJzVNGq', '赵六', '13800001004', 'zhaoliu@company.com',  1, 4, 5, '2025-01-10', 'employee', 1),
('emp10005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKJzVNGq', '陈七', '13800001005', 'chenqi@company.com',   2, 5, 6, '2025-02-18', 'employee', 1);
