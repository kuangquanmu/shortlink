package com.wanpu.myshortlink.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;

public class RequestUtil {

  private static final String UV_COOKIE_NAME = "uv_id";

  private RequestUtil() {}

  public static String getClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      String first = xff.split(",")[0].trim();
      if (!first.isBlank()) {
        return first;
      }
    }
    String xri = request.getHeader("X-Real-IP");
    if (xri != null && !xri.isBlank()) {
      return xri.trim();
    }
    return request.getRemoteAddr();
  }

  public static String getOrSetUvId(HttpServletRequest request, HttpServletResponse response) {
    String existing = getCookieValue(request, UV_COOKIE_NAME);
    if (existing != null && !existing.isBlank()) {
      return existing;
    }
    String uvId = UUID.randomUUID().toString().replace("-", "");
    Cookie cookie = new Cookie(UV_COOKIE_NAME, uvId);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(60 * 60 * 24 * 365);
    response.addCookie(cookie);
    return uvId;
  }

  private static String getCookieValue(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null || cookies.length == 0) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(c -> name.equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}

