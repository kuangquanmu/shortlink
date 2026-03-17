package com.wanpu.myshortlink.controller;

import static com.wanpu.myshortlink.common.result.Result.success;

import com.wanpu.myshortlink.common.result.Result;
import com.wanpu.myshortlink.dto.req.ShortLinkCreateReqDTO;
import com.wanpu.myshortlink.dto.resp.ShortLinkCreateRespDTO;
import com.wanpu.myshortlink.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class ShortLinkController {

  private final ShortLinkService shortLinkService;

  @PostMapping("/v1/create")
  public Result<ShortLinkCreateRespDTO> createShortLink(
      @RequestBody @Valid ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
    return Result.success(shortLinkService.createShortLink(shortLinkCreateReqDTO));
  }

  @GetMapping("/{shortUri}")
  public void redirectShortLink(@PathVariable("shortUri") String shortUri, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    shortLinkService.redirectShortLink(shortUri,httpServletRequest,httpServletResponse);
  }

}
