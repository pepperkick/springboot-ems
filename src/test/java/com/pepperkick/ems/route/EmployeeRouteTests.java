package com.pepperkick.ems.route;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeRouteTests extends AbstractTestNGSpringContextTests {
    @Autowired
    private EmployeeRoute employeeRoute;

    @LocalServerPort
    private int port;

    @BeforeClass
    public void init() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Test
    public void isRouteRunning() throws Exception {
        assertThat(employeeRoute).isNotNull();
    }

    @Test
    public void shouldPingApi() {
        given().
        when().
            get("/employee").
        then().
            assertThat().
            statusCode(200).
        and().
            contentType(ContentType.JSON);
    }

    @Test
    public void shouldGetDirector() {
        int id = 1;

        given().
            pathParam("id", id).
        when().
            get("/employee/{id}").
        then().
            assertThat().
            statusCode(200).
        and().
            contentType(ContentType.JSON).
        and().
            body("id", equalTo(id)).
            body("jobTitle", equalTo("Director"));
    }
}
