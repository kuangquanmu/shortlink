package com.wanpu.myshortlink.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanpu.myshortlink.dao.entity.ShortLinkDO;
import com.wanpu.myshortlink.dao.mapper.ShortLinkMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class BloomFilterConfig implements ApplicationRunner {

  private final RedissonClient redissonClient;
  private final ShortLinkMapper shortLinkMapper;
  public static final String SHORT_LINK_BLOOM_FILTER = "short_link:bloom_filter";

  @Bean
  public RBloomFilter<String> shortUriCreateBloomFilter() {
    RBloomFilter<String> rBloomFilter = redissonClient.getBloomFilter(SHORT_LINK_BLOOM_FILTER);
    rBloomFilter.tryInit(1000000L,0.001);
    return rBloomFilter;
  }

  @Override
  public void run(ApplicationArguments args) {
    log.info("布隆过滤器开始预热...");
    RBloomFilter<String> bloomFilter = shortUriCreateBloomFilter();
    List<ShortLinkDO> links = shortLinkMapper.selectList(
        new LambdaQueryWrapper<ShortLinkDO>().
            select(ShortLinkDO::getFullShortUrl).
            eq(ShortLinkDO::getDelFlag,0)
    );
    links.forEach(link -> bloomFilter.add(link.getFullShortUrl()));
    log.info("成功加载{}条数据", links.size());
  }
}

