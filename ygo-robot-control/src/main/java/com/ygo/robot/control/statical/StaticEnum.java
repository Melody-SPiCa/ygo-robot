package com.ygo.robot.control.statical;

import com.ygo.robot.control.capability.enums.CapabilityEnum;
import java.io.File;
import lombok.Getter;

@Getter
public enum StaticEnum {
  陨石("img" + File.separator + "原始生命态 未来龙皇.gif", CapabilityEnum.事件_陨石.getValue());

  private final String url;

  private final Integer[] values;

  StaticEnum(String url, Integer... values) {
    this.url = url;
    this.values = values;
  }

  public static String getLocalPath() {
    String os = System.getProperty("os.name");
    String user = System.getProperty("user.name");
    if (os.startsWith("Mac OS")) {
      return System.getProperty("user.home") + "/temp/";
    } else {
      return os.startsWith("Windows") ? "C:\\temp\\static\\" : String.format("/%s/temp/", user);
    }
  }

  public String getPath() {
    return getLocalPath() + url;
  }
}
