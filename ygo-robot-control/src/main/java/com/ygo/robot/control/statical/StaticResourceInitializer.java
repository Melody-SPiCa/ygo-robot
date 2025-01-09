package com.ygo.robot.control.statical;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StaticResourceInitializer {

  private static final String DIR = "static" + File.separator;
  private static final String MD5_MAP_FILE = "md5_map.txt";

  @PostConstruct
  public void init() {
    new Thread(
            () -> {
              String localPath = StaticEnum.getLocalPath();
              File localDir = new File(localPath);
              if (!localDir.exists()) {
                boolean mkdirs = localDir.mkdirs();
                if (!mkdirs) {
                  log.error("【初始化 静态资源加载】文件目录创建失败");
                }
              }

              Map<String, String> resourceMd5Map = getResourceMd5Map();
              Map<String, String> localMd5Map = getLocalMd5Map(localPath);

              // Compare and update local files
              updateLocalFiles(localPath, resourceMd5Map, localMd5Map);
            })
        .start();
  }

  private Map<String, String> getResourceMd5Map() {
    Map<String, String> md5Map = new HashMap<>();
    try {
      Path resourcePath =
          Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(DIR)).toURI());
      Files.walkFileTree(
          resourcePath,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              String relativePath = resourcePath.relativize(file).toString();
              String md5 = getFileMd5(file.toFile());
              md5Map.put(relativePath, md5);
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (Exception e) {
      log.error("【初始化 静态资源加载】获取 resource 文件 md5 异常", e);
    }
    return md5Map;
  }

  private Map<String, String> getLocalMd5Map(String localPath) {
    Map<String, String> md5Map = new HashMap<>();
    File md5File = new File(localPath, MD5_MAP_FILE);
    if (md5File.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(md5File))) {
        String line;
        while ((line = reader.readLine()) != null) {
          String[] parts = line.split("=");
          if (parts.length == 2) {
            md5Map.put(parts[0], parts[1]);
          }
        }
      } catch (IOException e) {
        log.error("【初始化 静态资源加载】获取本地静态文件 md5 异常", e);
      }
    }
    return md5Map;
  }

  private void updateLocalFiles(
      String localPath, Map<String, String> resourceMd5Map, Map<String, String> localMd5Map) {
    int deletedCount = 0;
    int addedCount = 0;
    int updatedCount = 0;

    StringBuilder changeLog = new StringBuilder();

    // delete extra files in local directory
    for (String localFile : localMd5Map.keySet()) {
      if (!resourceMd5Map.containsKey(localFile)) {
        File fileToDelete = new File(localPath, localFile);
        if (fileToDelete.delete()) {
          changeLog.append("Deleted: ").append(localFile).append("\n");
          deletedCount++;
        }
      }
    }

    // add or update files in local directory
    for (Map.Entry<String, String> entry : resourceMd5Map.entrySet()) {
      String relativePath = entry.getKey();
      String resourceMd5 = entry.getValue();
      File localFile = new File(localPath, relativePath);

      // ensure the parent directory exists
      File parentDir = localFile.getParentFile();
      if (!parentDir.exists() && !parentDir.mkdirs()) {
        log.error("Failed to create directory: {}", parentDir.getAbsolutePath());
        continue;
      }

      if (!localFile.exists()) {
        try (InputStream rs = getClass().getClassLoader().getResourceAsStream(DIR + relativePath);
            OutputStream ls = new FileOutputStream(localFile)) {
          byte[] buffer = new byte[1024];
          int bytesRead;
          if (rs != null) {
            while ((bytesRead = rs.read(buffer)) != -1) {
              ls.write(buffer, 0, bytesRead);
            }
          }
          changeLog.append("Added: ").append(relativePath).append("\n");
          addedCount++;
        } catch (IOException e) {
          log.error("【初始化 静态资源加载】Error while adding file: {}", relativePath, e);
        }
      } else if (!localMd5Map.getOrDefault(relativePath, "").equals(resourceMd5)) {
        try (InputStream rs = getClass().getClassLoader().getResourceAsStream(DIR + relativePath);
            OutputStream ls = new FileOutputStream(localFile)) {
          if (rs == null) {
            throw new FileNotFoundException("Resource not found: " + relativePath);
          }
          byte[] buffer = new byte[1024];
          int bytesRead;
          while ((bytesRead = rs.read(buffer)) != -1) {
            ls.write(buffer, 0, bytesRead);
          }
          changeLog.append("Updated: ").append(relativePath).append("\n");
          updatedCount++;
        } catch (IOException e) {
          log.error("【初始化 静态资源加载】Error while updating file: {}", relativePath, e);
        }
      }
    }

    // Save the new md5 map to the local file
    saveMd5Map(localPath, resourceMd5Map);

    log.info(
        "【初始化 静态资源加载】\nDeleted files: {}\nAdded files: {}\nUpdated files: {}\nChanges:\n{}",
        deletedCount,
        addedCount,
        updatedCount,
        changeLog.toString().trim());
  }

  private void saveMd5Map(String localPath, Map<String, String> md5Map) {
    File md5File = new File(localPath, MD5_MAP_FILE);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(md5File))) {
      for (Map.Entry<String, String> entry : md5Map.entrySet()) {
        writer.write(entry.getKey() + "=" + entry.getValue());
        writer.newLine();
      }
    } catch (IOException e) {
      log.error("【初始化 静态资源加载】添加文件 md5 映射表失败");
    }
  }

  private String getFileMd5(File file) {
    try (FileInputStream fis = new FileInputStream(file)) {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = fis.read(buffer)) != -1) {
        md.update(buffer, 0, bytesRead);
      }
      byte[] hashBytes = md.digest();
      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (IOException | NoSuchAlgorithmException e) {
      log.error("【初始化 静态资源加载】获取文件 md5 失败", e);
      return null;
    }
  }
}
