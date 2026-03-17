package com.wanpu.myshortlink.service.impl;

import com.wanpu.myshortlink.common.constant.RedisKeyConstant;
import com.wanpu.myshortlink.dao.entity.ShortLinkAccessLogDO;
import com.wanpu.myshortlink.dao.entity.ShortLinkStatsDailyDO;
import com.wanpu.myshortlink.dao.mapper.ShortLinkAccessLogMapper;
import com.wanpu.myshortlink.dao.mapper.ShortLinkStatsDailyMapper;
import com.wanpu.myshortlink.dto.req.ShortLinkAccessRecordDTO;
import com.wanpu.myshortlink.dto.resp.ShortLinkStatsDailyRespDTO;
import com.wanpu.myshortlink.service.ShortLinkStatsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

  private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

  private final StringRedisTemplate stringRedisTemplate;
  private final ShortLinkStatsDailyMapper shortLinkStatsDailyMapper;
  private final ShortLinkAccessLogMapper shortLinkAccessLogMapper;

  @Override
  @Async("statsTaskExecutor")
  public void recordAccess(String fullShortUrl, String uvId, ShortLinkAccessRecordDTO record) {
    try {
      LocalDate today = LocalDate.now();
      String day = today.format(DAY_FMT);

      // PV：每次访问都 +1
      long pvInc = 1L;

      // UV：按天去重（同一短链 + 同一天 + 同一 uvId 只算一次）
      String uvKey = RedisKeyConstant.SHORT_LINK_UV_SET_KEY + day + ":" + fullShortUrl;
      Long added = stringRedisTemplate.opsForSet().add(uvKey, uvId);
      if (added != null && added > 0) {
        stringRedisTemplate.expire(uvKey, java.time.Duration.ofDays(2));
      }
      long uvInc = (added != null && added > 0) ? 1L : 0L;

      // 落聚合表（按天）
      shortLinkStatsDailyMapper.upsertIncrement(fullShortUrl, today, pvInc, uvInc);

      // 落明细表（后续可以扩展：地区、设备、referer 分析等）
      ShortLinkAccessLogDO logDO =
          ShortLinkAccessLogDO.builder()
              .fullShortUrl(fullShortUrl)
              .uvId(uvId)
              .ip(trimTo(record != null ? record.getIp() : null, 64))
              .userAgent(trimTo(record != null ? record.getUserAgent() : null, 512))
              .referer(trimTo(record != null ? record.getReferer() : null, 512))
              .accessTime(LocalDateTime.now())
              .build();
      shortLinkAccessLogMapper.insert(logDO);
    } catch (Exception e) {
      log.warn("[统计埋点] recordAccess failed, fullShortUrl={}", fullShortUrl, e);
    }
  }

  @Override
  public List<ShortLinkStatsDailyRespDTO> queryDailyStats(
      String fullShortUrl, LocalDate startDate, LocalDate endDate) {
    List<ShortLinkStatsDailyDO> rows =
        shortLinkStatsDailyMapper.listByDateRange(fullShortUrl, startDate, endDate);
    return rows.stream()
        .map(
            r ->
                ShortLinkStatsDailyRespDTO.builder()
                    .statsDate(r.getStatsDate())
                    .pv(r.getPv())
                    .uv(r.getUv())
                    .build())
        .collect(Collectors.toList());
  }

  private static String trimTo(String s, int maxLen) {
    if (s == null) {
      return null;
    }
    if (s.length() <= maxLen) {
      return s;
    }
    return s.substring(0, maxLen);
  }
}

