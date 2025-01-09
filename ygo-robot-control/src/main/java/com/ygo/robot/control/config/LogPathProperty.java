package com.ygo.robot.control.config;

import ch.qos.logback.core.PropertyDefinerBase;

public class LogPathProperty extends PropertyDefinerBase {
  public LogPathProperty() {}

  public String getPropertyValue() {
    String osName = System.getProperty("os.name");
    String userName = System.getProperty("user.name");
    if (osName.startsWith("Mac OS")) {
      return System.getProperty("user.home") + "/logs";
    } else {
      return osName.startsWith("Windows") ? "logs" : String.format("/%s/logs", userName);
    }
  }
}
