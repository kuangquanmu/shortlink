package com.wanpu.myshortlink.utils;


import cn.hutool.core.lang.hash.MurmurHash;

public class HashUtil {
  private static final String BASE62 =
      "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  public static String hashToBase62(String str){
    int hash32 = MurmurHash.hash32(str);
    Long ul = Integer.toUnsignedLong(hash32);
    return toBase62(ul);
  }
  private static String toBase62(Long ul){
    StringBuilder sb = new StringBuilder();
    while (ul != 0){
      sb.append(BASE62.charAt((int)(ul % 62)));
      ul = ul / 62;
    }
    return sb.reverse().toString();
  }
}
