package com.ygo.robot.control.capability.set;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.ygo.robot.control.capability.CallbackMsg;
import com.ygo.robot.control.capability.RobotCapability;
import com.ygo.robot.control.statical.StaticEnum;
import com.ygo.robot.control.utils.WebUtil;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

/** {@link com.ygo.robot.control.capability.enums.CapabilityEnum#指令_查卡} */
@Slf4j
@Service
public class Capability2010 extends RobotCapability {

  /** 线程缓存 */
  private final ThreadLocal<Cache> searchTask = ThreadLocal.withInitial(() -> null);
  /**
   * 是否正在查询中
   *
   * <p>运行时只运行一人可以同时查询，防止大量查询影响第三方服务器
   */
  private volatile boolean isInSearch = false;

  @Override
  public <msg extends MessageEvent> boolean can(msg event) {
    if (event instanceof GroupMessageEvent)
      return Pattern.compile(matchesRegex()).matcher(event.getMessage()).matches();
    else return false;
  }

  @Override
  public <msg extends MessageEvent> CallbackMsg go_cqhttp(msg event) {
    GroupMessageEvent receiveEvent = (GroupMessageEvent) event;
    val groupId = receiveEvent.getGroupId();
    val receiveMsg = receiveEvent.getMessage();

    String reply = "不许查";
    if (!isInSearch) {
      Pattern pattern = Pattern.compile(matchesRegex());
      Matcher matcher = pattern.matcher(receiveMsg);
      if (matcher.matches()) {
        isInSearch = true;
        reply = "小蓝查询中...";

        searchTask.set(Cache.of(groupId, matcher.group(1).trim()));
      }
    }

    return CallbackMsg.ofGroup(groupId, MsgUtils.builder().text(reply).build());
  }

  @Override
  public CallbackMsg postProcess() {
    Cache search = searchTask.get();
    if (search == null) {
      return null;
    }

    val groupId = search.groupId;
    val text = search.search;

    try {
      val img = WebUtil.toImg(Constant.SEARCH_HOST + text);

      FileUtils.copyFile(img, new File(Constant.SEARCH_PATH));

      return CallbackMsg.ofGroup(groupId, MsgUtils.builder().img(Constant.SEARCH_PATH).build());
    } catch (Exception e) {
      log.error("【Capability-2010】卡片查询失败");
      return CallbackMsg.ofGroup(groupId, MsgUtils.builder().text("小蓝查不到...").build());
    } finally {
      isInSearch = false;
    }
  }

  @Override
  public String matchesRegex() {
    return "^小蓝查卡(?!调整|图|裁定|详情)(.{0,64})$";
  }

  @Override
  public Runnable hook() {
    return () -> {
      File file = new File(Constant.SEARCH_PATH);
      if (file.exists()) if (!file.delete()) log.error("【Capability-2010】文件删除失败");
    };
  }

  /** 基本常量 */
  private static class Constant {

    /** 卡片模糊查询服务器 */
    private static final String SEARCH_HOST = "https://ygocdb.com/?search=";

    /** 查询信息本地存放路径 */
    private static final String SEARCH_PATH = StaticEnum.getTempPath() + "v2010-search.png";
  }

  /** 线程缓存 */
  private static class Cache {

    /** 查询内容 */
    private String search;

    /** 检索群号 */
    private long groupId;

    /** 快速构建缓存体 */
    public static Cache of(long groupId, String search) {
      Cache cache = new Cache();
      cache.groupId = groupId;
      cache.search = search;
      return cache;
    }
  }
}
