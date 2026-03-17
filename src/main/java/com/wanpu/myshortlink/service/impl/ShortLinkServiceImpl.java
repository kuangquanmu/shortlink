package com.wanpu.myshortlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wanpu.myshortlink.common.constant.RedisKeyConstant;
import com.wanpu.myshortlink.common.exceptions.ServiceException;
import com.wanpu.myshortlink.common.result.ResultCode;
import com.wanpu.myshortlink.dao.entity.ShortLinkDO;
import com.wanpu.myshortlink.dao.entity.ShortLinkGoToDO;
import com.wanpu.myshortlink.dao.mapper.ShortLinkGoToMapper;
import com.wanpu.myshortlink.dao.mapper.ShortLinkMapper;
import com.wanpu.myshortlink.dto.req.ShortLinkCreateReqDTO;
import com.wanpu.myshortlink.dto.req.ShortLinkAccessRecordDTO;
import com.wanpu.myshortlink.dto.resp.ShortLinkCreateRespDTO;
import com.wanpu.myshortlink.service.ShortLinkStatsService;
import com.wanpu.myshortlink.utils.HashUtil;
import com.wanpu.myshortlink.utils.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wanpu.myshortlink.service.ShortLinkService;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper,ShortLinkDO>
    implements ShortLinkService {

  private final ShortLinkGoToMapper shortLinkGoToMapper;
  private final RBloomFilter<String> bloomFilter;
  private final StringRedisTemplate stringRedisTemplate;
  private final RedissonClient redissonClient;
  private final ShortLinkStatsService shortLinkStatsService;

  @Value("${short-link.domain}")
  private String domain;


  @Override
  @Transactional(rollbackFor = Exception.class)
  public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO req) {
    String shortCode = generateShortLink(req.getOriginUrl());
    String fullShortUrl = domain  + '/' + shortCode;
    ShortLinkDO shortLinkDO = ShortLinkDO.builder().domain(domain).
        fullShortUrl(fullShortUrl).
        shortUri(shortCode).
        originUrl(req.getOriginUrl()).
        clickNum(0).
        gid(req.getGid()).describe(req.getDescribe()).delFlag(0).build();

    ShortLinkGoToDO shortLinkGoToDO = ShortLinkGoToDO.builder().
        gid(req.getGid()).fullShortUrl(fullShortUrl).build();

    try{
      baseMapper.insert(shortLinkDO);
      shortLinkGoToMapper.insert(shortLinkGoToDO);
    }catch (Exception e){
      log.error("[创建短链接] 写入数据库异常, fullShortUrl: {}", fullShortUrl, e);
      throw new ServiceException(ResultCode.SHORT_LINK_GENERATE_FAIL);
    }

    bloomFilter.add(fullShortUrl);

    stringRedisTemplate.opsForValue().set(RedisKeyConstant.SHORT_LINK_GOTO_KEY + fullShortUrl,req.getOriginUrl(), Duration.ofHours(24));


    return ShortLinkCreateRespDTO.builder().gid(req.getGid()).fullShortUrl("http://"+fullShortUrl).originUrl(req.getOriginUrl()).build();
  }

  @Override
  @SneakyThrows
  public void redirectShortLink(String shortUri, HttpServletRequest request,
      HttpServletResponse response) {
    String serverName = request.getServerName();
    String serverPort = request.getServerPort() == 80
        ? "" : ":" + request.getServerPort();
    String fullShortUrl = serverName + serverPort + "/" + shortUri;

    // 布隆过滤器
    if(!bloomFilter.contains(fullShortUrl)){
      log.debug("[跳转] 访问非法短链{}",shortUri);
      response.sendRedirect("/page/not");
      return;
    }

    // 用 cookie 做 UV 身份（不存在则下发）
    String uvId = RequestUtil.getOrSetUvId(request, response);
    ShortLinkAccessRecordDTO accessRecord =
        ShortLinkAccessRecordDTO.builder()
            .ip(RequestUtil.getClientIp(request))
            .userAgent(request.getHeader("User-Agent"))
            .referer(request.getHeader("Referer"))
            .build();

    //空值缓存
    String nullFlag = stringRedisTemplate.opsForValue().
        get(RedisKeyConstant.SHORT_LINK_GOTO_IS_NULL_KEY+fullShortUrl);
    if(nullFlag != null){
      log.debug("[跳转] 短链不存在{}",shortUri);
      response.sendRedirect("/page/not");
      return;
    }

    //redis 缓存
    String originUrl = stringRedisTemplate.opsForValue().
        get(RedisKeyConstant.SHORT_LINK_GOTO_KEY+fullShortUrl);
    if(originUrl != null){
      log.debug("[跳转] 缓存命中{}",shortUri);
      shortLinkStatsService.recordAccess(fullShortUrl, uvId, accessRecord);
      response.sendRedirect(originUrl);
      return;
    }

    //查数据库
    RLock lock = redissonClient.getLock(RedisKeyConstant.SHORT_LINK_GOTO_LOCK_KEY+fullShortUrl);
    lock.lock();
    try{

      originUrl = stringRedisTemplate.opsForValue()
          .get(RedisKeyConstant.SHORT_LINK_GOTO_KEY + fullShortUrl);
      if(originUrl != null) {  // Redis有缓存直接返回
        shortLinkStatsService.recordAccess(fullShortUrl, uvId, accessRecord);
        response.sendRedirect(originUrl);
        return;
      }

      originUrl = getOriginUrlFromDB(fullShortUrl);

      if(originUrl == null){
        stringRedisTemplate.opsForValue().set(RedisKeyConstant.SHORT_LINK_GOTO_IS_NULL_KEY+fullShortUrl,"-",
            Duration.ofMinutes(30));
        response.sendRedirect("/page/not");
        return;
      }

      long ttl = 3600 + (long)(Math.random() * 1800); // 1~1.5小时随机
      stringRedisTemplate.opsForValue().set(
          RedisKeyConstant.SHORT_LINK_GOTO_KEY + fullShortUrl,
          originUrl,
          ttl, TimeUnit.SECONDS
      );
      shortLinkStatsService.recordAccess(fullShortUrl, uvId, accessRecord);
      response.sendRedirect(originUrl);
    }
    finally{
      lock.unlock();
    }
  }

  private String getOriginUrlFromDB(String fullShortUrl) {
    // 第一步：查路由表拿 gid
    LambdaQueryWrapper<ShortLinkGoToDO> gotoWrapper =
        new LambdaQueryWrapper<>();
    gotoWrapper.eq(ShortLinkGoToDO::getFullShortUrl, fullShortUrl);
    ShortLinkGoToDO gotoObj = shortLinkGoToMapper.selectOne(gotoWrapper);

    if (gotoObj == null) {
      return null;
    }

    // 第二步：带 gid 查主表
    LambdaQueryWrapper<ShortLinkDO> linkWrapper =
        new LambdaQueryWrapper<>();
    linkWrapper.eq(ShortLinkDO::getGid, gotoObj.getGid())
        .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
        .eq(ShortLinkDO::getDelFlag, 0);
    ShortLinkDO link = baseMapper.selectOne(linkWrapper);

    if (link == null) {
      return null;
    }

    // 检查有效期
    if (link.getValidDateType() == 1 && link.getValidDate() != null
        && link.getValidDate().isBefore(java.time.LocalDateTime.now())) {
      return null;
    }

    return link.getOriginUrl();
  }

  private String generateShortLink(String originUrl) throws ServiceException {

    for (int i = 0; i < 3; i++){
      String target = i == 0 ? originUrl : originUrl + System.currentTimeMillis();
      String shortCode = HashUtil.hashToBase62(target);

      String fullShortUrl = domain + '/' + shortCode;
      if(bloomFilter.contains(fullShortUrl)) {
        log.warn("创建失败,第{}次重试中",i);
      }
      else{
        return shortCode;
      }
    }
    throw new ServiceException(ResultCode.SHORT_LINK_GENERATE_FAIL);
  }
}
