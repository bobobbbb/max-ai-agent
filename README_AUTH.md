# 用户注册登录系统

基于 sa-token 实现的用户注册登录管理系统。

## 功能特性

- ✅ 用户注册
- ✅ 用户登录
- ✅ 用户登出
- ✅ 获取用户信息
- ✅ 用户名/邮箱/手机号重复检查
- ✅ 密码BCrypt加密
- ✅ Token认证
- ✅ 权限控制
- ✅ 全局异常处理

## 技术栈

- Spring Boot 3.4.7
- sa-token 1.38.0
- MyBatis Plus 3.5.5
- MySQL 8.0+
- Redis (用于token存储)
- BCrypt (密码加密)

## 数据库配置

### 1. 创建数据库
```sql
CREATE DATABASE ai_agent CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 创建用户表
```sql
CREATE TABLE `user` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（哈希后）',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0=禁用 1=正常',
    `role` VARCHAR(50) DEFAULT 'user' COMMENT '角色',
    `credits` INT DEFAULT 0 COMMENT '可用额度',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 3. 环境变量配置
在 application.yml 中配置以下环境变量:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_agent?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
```

或者在系统环境变量中设置:
- `DB_USERNAME`: 数据库用户名
- `DB_PASSWORD`: 数据库密码

## API 接口

### 1. 用户注册
```http
POST /api/user/register
Content-Type: application/json

{
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "password": "123456",
    "confirmPassword": "123456"
}
```

### 2. 用户登录
```http
POST /api/user/login
Content-Type: application/json

{
    "username": "testuser",
    "password": "123456"
}
```

### 3. 用户登出
```http
POST /api/user/logout
Authorization: Bearer {token}
```

### 4. 获取用户信息
```http
GET /api/user/info
Authorization: Bearer {token}
```

### 5. 检查用户名是否可用
```http
GET /api/user/check-username?username=testuser
```

### 6. 检查邮箱是否可用
```http
GET /api/user/check-email?email=test@example.com
```

### 7. 检查手机号是否可用
```http
GET /api/user/check-phone?phone=13800138000
```

## Token 使用

### 1. 前端存储Token
登录成功后，后端会返回token，前端需要存储此token：
```javascript
localStorage.setItem('satoken', response.data.token);
```

### 2. 请求头携带Token
后续请求需要在请求头中携带token：
```javascript
headers: {
    'satoken': localStorage.getItem('satoken')
}
```

## 权限控制

### 1. 检查登录状态
```java
@GetMapping("/protected")
public Result<String> protectedEndpoint() {
    // 检查是否登录，未登录会抛出异常
    StpUtil.checkLogin();
    return Result.success("这是受保护的接口");
}
```

### 2. 角色权限控制
```java
@GetMapping("/admin")
public Result<String> adminEndpoint() {
    // 检查是否具有admin角色
    StpUtil.checkRole("admin");
    return Result.success("管理员专用接口");
}
```

### 3. 权限码控制
```java
@GetMapping("/user-manage")
public Result<String> userManageEndpoint() {
    // 检查是否具有用户管理权限
    StpUtil.checkPermission("user:write");
    return Result.success("用户管理接口");
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未登录/Token无效 |
| 403 | 权限不足 |
| 500 | 服务器内部错误 |

## 开发调试

### 1. 启动项目
```bash
mvn spring-boot:run
```

### 2. 访问Swagger文档
http://localhost:8123/api/swagger-ui.html

### 3. 查看API文档
http://localhost:8123/api/doc.html (knife4j)

## 安全建议

1. **密码强度**: 建议前端增加密码强度验证
2. **验证码**: 生产环境建议添加图形验证码或短信验证码
3. **登录限制**: 建议添加登录失败次数限制
4. **Token刷新**: 建议实现Token自动刷新机制
5. **HTTPS**: 生产环境必须使用HTTPS

## 扩展功能

后续可以扩展的功能：
- 忘记密码/重置密码
- 邮箱验证
- 手机号验证
- 第三方登录(微信、QQ等)
- 用户头像上传
- 用户资料修改 