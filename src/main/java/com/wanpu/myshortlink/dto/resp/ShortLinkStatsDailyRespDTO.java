package com.wanpu.myshortlink.dto.resp;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsDailyRespDTO {

  private LocalDate statsDate;
  private Long pv;
  private Long uv;
}

