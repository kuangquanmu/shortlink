-- 统计分析相关表（MySQL）

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

