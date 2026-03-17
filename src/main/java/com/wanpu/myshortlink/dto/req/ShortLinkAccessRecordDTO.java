package com.wanpu.myshortlink.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkAccessRecordDTO {

  private String ip;
  private String userAgent;
  private String referer;
}

