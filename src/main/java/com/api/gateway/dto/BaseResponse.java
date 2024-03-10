package com.api.gateway.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
public class BaseResponse<T> {

  private T data;
  private boolean success;
  private String code;
  private String message;

  public BaseResponse(T data, String code, String message) {
    this(data, true, code, message);
  }

  public BaseResponse(T data) {
    this(data, true, "200", "Success");
  }
}
