package com.wanpu.myshortlink.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_stats_daily")
public class ShortLinkStatsDailyDO {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String fullShortUrl;

  private LocalDate statsDate;

  private Long pv;

  private Long uv;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;
}

