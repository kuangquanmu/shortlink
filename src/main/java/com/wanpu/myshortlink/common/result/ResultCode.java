package com.wanpu.myshortlink.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

  SUCCESS("0", "操作成功"),
  SYSTEM_ERROR("-1", "系统繁忙，请稍后重试"),

  // 短链接相关错误码
  SHORT_LINK_NOT_EXISTS("A000001", "短链接不存在"),
  SHORT_LINK_EXPIRED("A000002", "短链接已过期"),
  SHORT_LINK_GENERATE_FAIL("A000003", "短链接生成失败，请重试"),
  SHORT_LINK_HAS_EXISTED("A000004", "短链接已存在"),

  // 参数相关
  PARAM_ERROR("B000001", "参数校验失败");

  private final String code;
  private final String message;
}