package com.ygo.robot.qq.api;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ygo.robot.qq.config.BotConfig;
import com.ygo.robot.qq.entity.CustomPrivateKey;
import com.ygo.robot.qq.entity.CustomPublicKey;
import com.ygo.robot.qq.entity.Payload;
import com.ygo.robot.qq.entity.ValidationRequest;
import com.ygo.robot.qq.entity.ValidationResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.util.HexFormat;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
public class BotController {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static boolean verifySignature(
      String signatureHex, String timestamp, byte[] httpBody, byte[] publicKeyBytes) {
    try {
      // 解码签名
      byte[] sig = HexFormat.of().parseHex(signatureHex);
      // 检查签名长度和格式
      if (sig.length != 64 || (sig[63] & 0xE0) != 0) {
        log.warn("Invalid signature format");
        return false;
      }
      // 组成签名体
      ByteArrayOutputStream msg = new ByteArrayOutputStream();
      msg.write(timestamp.getBytes(StandardCharsets.UTF_8));
      msg.write(httpBody);
      Ed25519Signer verifier = new Ed25519Signer();
      verifier.init(false, new Ed25519PublicKeyParameters(publicKeyBytes, 0));
      verifier.update(msg.toByteArray(), 0, msg.size());
      return verifier.verifySignature(sig);
    } catch (Exception e) {
      log.error("验证签名报错：", e);
      return false;
    }
  }

  private static String prepareSeed(String seed) {
    if (seed.length() < 32) seed = seed.repeat(2);
    return seed.substring(0, 32);
  }

  private static KeyPair generateEd25519KeyPair(byte[] seed) {
    Ed25519KeyPairGenerator generator = new Ed25519KeyPairGenerator();
    generator.init(new KeyGenerationParameters(null, 32));
    // 使用种子初始化私钥参数
    Ed25519PrivateKeyParameters privateKeyParams = new Ed25519PrivateKeyParameters(seed, 0);
    // 从私钥参数中提取公钥参数
    Ed25519PublicKeyParameters publicKeyParams = privateKeyParams.generatePublicKey();
    // 将参数转换为字节数组
    byte[] privateKeyBytes = privateKeyParams.getEncoded();
    byte[] publicKeyBytes = publicKeyParams.getEncoded();
    return new KeyPair(new CustomPublicKey(publicKeyBytes), new CustomPrivateKey(privateKeyBytes));
  }

  private static byte[] signMessage(PrivateKey privateKey, byte[] message) {
    Ed25519Signer signer = new Ed25519Signer();
    signer.init(true, new Ed25519PrivateKeyParameters(privateKey.getEncoded(), 0));
    signer.update(message, 0, message.length);
    return signer.generateSignature();
  }

  @PostMapping("/webhook")
  public ResponseEntity<?> webhook(
      @RequestBody String rawBody,
      @RequestHeader("X-Signature-Ed25519") String sig,
      @RequestHeader("X-Signature-Timestamp") String timestamp) {
    log.info("webhook message: {}", rawBody);
    try {
      String seed = prepareSeed(BotConfig.APP_SECRET);
      KeyPair keyPair = generateEd25519KeyPair(seed.getBytes(StandardCharsets.UTF_8));
      Payload<?> payload = JSONUtil.toBean(rawBody, Payload.class);
      switch (payload.getOp()) {
        case 0 -> {
          boolean isValid =
              verifySignature(
                  sig,
                  timestamp,
                  rawBody.getBytes(StandardCharsets.UTF_8),
                  keyPair.getPublic().getEncoded());
          return ResponseEntity.ok(isValid);
        }
        case 13 -> {
          // 验证签名
          log.info("验证有效性...");
          ValidationRequest validationPayload =
              objectMapper.convertValue(payload.getD(), ValidationRequest.class);
          byte[] message =
              (validationPayload.getEvent_ts() + validationPayload.getPlain_token())
                  .getBytes(StandardCharsets.UTF_8);
          byte[] signature = signMessage(keyPair.getPrivate(), message);
          ValidationResponse resp =
              new ValidationResponse(
                  validationPayload.getPlain_token(), HexFormat.of().formatHex(signature));

          log.info("webhook sign response: {}", resp);
          return ResponseEntity.ok(resp);
        }
        default -> log.info("未处理操作：" + JSONUtil.toJsonStr(payload));
      }
    } catch (Exception e) {
      log.error("验证失败：", e);
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
  }
}
