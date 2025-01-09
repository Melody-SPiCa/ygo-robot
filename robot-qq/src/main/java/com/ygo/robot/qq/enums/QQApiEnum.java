package com.ygo.robot.qq.enums;

/** QQ API 接口枚举 */
public enum QQApiEnum {
  获取调用凭证("https://bots.qq.com/app/getAppAccessToken"),
  //    发动消息到群("https://api.sgroup.qq.com/v2/groups/{group_openid}/messages"),
  发动消息到群("https://sandbox.api.sgroup.qq.com/v2/groups/{group_openid}/messages"),
  获取频道详情("https://sandbox.api.sgroup.qq.com//guilds/{guild_id}"),
  ;

  private final String url;

  QQApiEnum(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
