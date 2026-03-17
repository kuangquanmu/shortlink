package com.wanpu.myshortlink.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_access_log")
public class ShortLinkAccessLogDO {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String fullShortUrl;

  private String uvId;

  private String ip;

  private String userAgent;

  private String referer;

  private LocalDateTime accessTime;
}

