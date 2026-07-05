# Forum Backend

> 基于 Spring Boot + MongoDB 的论坛系统后端，采用前后端分离架构，实现用户管理、帖子互动、私信聊天、通知中心、个性化推荐及后台数据统计等功能。

## 📖 项目简介

Forum Backend 是一个完整的论坛系统后端项目，采用 Spring Boot 开发，MongoDB 作为数据库，实现了论坛社区常见功能，并扩展了推荐系统、浏览记录、访客机制、私信系统、数据统计等模块。

项目已完成 Linux 服务器部署，使用 Docker 部署 MongoDB，并结合 Nginx 实现反向代理和公网访问。

---

## ✨ 核心功能

### 用户模块
- 用户注册、登录（JWT 身份认证）
- 用户信息修改
- 头像上传
- 修改密码
- 关注 / 取关用户
- 我的帖子、我的评论、我的收藏
- 浏览记录
- 访客记录

### 帖子模块
- 发布帖子
- 图片上传
- 分类浏览
- 全文搜索
- 点赞 / 取消点赞
- 收藏 / 取消收藏
- 浏览量统计
- 热门排序

### 评论模块
- 一级评论
- 回复评论（楼中楼）
- 评论点赞
- 评论软删除

### 社交模块
- 私信聊天
- 会话管理
- 未读消息统计
- 通知中心（点赞、评论、回复、关注）

### 推荐系统
- 用户兴趣建模
- 基于行为的个性化推荐
- 协同过滤推荐
- 热度排序
- 时间衰减
- 冷启动兜底策略

### 数据统计
- 系统概览
- 趋势分析
- 热门帖子统计
- 分类分析
- 用户行为分析
- 推荐效果分析

---

## 🛠 技术栈

| 分类 | 技术 |
|------|------|
| 开发语言 | Java 17 |
| 后端框架 | Spring Boot |
| 数据库 | MongoDB |
| 身份认证 | JWT |
| 构建工具 | Maven |
| 部署环境 | Ubuntu、Docker、Nginx |
| 接口测试 | Apifox |

---

## 📂 项目结构

```text
src
├── controller     // 控制层
├── service        // 业务层
├── repository     // 数据访问层
├── model          // 实体类
├── config         // 配置类
├── interceptor    // JWT、权限拦截器
├── util           // 工具类
└── common         // 公共返回结果
```

---

## 🔐 权限控制

- JWT 身份认证
- 登录拦截器
- 管理员权限控制
- RESTful API 权限校验

---

## 🌟 项目亮点

- 基于 Spring Boot 的前后端分离架构
- MongoDB 文档数据库设计
- JWT + 拦截器实现身份认证与权限控制
- 基于用户行为的个性化推荐算法
- 支持全文搜索、评论树、通知中心、私信聊天
- 后台数据统计与可视化分析
- 支持 Linux + Docker + Nginx 部署

---

## 🚀 快速启动

### 1. 克隆项目

```bash
git clone <your-repository-url>
```

### 2. 安装依赖

```bash
mvn clean install
```

### 3. 配置数据库

启动 MongoDB，并修改 `application.yml` 中的数据库配置。

### 4. 启动项目

```bash
mvn spring-boot:run
```

或

```bash
java -jar forum-backend.jar
```

---

## 📸 项目展示

建议在此处添加以下截图：

- 首页
- 帖子详情
- 用户主页
- 私信聊天
- 通知中心
- 后台数据统计

---

## 📈 后续优化

- Redis 缓存
- Kafka 异步消息队列
- WebSocket 实时聊天
- Spring Security
- Docker Compose
- GitHub Actions 自动化部署

---

## 📄 License

仅用于学习交流与个人作品展示。
