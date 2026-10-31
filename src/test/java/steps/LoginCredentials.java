package steps;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import utils.SeleniumUtils;

public class LoginCredentials {
    private WebDriver driver = new ChromeDriver();
    SeleniumUtils obj = new SeleniumUtils(driver);

    @Given("I open the login website")
    public void openWebsite1() {
        driver.get("https://www.saucedemo.com/");
    }

    @Then("Enter {string} & {string}")
    public void userPass(String username, String password) {
        //obj.click(By.name("user-name"));
        obj.type(By.name("user-name"), username);
        //obj.click(By.name("password"));
        obj.type(By.name("password"), password);
    }

    @Then("click on submit button")
    public void submit()
    {
        obj.click(By.name("login-button"));
    }

    @After
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}
