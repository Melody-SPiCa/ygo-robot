package com.ygo.robot.control.capability;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

/** 事件指令的消息回调 */
@Getter
public class CallbackMsg {

  /** 消息体 */
  List<Msg> msgList = new LinkedList<>();

  /**
   * 构建回调消息
   *
   * @param msgs message array
   * @return callback
   */
  public static <msg extends Msg> CallbackMsg of(msg... msgs) {
    CallbackMsg callbackMsg = new CallbackMsg();
    callbackMsg.msgList.addAll(Arrays.asList(msgs));
    return callbackMsg;
  }

  /**
   * 构建回调消息
   *
   * @param groupId 群
   * @param msg 消息内容
   * @return callback
   */
  public static CallbackMsg ofGroup(long groupId, String msg) {
    return ofGroup(groupId, msg, false);
  }

  /**
   * 构建回调消息
   *
   * @param groupId 群
   * @param msg 消息内容
   * @param autoEscape 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效
   * @return callback
   */
  public static CallbackMsg ofGroup(long groupId, String msg, boolean autoEscape) {
    CallbackMsg callbackMsg = new CallbackMsg();
    val groupMsg = new GroupMsg();
    groupMsg.setGroupId(groupId);
    groupMsg.setMsg(msg);
    groupMsg.setAutoEscape(autoEscape);
    callbackMsg.msgList.add(groupMsg);
    return callbackMsg;
  }

  /**
   * 添加一个消息
   *
   * @param msg message
   */
  public <msg extends Msg> void add(msg msg) {
    msgList.add(msg);
  }

  @Data
  public static class Msg {

    String msg;

    Boolean autoEscape;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class GroupMsg extends Msg {

    Long groupId;

    Long userId;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class SingleMsg extends Msg {

    Long groupId;

    Long userId;
  }
}
