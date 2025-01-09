package com.ygo.robot.control.capability.enums;

import lombok.Getter;

@Getter
public enum BotEnum {
  GO_CQHTTP(1, "go-http"),
  QQ(2, "qq官方bot"),
  ;

  private final int value;

  private final String description;

  BotEnum(int value, String description) {
    this.value = value;
    this.description = description;
  }
}
