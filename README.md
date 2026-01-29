# Art Design Pro Backend

## 项目简介
Art Design Pro 后端服务，基于 Spring Boot 3.3.0 开发，提供考勤系统、HR 系统和 OA 流程的后端支持。

## 技术栈
- **框架**：Spring Boot 3.3.0
- **语言**：Java 21 LTS
- **数据库**：PostgreSQL 18
- **持久层**：Spring Data JPA
- **安全**：Spring Security
- **监控**：Spring Boot Actuator

## 环境要求
- **Java**：JDK 21 LTS 或更高版本（系统默认：21.0.10 LTS）
- **Maven**：3.9.12 或更高版本
- **PostgreSQL**：15.0+（当前安装：18）
- **数据库**：需要创建 `system` 数据库

## 数据库配置
1. 启动 PostgreSQL 服务
2. 创建数据库 `system`
3. 修改 `src/main/resources/application.properties` 中的数据库连接信息（如需）：
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/system
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

## 项目结构
```
backend/
├── pom.xml                    # Maven 依赖配置
├── README.md                  # 项目说明文档
└── src/main/
    ├── java/com/artdesign/backend/
    │   ├── BackendApplication.java  # 主应用类
    │   ├── entity/           # 实体类（如 User）
    │   ├── repository/       # 数据访问层（JPA 接口）
    │   ├── service/          # 业务逻辑层（接口 + 实现）
    │   ├── controller/       # HTTP 控制器（REST API）
    │   └── config/           # 配置类（预留）
    └── resources/
        └── application.properties  # 系统配置
```

## 启动项目
1. **构建项目**：
   ```bash
   mvn clean package
   ```

2. **运行项目**：
   ```bash
   mvn spring-boot:run
   ```
   或
   ```bash
   java -jar target/backend-0.0.1-SNAPSHOT.jar
   ```

3. **访问服务**：
   - API 基础路径：`http://localhost:8080/api`
   - 健康检查：`http://localhost:8080/api/actuator/health`
   - 用户管理 API：`http://localhost:8080/api/users`

## 默认账号
- **用户名**：admin
- **密码**：admin123

## 后期扩展
- **考勤系统**：添加 `Attendance` 实体和相关 API
- **HR 系统**：添加 `Employee`、`Department`、`Position` 等实体
- **OA 流程**：集成 Activiti 流程引擎
- **微服务拆分**：使用 Spring Cloud 实现服务独立部署
