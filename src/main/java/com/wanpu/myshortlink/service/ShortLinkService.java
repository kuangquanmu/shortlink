package com.wanpu.myshortlink.service;

import com.wanpu.myshortlink.dto.req.ShortLinkCreateReqDTO;
import com.wanpu.myshortlink.dto.resp.ShortLinkCreateRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ShortLinkService {
  public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO req);

  public void redirectShortLink(String shortUri, HttpServletRequest request, HttpServletResponse response);

}
