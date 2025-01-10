package com.ygo.robot.control.utils;

import com.ygo.robot.control.statical.StaticEnum;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebUtil {

  private static String chromeDriverPath = null;

  @SneakyThrows
  public static void main(String[] args) {
    List<String> linksFromBody =
        WebUtil.getCardLink("https://ygocdb.com/?search=%E9%9D%92%E7%9C%BC%E7%99%BD%E9%BE%99");

    for (String url : linksFromBody) {
      System.out.println(url);
    }
  }

  public static File toImg(String url) {
    // 设置 ChromeDriver 路径
    String chromeDriverPath = getChromeDriverPath();
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
      driver.get(url);

      // 等待页面加载完成
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

      // 滚动页面到底部并截取整个页面的截图
      return takeFullPageScreenshot(driver);
    } finally {
      // 关闭浏览器
      driver.quit();
    }
  }

  public static List<String> getCardLink(String url) {

    // 卡片样式 div class
    String cardDivClass = "div.col-md-3.col-xs-4.cardimg";

    // 设置 ChromeDriver 路径
    String chromeDriverPath = getChromeDriverPath();
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
      driver.get(url);

      // 等待页面加载完成
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

      // 查找第一个 class 为 row card result 的 div
      List<WebElement> divs = driver.findElements(By.cssSelector(cardDivClass));
      if (divs.isEmpty()) {
        return new ArrayList<>();
      }

      WebElement firstDiv = divs.getFirst();

      // 获取该 div 中的所有链接
      List<WebElement> links = firstDiv.findElements(By.tagName("a"));
      List<String> linkUrls = new ArrayList<>();
      for (WebElement link : links) {
        String href = link.getAttribute("href");
        if (href != null) {
          linkUrls.add(href);
        }
      }

      return linkUrls;
    } finally {
      // 关闭浏览器
      driver.quit();
    }
  }

  @SneakyThrows
  private static File takeFullPageScreenshot(WebDriver driver) {
    JavascriptExecutor js = (JavascriptExecutor) driver;

    // 获取页面宽度和高度
    long pageWidth = (Long) js.executeScript("return document.body.offsetWidth");
    long pageHeight = (Long) js.executeScript("return document.body.scrollHeight");

    // 设置浏览器窗口大小
    driver.manage().window().setSize(new Dimension((int) pageWidth, (int) pageHeight));

    // 滚动页面到底部
    // js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

    // 等待页面加载更多内容
    Thread.sleep(2000);

    // 截取整个页面的截图
    TakesScreenshot screenshot = (TakesScreenshot) driver;

    return screenshot.getScreenshotAs(OutputType.FILE);
  }

  private static String getChromeDriverPath() {
    if (chromeDriverPath == null) {
      String osName = System.getProperty("os.name");
      if (Pattern.matches(".*Windows.*", osName)) {
        chromeDriverPath = StaticEnum.浏览器驱动_win64.getPath();
      } else if (Pattern.matches(".*Mac OS.*", osName)) {
        chromeDriverPath = StaticEnum.浏览器驱动_win64.getPath();
      } else {
        chromeDriverPath = StaticEnum.浏览器驱动_linux64.getPath();
      }
    }

    return chromeDriverPath;
  }
}
