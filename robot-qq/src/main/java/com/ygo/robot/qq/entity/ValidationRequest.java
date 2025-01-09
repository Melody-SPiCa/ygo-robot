package com.ygo.robot.qq.entity;

import lombok.Data;

@Data
public class ValidationRequest {

  private String plain_token;

  private String event_ts;
}
