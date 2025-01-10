package com.ygo.robot.control.capability.set;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.ygo.robot.control.capability.CallbackMsg;
import com.ygo.robot.control.capability.RobotCapability;
import com.ygo.robot.control.statical.StaticEnum;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.val;
import org.springframework.stereotype.Service;

/** {@link com.ygo.robot.control.capability.enums.CapabilityEnum#事件_陨石} */
@Service
public class Capability1011 extends RobotCapability {

  private static final Map<String, MsgCounter> groupMessageMap = new ConcurrentHashMap<>();

  @Override
  public <msg extends MessageEvent> boolean can(msg event) {
    if (!(event instanceof GroupMessageEvent receiveEvent)) return false;

    val groupId = receiveEvent.getGroupId().toString();

    val msgCounter = groupMessageMap.computeIfAbsent(groupId, k -> new MsgCounter());
    return msgCounter.add(receiveEvent.getArrayMsg());
  }

  @Override
  public <msg extends MessageEvent> CallbackMsg go_cqhttp(msg event) {
    GroupMessageEvent receiveEvent = (GroupMessageEvent) event;

    val groupId = receiveEvent.getGroupId();

    return CallbackMsg.ofGroup(groupId, MsgUtils.builder().img(StaticEnum.陨石.getPath()).build());
  }

  /** 重复消息的计数器 */
  protected static class MsgCounter {

    /** 到达触发任务的重复消息次数 */
    private static final Integer TRIGGER_COUNT = 5;

    /** 当前消息 */
    private List<ArrayMsg> msg;

    /** 该消息的重复次数 */
    private int repeatCount;

    /**
     * 添加并计算消息重复次数
     *
     * @param receiveMsg 接受的消息
     * @return 是否达到重复阈值
     */
    protected boolean add(List<ArrayMsg> receiveMsg) {
      if (receiveMsg == null || msg == null || receiveMsg.size() != msg.size()) {
        reset(receiveMsg);
        return false;
      }

      for (int i = 0; i < receiveMsg.size(); i++) {
        if (!areMessagesEqual(receiveMsg.get(i), msg.get(i))) {
          reset(receiveMsg);
          return false;
        }
      }

      repeatCount++;
      return repeatCount == TRIGGER_COUNT - 1;
    }

    private boolean areMessagesEqual(ArrayMsg a1, ArrayMsg a2) {
      val typeSet = Set.of(MsgTypeEnum.at, MsgTypeEnum.text, MsgTypeEnum.image);

      if (!typeSet.contains(a1.getType())) {
        return false;
      }
      if (a1.getType() != a2.getType()) {
        return false;
      }

      return switch (a1.getType()) {
        case MsgTypeEnum.text -> a1.getData().get("text").equals(a2.getData().get("text"));
        case MsgTypeEnum.at -> a1.getData().get("qq").equals(a2.getData().get("qq"));
        case MsgTypeEnum.image -> a1.getData().get("file").equals(a2.getData().get("file"));
        default -> false;
      };
    }

    /**
     * 重置消息计数器
     *
     * @param receiveMsg 接受的消息
     */
    private void reset(List<ArrayMsg> receiveMsg) {
      msg = receiveMsg;
      repeatCount = 0;
    }
  }
}
