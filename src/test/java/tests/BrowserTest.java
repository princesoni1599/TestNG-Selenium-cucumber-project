package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BrowserTest {

    @Test
    public void verifyPageTitle() {
        WebDriver driver = new ChromeDriver();

        try {
            driver.get("https://www.selenium.dev/");
            Assert.assertTrue(driver.getTitle().contains("Selenium"));
        } finally {
            driver.quit();
        }
    }
}