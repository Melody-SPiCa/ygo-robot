package com.ygo.robot.control.capability;

import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.ygo.robot.control.capability.enums.BotEnum;

public interface RobotCapability {

  default <msg extends MessageEvent> CallbackMsg execute(msg event, BotEnum botEnum) {
    if (can(event)) {
      switch (botEnum) {
        case BotEnum.GO_CQHTTP -> {
          return go_cqhttp(event);
        }
      }
    }

    return null;
  }

  <msg extends MessageEvent> boolean can(msg event);

  <msg extends MessageEvent> CallbackMsg go_cqhttp(msg event);
}
