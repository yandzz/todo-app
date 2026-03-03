# Todo App - Java + Vue.js 版本

## 技术栈

- **后端**: Java 17 + Spring Boot 3.2
- **数据库**: MySQL 8.0
- **前端**: Vue 3 + Chart.js

## 项目结构

```
todo-app-java/
├── backend/                 # Spring Boot 后端
│   ├── src/
│   │   └── main/
│   │       ├── java/com/todo/
│   │       │   ├── controller/  # REST API控制器
│   │       │   ├── service/    # 业务逻辑
│   │       │   ├── model/      # 数据模型
│   │       │   └── repository/ # 数据访问
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
└── frontend/              # Vue.js 前端
    └── index.html
```

## 快速开始

### 1. 准备MySQL数据库

```sql
CREATE DATABASE todo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 启动后端

```bash
cd backend
./mvnw spring-boot:run
```

后端将在 http://localhost:8080 启动

### 3. 部署前端

将 `frontend/index.html` 部署到任何Web服务器，或者直接用浏览器打开。

## API接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/tasks | 获取所有任务 |
| POST | /api/tasks | 创建任务 |
| PUT | /api/tasks/{id} | 更新任务 |
| PATCH | /api/tasks/{id}/toggle | 切换任务状态 |
| DELETE | /api/tasks/{id} | 删除任务 |
| GET | /api/stats | 获取统计信息 |
| GET | /api/health | 健康检查 |

## 环境变量

- `SERVER_PORT`: 后端端口 (默认 8080)
- `SPRING_DATASOURCE_URL`: 数据库URL
- `SPRING_DATASOURCE_USERNAME`: 数据库用户名
- `SPRING_DATASOURCE_PASSWORD`: 数据库密码
