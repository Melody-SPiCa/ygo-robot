package com.ygo.robot.control.capability;

import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.ygo.robot.control.capability.enums.BotEnum;

public abstract class RobotCapability {

  public final <msg extends MessageEvent> CallbackMsg execute(msg event, BotEnum botEnum) {
    if (can(event)) {
      switch (botEnum) {
        case BotEnum.GO_CQHTTP -> {
          return go_cqhttp(event);
        }
      }
    }

    return null;
  }

  public abstract <msg extends MessageEvent> boolean can(msg event);

  public abstract <msg extends MessageEvent> CallbackMsg go_cqhttp(msg event);

  public CallbackMsg postProcess() {
    return null;
  }

  public String matchesRegex() {
    return null;
  }

  public Runnable hook() {
    return null;
  }
}
