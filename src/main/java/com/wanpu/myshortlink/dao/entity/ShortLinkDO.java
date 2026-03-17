package com.wanpu.myshortlink.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link")   // MyBatis-Plus 分表时动态替换
public class ShortLinkDO {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 域名，例如 s.wanpu.com */
  private String domain;

  /** 短链接后缀，6位，例如 aB3x9Z */
  private String shortUri;

  /** 完整短链接 = domain + "/" + shortUri */
  private String fullShortUrl;

  /** 原始长链接 */
  private String originUrl;

  /** 点击量 */
  private Integer clickNum;

  /** 分组标识 */
  private String gid;

  /** 有效期类型：0=永久，1=自定义 */
  private Integer validDateType;

  /** 自定义有效期 */
  private LocalDateTime validDate;

  /** 描述备注 */
  @TableField("`describe`")
  private String describe;

  /** 创建时间（由数据库自动填充） */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  /** 更新时间（由数据库自动填充） */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  /** 逻辑删除：0未删除 1已删除 */
  @TableLogic
  private Integer delFlag;
}