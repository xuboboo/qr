# QR Stat MVP - 二维码访问统计工具

一个内部使用的二维码访问统计 MVP：创建二维码 -> 用户扫码访问统计链接 -> 后端记录访问 -> 302 跳转目标网站。

## 技术栈

- 后端：Java 1.8 + Spring Boot 2.7.18 + H2 文件数据库 + ZXing
- 前端：Vue 2 + Element UI + Axios

## 目录

```text
qr-stat-mvp
├── backend   Spring Boot 后端
└── frontend  Vue2 前端
```

## 一键启动开发环境

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8080
```

H2 控制台：

```text
http://localhost:8080/h2-console
```

H2 连接信息：

```text
JDBC URL: jdbc:h2:file:./data/qrstat;MODE=MySQL;DATABASE_TO_LOWER=TRUE
User Name: sa
Password: 留空
```

### 2. 启动前端

```bash
cd frontend
npm install
npm run serve
```

前端地址：

```text
http://localhost:8081
```

## 默认登录账号

后台管理页已经加了登录保护。

```text
用户名：admin
密码：ChangeMe123!
```

生产环境请修改默认账号密码，不要直接使用默认值。

推荐通过环境变量设置：

```bash
export QR_ADMIN_USERNAME=admin
export QR_ADMIN_PASSWORD='你的强密码'
export QR_SESSION_EXPIRE_MINUTES=720
```

也可以直接修改：

```text
backend/src/main/resources/application.yml
```

```yaml
app:
  security:
    enabled: true
    admin-username: ${QR_ADMIN_USERNAME:admin}
    admin-password: ${QR_ADMIN_PASSWORD:ChangeMe123!}
    session-expire-minutes: ${QR_SESSION_EXPIRE_MINUTES:720}
```

说明：登录成功后，后端会生成随机 Token，并保存在服务端内存里；前端只保存这个登录 Token，不再使用固定写死的管理 Token。

## 安全说明

当前版本的安全策略：

```text
1. /api/auth/login 公开，用于后台登录
2. /api/qrcodes/** 管理接口需要登录 Token
3. /api/qrcodes/{code}/image 二维码图片允许公开访问，方便下载和预览
4. /q/{code} 用户扫码跳转接口必须公开，否则公众号用户无法访问
5. 后端默认阻止目标链接跳转到 localhost、127.0.0.1、内网 IP，降低 SSRF 风险
6. 登录会话默认 12 小时过期，后台持续使用时会自动续期
```

也就是说，别人拿到你的后台地址，不能直接查看二维码列表和统计数据；但是用户扫码用的 `/q/{code}` 必须公开。

## 扫码链路说明

假设你创建二维码时填的目标链接是：

```text
https://www.example.com/activity
```

系统生成的统计链接是：

```text
https://qr.yourcompany.com/q/a8x3k21p
```

二维码图片里实际写入的是统计链接，不是目标链接：

```text
https://qr.yourcompany.com/q/a8x3k21p
```

用户扫码后的真实流程：

```text
1. 用户扫码
2. 微信内置浏览器打开 https://qr.yourcompany.com/q/a8x3k21p
3. 你的 Spring Boot 后端收到请求
4. 后端写入一条访问记录：时间、IP、User-Agent、二维码短码等
5. 后端立即返回 HTTP 302 Redirect
6. 微信内置浏览器自动跳转到 https://www.example.com/activity
```

这个过程不是“停留几秒再跳”。后端记录完成后会立即 302 跳转，通常是几十毫秒到几百毫秒级，用户基本看到的是直接打开目标网站。

只有目标服务器慢、网络慢、数据库写入慢时，才可能感觉到一点延迟。

## 使用方式

1. 打开前端页面
2. 登录后台
3. 输入二维码名称和目标链接
4. 点击生成二维码
5. 下载二维码放到公众号文章
6. 用户扫码后，会先访问 `/q/{code}`，后端记录访问，然后 302 跳转到目标链接
7. 在后台查看访问量、今日访问量、最近 7 天趋势、访问明细

## 重要配置

后端配置文件：

```text
backend/src/main/resources/application.yml
```

开发默认：

```yaml
app:
  public-domain: http://localhost:8080
```

正式部署时改成你的公网 HTTPS 域名，例如：

```yaml
app:
  public-domain: https://qr.yourcompany.com
```

二维码里写入的就是：

```text
https://qr.yourcompany.com/q/{code}
```

## 生产部署建议

- 使用公司备案域名
- 使用 HTTPS
- Nginx 反向代理到后端 8080
- 后端 `app.public-domain` 配置成正式域名
- 修改默认后台账号密码
- 生产环境不要把 H2 控制台暴露到公网，建议改 MySQL，并关闭 `spring.h2.console.enabled`
- Nginx 层可以再加 IP 白名单，例如只允许公司办公网访问 `/api/**` 和前端后台页面
- 如要使用 MySQL，可按 README 下方说明切换

## 可选：切换 MySQL

1. 修改 `backend/pom.xml`，取消 MySQL 依赖注释
2. 新建 MySQL 数据库，例如：

```sql
CREATE DATABASE qr_stat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

3. 修改 `application.yml`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/qr_stat?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: your_password
  h2:
    console:
      enabled: false
```

4. 首次启动会自动执行 `schema.sql`。

## API 简表

### 登录

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "ChangeMe123!"
}
```

返回：

```json
{
  "success": true,
  "message": "success",
  "data": {
    "token": "后端生成的随机登录Token",
    "username": "admin"
  }
}
```

之后访问管理接口需要带 Header：

```http
Authorization: Bearer <token>
```

### 退出登录

```http
POST /api/auth/logout
Authorization: Bearer <token>
```

### 创建二维码

```http
POST /api/qrcodes
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "公众号6月活动",
  "targetUrl": "https://www.example.com/activity"
}
```

### 获取二维码图片

```http
GET /api/qrcodes/{code}/image
```

### 用户扫码访问

```http
GET /q/{code}
```

这个接口会立即返回 302 跳转到目标链接。

### 二维码列表

```http
GET /api/qrcodes
Authorization: Bearer <token>
```

### 统计详情

```http
GET /api/qrcodes/{code}/stats
Authorization: Bearer <token>
```

### 停用 / 启用

```http
PUT /api/qrcodes/{code}/enabled?enabled=false
Authorization: Bearer <token>
```
