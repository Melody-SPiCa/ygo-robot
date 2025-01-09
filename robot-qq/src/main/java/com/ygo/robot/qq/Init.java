package com.ygo.robot.qq;

import cn.hutool.http.HttpRequest;
import com.ygo.robot.qq.enums.QQApiEnum;
import org.springframework.stereotype.Service;

@Service
public class Init {

  public static void main(String[] args) {
    //
    //        String result =  HttpRequest.post(QQApiEnum.获取调用凭证.getUrl())
    //                .body(
    //                        new JSONObject(
    //                                Map.of("appId","102077153",
    //                                        "clientSecret","skcVOHA3wpjdXRLF93ytojeZUQMIEA62"
    //
    //                                )
    //                        ).toString()
    //                )
    //                .timeout(20000)//超时，毫秒
    //                .execute().body();
    //
    //        System.out.println(result);

    String token = "GLZQcOecAPA7wpThLH7f9iqT-N3OokLMRiyCZSuQM7KgQhbXNTIgWqv4ohvvfHDCNASUmOnp1GLNbA";
    //        String result =
    // HttpRequest.post(QQApiEnum.发动消息到群.getUrl().replace("{group_openid}","608750048"))
    //                .header("Authorization", "QQBot
    // {ACCESS_TOKEN}".replace("{ACCESS_TOKEN}",token))
    //                .body(
    //                        new JSONObject(
    //                                Map.of("content","你好",
    //                                        "msg_type","0"
    //
    //                                )
    //                        ).toString()
    //                )
    //                .timeout(20000)//超时，毫秒
    //                .execute().body();
    //        System.out.println(result);

    String result =
        HttpRequest.get(QQApiEnum.获取频道详情.getUrl().replace("{guild_id}", "utqlnq0l3n"))
            .header("Authorization", "QQBot {ACCESS_TOKEN}".replace("{ACCESS_TOKEN}", token))
            .timeout(20000) // 超时，毫秒
            .execute()
            .body();
    System.out.println(result);
  }
}
