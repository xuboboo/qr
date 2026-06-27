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

## 使用方式

1. 打开前端页面
2. 输入二维码名称和目标链接
3. 点击生成二维码
4. 下载二维码放到公众号文章
5. 用户扫码后，会先访问 `/q/{code}`，后端记录访问，然后 302 跳转到目标链接
6. 在后台查看访问量、今日访问量、最近 7 天趋势、访问明细

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

### 创建二维码

```http
POST /api/qrcodes
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

### 二维码列表

```http
GET /api/qrcodes
```

### 统计详情

```http
GET /api/qrcodes/{code}/stats
```

### 停用 / 启用

```http
PUT /api/qrcodes/{code}/enabled?enabled=false
```
