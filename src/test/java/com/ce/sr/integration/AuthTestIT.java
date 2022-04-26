package com.ce.sr.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.ce.sr.models.Role;
import com.ce.sr.payload.request.LoginRequest;
import com.ce.sr.payload.request.SignupRequest;
import com.ce.sr.payload.response.UserInfoResponse;
import com.ce.sr.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Repeat;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.http.Cookie;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
public class AuthTestIT extends IT {

    private Cookie auth_token = null;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    private String userId;
    Random rand = new Random();

    String username = "test" + rand.nextInt();
    String email = "test" + rand.nextInt() + "@email.com";
    String password = "password" + rand.nextInt();

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
        RestAssuredMockMvc.mockMvc(mockMvc);
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        SignupRequest request = new SignupRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setRoles(roles);

        RestAssuredMockMvc.given().header("Content-Type", "application/json")
                .body(request)
                .when().post("/api/auth/signup")
                .then().log().ifError()
                .statusCode(200).contentType("application/json");
    }

    @AfterEach
    public void clean() {
        if (auth_token != null)
            RestAssuredMockMvc
                    .given().cookie(auth_token)
                    .when().post("/api/auth/signout")
                    .then().assertThat().statusCode(200)
                    .and().assertThat().body("message", equalTo("You've been logout!"));
        if (userId != null)
            userRepository.deleteById(userId);
    }

    @Test
    public void signInTestIT() {
        MockMvcResponse response = RestAssuredMockMvc.given().header("Content-Type", "application/json")
                .body(new LoginRequest(username, password))
                .when().post("/api/auth/signin")
                .then().log().ifError()
                .statusCode(200).contentType("application/json").cookie("Cookie").extract().response();

        auth_token = response.detailedCookie("Cookie");
        userId = response.as(UserInfoResponse.class).getId();
    }

    @Test
    public void signInBadCredentialsTestIT() {

        String user = "badcredentials";

        RestAssuredMockMvc.given().header("Content-Type", "application/json")
                .body(new LoginRequest(user, "badcredentials1"))
                .when().post("/api/auth/signin")
                .then().assertThat().statusCode(403)
                .and().assertThat().body("message", equalTo("Bad credentials " + user));
    }

    @Test
    public void signUpUserExistsTestIT() {

        SignupRequest request = new SignupRequest();
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        request.setUsername(username);
        request.setEmail("test" + rand.nextInt() + "@email.com");
        request.setPassword("password1");
        request.setRoles(roles);

        MockMvcResponse response = RestAssuredMockMvc.given().header("Content-Type", "application/json")
                .body(request)
                .when().post("/api/auth/signup")
                .then().assertThat().statusCode(400)
                .and().assertThat().body("message", equalTo("Username exists!")).extract().response();
        auth_token = response.detailedCookie("Cookie");
    }

    @Test
    public void signUpEmailExistsTestIT() {

        SignupRequest request = new SignupRequest();
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        request.setUsername("test" + rand.nextInt());
        request.setEmail(email);
        request.setPassword("password1");
        request.setRoles(roles);

        RestAssuredMockMvc.given().header("Content-Type", "application/json")
                .body(request)
                .when().post("/api/auth/signup")
                .then().assertThat().statusCode(400)
                .and().assertThat().body("message", equalTo("Email exists!"));
    }

}