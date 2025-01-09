package com.ygo.robot.control.capability.enums;

import com.ygo.robot.control.capability.RobotCapability;
import lombok.Getter;

@Getter
public enum CapabilityEnum {
  事件_复读(1010, "同一连锁上全是复数次同名消息的通常发言发动的场合，那个连锁3以后才能发动，这个 bot 消息直到发言结束阶段变成和上一个通常发言发动时的消息相同。", null),
  事件_陨石(1011, "同一连锁上全是复数次同名消息的通常发言发动的场合，那个连锁5才能发动，从卡组外把一只[原始生命态 未来龙皇]特殊召唤", null),
  事件_休息(1020, "在自己·对方休息阶段概率发动，饮茶先啦。", null),
  事件_冒泡(1021, "在自己·对方休息阶段概率发动，宣言1张[未来龙皇]卡。并且，可以再从卡组外把一只[未来龙皇]怪兽加入手卡或特殊召唤", null),

  指令_堆墓(2060, "从卡组外把3张卡送去墓地。", null),
  ;

  /** 能力集代码 */
  private final int value;

  /** 能力描述 */
  private final String description;

  /** 能力集的具体实现 */
  private final RobotCapability cap;

  CapabilityEnum(int value, String description, RobotCapability cap) {
    this.value = value;
    this.description = description;
    this.cap = cap;
  }
}
