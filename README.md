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

- 首页
<img width="974" height="474" alt="image" src="https://github.com/user-attachments/assets/4434d267-32c3-4b5d-b1f0-80bed80e5f1e" />
<img width="878" height="499" alt="image" src="https://github.com/user-attachments/assets/e20a8f02-fcdc-413e-b1c1-f1e26405866e" />
<img width="974" height="504" alt="image" src="https://github.com/user-attachments/assets/f980dc09-37ca-4786-83e3-e33de118b9e2" />
<img width="974" height="510" alt="image" src="https://github.com/user-attachments/assets/4be52cc4-391b-4458-ac71-adadc73b1bc4" />

- 帖子详情
<img width="974" height="520" alt="image" src="https://github.com/user-attachments/assets/bc39056a-1668-44ca-93e6-9bdf6da9bca0" />
<img width="975" height="455" alt="image" src="https://github.com/user-attachments/assets/bdf159e6-a695-41c3-a0a6-d6c10b26a5ac" />

- 用户主页
<img width="974" height="571" alt="image" src="https://github.com/user-attachments/assets/5a5a5334-791a-4719-b9bf-35b427779da6" />
<img width="975" height="388" alt="image" src="https://github.com/user-attachments/assets/59cdae5d-386e-4f9d-a87e-9c8e105e9edb" />
<img width="974" height="296" alt="image" src="https://github.com/user-attachments/assets/bc913d74-79a6-4c4a-868f-45e9b83753b8" />
<img width="974" height="207" alt="image" src="https://github.com/user-attachments/assets/dcc2cb99-af94-4ea0-a5c7-04c68af097d1" />
<img width="974" height="217" alt="image" src="https://github.com/user-attachments/assets/ecb22699-e788-47f9-909d-cece3e0eb6af" />
<img width="975" height="146" alt="image" src="https://github.com/user-attachments/assets/901aff44-6035-4482-8a15-6e1652e003eb" />
<img width="974" height="514" alt="image" src="https://github.com/user-attachments/assets/7f7c3d6c-968f-4053-90f8-7b323f55a5a3" />
<img width="974" height="409" alt="image" src="https://github.com/user-attachments/assets/540154cb-5645-4a1e-b5a0-a56c935f5146" />
<img width="975" height="510" alt="image" src="https://github.com/user-attachments/assets/be73af1b-7182-4eb9-a53d-f7eeffcace8c" />
<img width="975" height="502" alt="image" src="https://github.com/user-attachments/assets/72bf8b2c-ad40-45be-8322-d3b28bb91cee" />

- 私信聊天
<img width="974" height="268" alt="image" src="https://github.com/user-attachments/assets/c1c01627-c067-4099-b329-1e8c999fc1e7" />
<img width="974" height="514" alt="image" src="https://github.com/user-attachments/assets/bc6436d9-3325-4fe3-be64-624020c13b82" />

- 通知中心
<img width="974" height="446" alt="image" src="https://github.com/user-attachments/assets/c913e29e-f697-4957-80fc-be61b72cf786" />
<img width="975" height="267" alt="image" src="https://github.com/user-attachments/assets/d72814c4-aaa5-482d-9e2a-4edb804040f6" />

- 后台数据统计
<img width="974" height="515" alt="image" src="https://github.com/user-attachments/assets/770191cd-dfa3-4889-831f-3683cdf66f9f" />
<img width="975" height="347" alt="image" src="https://github.com/user-attachments/assets/af640b12-02ff-496e-be2e-844fccaff2b7" />
<img width="975" height="511" alt="image" src="https://github.com/user-attachments/assets/60d5488f-7d3b-4160-a648-7b19463ccd69" />
<img width="975" height="521" alt="image" src="https://github.com/user-attachments/assets/74fc3bae-28f7-49b4-85fe-562568a5e62a" />
<img width="975" height="499" alt="image" src="https://github.com/user-attachments/assets/689e9d3d-e95c-498c-b782-127405e30607" />
<img width="975" height="126" alt="image" src="https://github.com/user-attachments/assets/8c6acc9d-94cf-44e2-b8c5-124f87d7f02f" />
<img width="975" height="525" alt="image" src="https://github.com/user-attachments/assets/14939b67-8f03-4644-95b6-97faa51c888f" />

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
