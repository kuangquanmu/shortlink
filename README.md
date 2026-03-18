# 短链接系统

## 项目简介
基于 Spring Boot 3 的高并发短链接系统，实现短链接的创建与 302 跳转，
引入多级缓存机制，本机压测 500 并发下 QPS 达 4800+，P99 延迟 11ms。

## 技术栈
| 技术 | 说明 |
|------|------|
| Spring Boot 3.2 | 基础框架 |
| MyBatis-Plus 3.5.9 | ORM 框架 |
| MySQL 8.0 | 主数据库 |
| Redis 7.2 | 缓存层 |
| Redisson | 分布式锁，防止缓存击穿 |
| Caffeine | 本地缓存 |
| HikariCP | 数据库连接池 |
| Docker Compose | 服务编排 |

## 系统架构
```
Controller → Service → Mapper → MySQL
                ↓
           Redis 缓存层
           布隆过滤器
```

## 核心功能

### 创建短链接
- MurmurHash32 + Base62 生成 6 位短码（62^6 = 568 亿种组合）
- 哈希冲突时拼接时间戳重试，最多 3 次
- 设计路由表实现短码到分组的快速映射

### 短链接跳转
302 跳转流程：
```
请求进来
  → 布隆过滤器（拦截非法短码）
  → 空值缓存（拦截已确认不存在的短码）
  → Redis 缓存（命中直接跳转）
  → 分布式锁 + 双重检查（防击穿）
  → 查数据库 → 写入缓存 → 跳转
```

### 缓存三大问题解决方案
- **缓存穿透**：布隆过滤器拦截非法短码 + 空值缓存
- **缓存击穿**：Redisson 分布式锁 + 双重检查，保证同一时刻只有一个请求重建缓存
- **缓存雪崩**：缓存 TTL 加随机抖动（1~1.5小时），错峰过期

## 压测数据
测试环境：AMD Ryzen 9 7940H 8核16GB，服务与压测工具同机运行

| 场景 | 并发数 | QPS | P99延迟 | 错误率 |
|------|--------|-----|---------|--------|
| 单码热点 | 500 | 4865 | 6ms | 0.00% |
| 多码随机（10个短码） | 500 | 4777 | 11ms | 0.00% |

## 快速启动

### 环境要求
- JDK 17+
- MySQL 8.0
- Docker Desktop

### 启动步骤

**1. 启动 Redis**
```bash
docker-compose up -d
```

**2. 初始化数据库**

在 MySQL 中执行：
```sql
CREATE DATABASE shortlink DEFAULT CHARACTER SET utf8mb4;
USE shortlink;

CREATE TABLE `t_link` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `domain`          VARCHAR(128)  NOT NULL,
    `short_uri`       VARCHAR(8)    NOT NULL,
    `full_short_url`  VARCHAR(128)  NOT NULL,
    `origin_url`      VARCHAR(1024) NOT NULL,
    `click_num`       INT           NOT NULL DEFAULT 0,
    `gid`             VARCHAR(32)   NOT NULL,
    `valid_date_type` TINYINT       NOT NULL DEFAULT 0,
    `valid_date`      DATETIME      NULL,
    `describe`        VARCHAR(1024) NULL,
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `del_flag`        TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_full_short_url` (`full_short_url`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_link_goto` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT,
    `gid`            VARCHAR(32)   NOT NULL,
    `full_short_url` VARCHAR(128)  NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_full_short_url` (`full_short_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_link_stats_daily (
                                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                  full_short_url VARCHAR(255) NOT NULL,
    stats_date DATE NOT NULL,
    pv BIGINT NOT NULL DEFAULT 0,
    uv BIGINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_full_short_url_date (full_short_url, stats_date),
    KEY idx_stats_date (stats_date)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_link_access_log (
                                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                 full_short_url VARCHAR(255) NOT NULL,
    uv_id VARCHAR(64) NULL,
    ip VARCHAR(64) NULL,
    user_agent VARCHAR(512) NULL,
    referer VARCHAR(512) NULL,
    access_time DATETIME NOT NULL,
    KEY idx_full_short_url_time (full_short_url, access_time),
    KEY idx_access_time (access_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


```

**3. 修改配置**

`application-dev.yml` 中修改数据库密码：
```yaml
spring:
  datasource:
    password: 你的MySQL密码
```

**4. 启动项目**

运行 `MyshortlinkApplication.java` 的 main 方法。

**5. 测试接口**

创建短链接：
```http
POST http://localhost:8080/api/short-link/v1/create
Content-Type: application/json

{
  "originUrl": "https://www.baidu.com",
  "gid": "default",
  "describe": "测试"
}
```

访问跳转：
```
浏览器打开 http://localhost:8080/api/short-link/{shortUri}
```