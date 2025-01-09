package com.ygo.robot.qq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BotConfig {

  public static String APPID;

  public static String TOKEN;

  public static String APP_SECRET;

  @Value("${bot.appid:102077153}")
  public void setAPPID(String APPID) {
    BotConfig.APPID = APPID;
  }

  @Value("${bot.token:oUXesmbwT32V4Y09BHEWahcfYsNPwrij}")
  public void setTOKEN(String TOKEN) {
    BotConfig.TOKEN = TOKEN;
  }

  @Value("${bot.secret:skcVOHA3wpjdXRLF93ytojeZUQMIEA62}")
  public void setAppSecret(String APP_SECRET) {
    BotConfig.APP_SECRET = APP_SECRET;
  }
}
