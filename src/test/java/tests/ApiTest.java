package tests;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ApiTest {

    @Test
    public void verifyTodo() {
        given()
                .baseUri("https://jsonplaceholder.typicode.com")
                .when()
                .get("/todos/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1));
    }
}