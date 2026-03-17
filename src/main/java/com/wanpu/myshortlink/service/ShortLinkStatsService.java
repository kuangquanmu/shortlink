package com.wanpu.myshortlink.service;

import com.wanpu.myshortlink.dto.resp.ShortLinkStatsDailyRespDTO;
import com.wanpu.myshortlink.dto.req.ShortLinkAccessRecordDTO;
import java.time.LocalDate;
import java.util.List;

public interface ShortLinkStatsService {

  void recordAccess(String fullShortUrl, String uvId, ShortLinkAccessRecordDTO record);

  List<ShortLinkStatsDailyRespDTO> queryDailyStats(
      String fullShortUrl, LocalDate startDate, LocalDate endDate);
}

