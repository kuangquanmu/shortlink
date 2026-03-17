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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_link_goto")
public class ShortLinkGoToDO {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String gid;

  private String fullShortUrl;
}
