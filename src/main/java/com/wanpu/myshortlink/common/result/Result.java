package com.wanpu.myshortlink.common.result;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

  private String  code;
  private String message;
  private T data;

  public static <T> Result<T> success(T data) {
    return new Result<T>().setCode(ResultCode.SUCCESS.getCode()).setMessage(ResultCode.SUCCESS.getMessage()).setData(data);
  }
  public static <T> Result<T> success() {
    return success(null);
  }
  public static <T> Result<T> fail(ResultCode resultCode) {
    return fail(resultCode.getCode(),resultCode.getMessage());
  }
  public static <T> Result<T> fail(String code, String message) {
    return  new Result<T>().setCode(code).setMessage(message).setData(null);
  }
}
