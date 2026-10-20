package steps;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TitleSteps {

    private WebDriver driver;

    @Given("I open the Selenium website")
    public void openWebsite() {
        driver = new ChromeDriver();
        driver.get("https://www.selenium.dev/");
    }

    @Then("the page title contains {string}")
    public void verifyTitle(String expectedText) {
        Assert.assertTrue(driver.getTitle().contains(expectedText));
    }

    @Then("Api response test")
    public void validate() {
        given()
                .baseUri("https://jsonplaceholder.typicode.com")
                .when()
                .get("/todos/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1));
    }

    @After
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}