package com.wanpu.myshortlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanpu.myshortlink.dao.entity.ShortLinkStatsDailyDO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ShortLinkStatsDailyMapper extends BaseMapper<ShortLinkStatsDailyDO> {

  @Insert("""
      INSERT INTO t_link_stats_daily (full_short_url, stats_date, pv, uv, create_time, update_time)
      VALUES (#{fullShortUrl}, #{statsDate}, #{pvInc}, #{uvInc}, NOW(), NOW())
      ON DUPLICATE KEY UPDATE
        pv = pv + VALUES(pv),
        uv = uv + VALUES(uv),
        update_time = NOW()
      """)
  int upsertIncrement(
      @Param("fullShortUrl") String fullShortUrl,
      @Param("statsDate") LocalDate statsDate,
      @Param("pvInc") long pvInc,
      @Param("uvInc") long uvInc);

  @Select("""
      SELECT id, full_short_url, stats_date, pv, uv, create_time, update_time
      FROM t_link_stats_daily
      WHERE full_short_url = #{fullShortUrl}
        AND stats_date BETWEEN #{startDate} AND #{endDate}
      ORDER BY stats_date ASC
      """)
  List<ShortLinkStatsDailyDO> listByDateRange(
      @Param("fullShortUrl") String fullShortUrl,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}

