package com.ygo.robot.control.plugin;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.ygo.robot.control.capability.CallbackMsg;
import com.ygo.robot.control.capability.RobotCapability;
import com.ygo.robot.control.capability.enums.BotEnum;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

@Slf4j
@Shiro
@Component
public class ExamplePlugin {

  @Resource private List<RobotCapability> robotCapabilitieList;

  /**
   * 如果 at 参数设定为 AtEnum.NEED 则只有 at 了机器人的消息会被响应
   *
   * @param event 群消息
   */
  @GroupMessageHandler
  @MessageHandlerFilter(at = AtEnum.NEED)
  public void fun2(GroupMessageEvent event) {
    System.out.println(event.getMessage());
  }

  // 当机器人收到的私聊消息消息符合 cmd 值 "hi" 时，这个方法会被调用。
  @GroupMessageHandler
  //    @MessageHandlerFilter(cmd = "^小蓝(.*)")
  @MessageHandlerFilter
  public void fun1(Bot bot, GroupMessageEvent event) {
    System.out.println(1);

    for (RobotCapability robotCapability : robotCapabilitieList) {
      val callbackMsg = robotCapability.execute(event, BotEnum.GO_CQHTTP);
      if (callbackMsg != null)
        for (CallbackMsg.Msg msg : callbackMsg.getMsgList()) {
          log.debug("fun1 callback reply msg: {}", msg.toString());

          if (msg instanceof CallbackMsg.GroupMsg gmsg)
            bot.sendGroupMsg(gmsg.getGroupId(), gmsg.getMsg(), gmsg.getAutoEscape());
          else if (msg instanceof CallbackMsg.SingleMsg smsg)
            bot.sendPrivateMsg(smsg.getUserId(), smsg.getMsg(), smsg.getAutoEscape());
        }
    }
  }
}
