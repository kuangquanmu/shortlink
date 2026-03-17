package com.wanpu.myshortlink.common.exceptions;

import com.wanpu.myshortlink.common.result.ResultCode;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
  private final String code;
  private final String message;
 public ServiceException(ResultCode resultCode){
   super(resultCode.getMessage());
   this.code = resultCode.getCode();
   this.message = resultCode.getMessage();
 }
 public ServiceException(String code,String message){
   super(message);
   this.code = code;
   this.message = message;
 }
}
