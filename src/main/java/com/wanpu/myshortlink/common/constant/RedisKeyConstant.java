package com.wanpu.myshortlink.common.constant;

public class RedisKeyConstant {

  /**
   * 短链接跳转缓存
   * 完整key格式：short_link:goto:localhost:8080/aB3x9Z
   */
  public static final String SHORT_LINK_GOTO_KEY = "short_link:goto:";

  /**
   * 短链接不存在的空值缓存（防穿透）
   * 短码不存在时缓存一个空值，避免每次都打穿到DB
   */
  public static final String SHORT_LINK_GOTO_IS_NULL_KEY =
      "short_link:is_null:";

  /**
   * 短链接跳转锁（防击穿）
   * 热点key过期时，用分布式锁保证只有一个请求重建缓存
   */
  public static final String SHORT_LINK_GOTO_LOCK_KEY =
      "short_link:lock:goto:";

  /**
   * UV 去重集合
   * 完整key格式：short_link:uv:{yyyyMMdd}:{fullShortUrl}
   */
  public static final String SHORT_LINK_UV_SET_KEY = "short_link:uv:";

  /**
   * 按天统计缓存（可选）
   * 完整key格式：short_link:stats:daily:{yyyyMMdd}:{fullShortUrl}
   */
  public static final String SHORT_LINK_STATS_DAILY_KEY = "short_link:stats:daily:";
}