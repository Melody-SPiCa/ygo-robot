package com.ygo.robot.control.statical;

import com.ygo.robot.control.capability.enums.CapabilityEnum;
import java.io.File;
import lombok.Getter;

@Getter
public enum StaticEnum {
  陨石("img" + File.separator + "原始生命态 未来龙皇.gif", CapabilityEnum.事件_陨石.getValue()),

  浏览器驱动_win64("driver" + File.separator + "win64", CapabilityEnum.指令_查卡.getValue()),
  浏览器驱动_linux64("driver" + File.separator + "linux64", CapabilityEnum.指令_查卡.getValue()),
  ;

  private final String url;

  private final Integer[] values;

  private static String LOCAL_PATH = null;

  StaticEnum(String url, Integer... values) {
    this.url = url;
    this.values = values;
  }

  public static String getLocalPath() {
    if (LOCAL_PATH == null) {
      String os = System.getProperty("os.name");
      String user = System.getProperty("user.name");
      if (os.startsWith("Mac OS")) {
        LOCAL_PATH = System.getProperty("user.home") + "/ygo-robot/";
      } else {
        String windows = "C:\\ygo-robot\\static\\";
        String linux_path = String.format("/%s/ygo-robot/", user);
        LOCAL_PATH = os.startsWith("Windows") ? windows : linux_path;
      }
    }
    return LOCAL_PATH;
  }

  public static String getTempPath() {
    return getLocalPath() + File.separator + "temp" + File.separator;
  }

  public String getPath() {
    return getLocalPath() + url;
  }
}
