-- 创建后台管理数据库
CREATE DATABASE IF NOT EXISTS mall_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall_admin;

-- 管理员表
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    resource_type VARCHAR(20) COMMENT '资源类型：menu=菜单，button=按钮，api=接口',
    resource_path VARCHAR(200) COMMENT '资源路径',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 管理员-角色关联表
CREATE TABLE IF NOT EXISTS admin_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_admin_role (admin_id, role_id),
    INDEX idx_admin_id (admin_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员-角色关联表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- 插入默认超级管理员（密码：admin123，BCrypt加密）
INSERT INTO admin_user (username, password, real_name, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1);

-- 插入默认角色
INSERT INTO role (role_name, role_code, description, status) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('商品管理员', 'PRODUCT_ADMIN', '管理商品相关', 1),
('订单管理员', 'ORDER_ADMIN', '管理订单相关', 1);

-- 插入默认权限
INSERT INTO permission (permission_name, permission_code, resource_type, resource_path, parent_id, sort_order) VALUES
-- 一级菜单
('商品管理', 'product:menu', 'menu', '/product', 0, 1),
('订单管理', 'order:menu', 'menu', '/order', 0, 2),
('用户管理', 'user:menu', 'menu', '/user', 0, 3),
('数据统计', 'dashboard:menu', 'menu', '/dashboard', 0, 4),
-- 商品管理子权限
('商品列表', 'product:list', 'api', '/api/admin/product/list', 1, 1),
('商品新增', 'product:add', 'api', '/api/admin/product/add', 1, 2),
('商品编辑', 'product:edit', 'api', '/api/admin/product/edit', 1, 3),
('商品删除', 'product:delete', 'api', '/api/admin/product/delete', 1, 4),
-- 订单管理子权限
('订单列表', 'order:list', 'api', '/api/admin/order/list', 2, 1),
('订单详情', 'order:detail', 'api', '/api/admin/order/detail', 2, 2),
('订单发货', 'order:ship', 'api', '/api/admin/order/ship', 2, 3),
('订单取消', 'order:cancel', 'api', '/api/admin/order/cancel', 2, 4);

-- 超级管理员关联超级管理员角色
INSERT INTO admin_role (admin_id, role_id) VALUES (1, 1);

-- 超级管理员角色关联所有权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission;