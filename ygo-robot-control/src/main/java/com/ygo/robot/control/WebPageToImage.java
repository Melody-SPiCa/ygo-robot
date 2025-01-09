package com.ygo.robot.control;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebPageToImage {
  public static void main(String[] args) throws IOException, InterruptedException {
    // 设置 ChromeDriver 路径
    String chromeDriverPath = "C:\\Users\\11393\\Downloads\\chromedriver-win64\\chromedriver.exe";
    System.setProperty("webdriver.chrome.driver", chromeDriverPath);

    // 创建 ChromeDriverService
    ChromeDriverService service =
        new ChromeDriverService.Builder().usingDriverExecutable(new File(chromeDriverPath)).build();

    // 创建 ChromeOptions 并启用无头模式
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless"); // 启用无头模式
    options.addArguments("--disable-gpu"); // 禁用 GPU 加速
    options.addArguments("--window-size=1920,1080"); // 设置窗口大小

    // 创建 WebDriver
    WebDriver driver = new ChromeDriver(service, options);

    try {
      // 打开目标网页
      String url = "https://www.baidu.com/";
      driver.get(url);

      // 等待页面加载完成
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

      // 滚动页面到底部并截取整个页面的截图
      File screenshotFile = takeFullPageScreenshot(driver);
      FileUtils.copyFile(screenshotFile, new File("screenshot.png")); // 保存截屏图片

    } finally {
      // 关闭浏览器
      driver.quit();
    }
  }

  @SneakyThrows
  private static File takeFullPageScreenshot(WebDriver driver) throws IOException {
    JavascriptExecutor js = (JavascriptExecutor) driver;

    // 获取页面宽度和高度
    long pageWidth = (Long) js.executeScript("return document.body.offsetWidth");
    long pageHeight = (Long) js.executeScript("return document.body.scrollHeight");

    // 设置浏览器窗口大小
    driver.manage().window().setSize(new Dimension((int) pageWidth, (int) pageHeight));

    // 滚动页面到底部
    //        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

    // 等待页面加载更多内容
    Thread.sleep(2000);

    // 截取整个页面的截图
    TakesScreenshot screenshot = (TakesScreenshot) driver;

    return screenshot.getScreenshotAs(OutputType.FILE);
  }
}
