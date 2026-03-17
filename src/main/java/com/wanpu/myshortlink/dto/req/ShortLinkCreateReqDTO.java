package com.wanpu.myshortlink.dto.req;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ShortLinkCreateReqDTO {

  @NotBlank(message = "原始链接不能为空")
  private String originUrl;

  @NotBlank(message = "分组信息不能为空")
  private String gid;

  /** 有效期类型：0=永久，1=自定义 */
  private Integer validDateType;

  /** 自定义有效期 */
  private LocalDateTime validDate;

  private String describe;

}
