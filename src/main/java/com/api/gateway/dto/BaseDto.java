package com.api.gateway.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract class BaseDto {

  private String createdBy;

  private LocalDateTime createdDate;

  private String lastModifiedBy;

  private LocalDateTime lastModifiedDate;

  private String entityId;
}
