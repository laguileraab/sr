package com.ce.sr.integration;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.ce.sr.payload.request.LoginRequest;
import com.ce.sr.payload.request.SignupRequest;
import com.ce.sr.payload.response.UserInfoResponse;
import com.ce.sr.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.http.Cookie;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;

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
        roles.add("user");
        roles.add("admin");

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

                MockMvcResponse response = RestAssuredMockMvc.given().header("Content-Type", "application/json")
                .body(new LoginRequest(username, password))
                .when().post("/api/auth/signin")
                .then().log().ifError()
                .statusCode(200).contentType("application/json").cookie("Cookie").extract().response();

        auth_token = response.detailedCookie("Cookie");
        userId = response.as(UserInfoResponse.class).getId();
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
        roles.add("user");
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
    public void signUpUserWithoutRoleTestIT() {

        String username1 = "test" + rand.nextInt();
        String email1 = "test" + rand.nextInt() + "@email.com";
        String password1 = "password" + rand.nextInt();

        SignupRequest request1 = new SignupRequest();
        request1.setUsername(username1);
        request1.setEmail(email1);
        request1.setPassword(password1);

        RestAssuredMockMvc.given().header("Content-Type", "application/json")
                .body(request1)
                .when().post("/api/auth/signup")
                .then().assertThat().statusCode(200);
        MockMvcResponse response = RestAssuredMockMvc.given().header("Content-Type", "application/json")
                .body(new LoginRequest(username1, password1))
                .when().post("/api/auth/signin")
                .then().log().ifError()
                .statusCode(200).contentType("application/json").cookie("Cookie").extract().response();

        Cookie auth_token1 = response.detailedCookie("Cookie");
        String userId1 = response.as(UserInfoResponse.class).getId();

        if (auth_token1 != null)
            RestAssuredMockMvc
                    .given().cookie(auth_token1)
                    .when().post("/api/auth/signout")
                    .then().assertThat().statusCode(200)
                    .and().assertThat().body("message", equalTo("You've been logout!"));
        if (userId1 != null)
            userRepository.deleteById(userId1);
    }

    @Test
    public void signUpWithoutUserTestIT() {

        String email = "test" + rand.nextInt() + "@email.com";
        String password = "password" + rand.nextInt();

        SignupRequest request = new SignupRequest();
        request.setEmail(email);
        request.setPassword(password);

        RestAssuredMockMvc.given().header("Content-Type", "application/json")
                .body(request)
                .when().post("/api/auth/signup")
                .then().assertThat().statusCode(400);

    }

    @Test
    public void signUpEmailExistsTestIT() {

        SignupRequest request = new SignupRequest();
        Set<String> roles = new HashSet<>();
        roles.add("user");
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

    @Test
    public void AuthBadCredentialsFormUserTestIT() {
        Set<String> roles = new HashSet<>();
        roles.add("user");

        String username = "t"; // Short Username
        String email = "test" + rand.nextInt() + "@email.com";
        String password = "password" + rand.nextInt();

        if (!userRepository.existsByUsername(username) && !userRepository.existsByEmail(email)) {
            SignupRequest request = new SignupRequest();
            request.setUsername(username);
            request.setEmail(email);
            request.setPassword(password);
            request.setRoles(roles);

            RestAssuredMockMvc.given().header("Content-Type", "application/json")
                    .body(request)
                    .when().post("/api/auth/signup")
                    .then().statusCode(400);
        }
    }

    @Test
    public void AuthBadCredentialsFormEmailTestIT() {
        Set<String> roles = new HashSet<>();
        roles.add("user");

        String username = "t";
        String email = "test" + rand.nextInt(); // Not email
        String password = "password" + rand.nextInt();

        if (!userRepository.existsByUsername(username) && !userRepository.existsByEmail(email)) {
            SignupRequest request = new SignupRequest();
            request.setUsername(username);
            request.setEmail(email);
            request.setPassword(password);
            request.setRoles(roles);

            RestAssuredMockMvc.given().header("Content-Type", "application/json")
                    .body(request)
                    .when().post("/api/auth/signup")
                    .then().statusCode(400);
        }
    }

}