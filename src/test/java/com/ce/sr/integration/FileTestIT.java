package com.ce.sr.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.ce.sr.controllers.FileController;
import com.ce.sr.payload.request.LoginRequest;
import com.ce.sr.payload.request.SignupRequest;
import com.ce.sr.repository.FileRepository;
import com.ce.sr.repository.UserRepository;
import com.ce.sr.services.FileService;
import com.mongodb.client.gridfs.model.GridFSFile;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.http.Cookie;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.response.Response;

@SpringBootTest
public class FileTestIT extends IT {

    private Cookie auth_token = null;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    private String userId;
    Random rand = new Random();

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private GridFsTemplate template;

    @Autowired
    private GridFsOperations operations;

    @Autowired
    private FileService fileService;

    @BeforeEach
    public void setup() {

        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
        RestAssuredMockMvc.mockMvc(mockMvc);

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");

        String username = "test" + rand.nextInt();
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
                    .then().log().ifError()
                    .statusCode(200).contentType("application/json");
        }
        MockMvcResponse response = RestAssuredMockMvc.given().header("Content-Type", "application/json")
                .body(new LoginRequest(username, password))
                .when().post("/api/auth/signin")
                .then().log().ifError()
                .statusCode(200).contentType("application/json").cookie("Cookie").extract().response();

        auth_token = response.detailedCookie("Cookie");
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
    public void listFileTestIT() throws Exception {
        RestAssuredMockMvc
                .given().cookie(auth_token)
                .when().get("/api/file")
                .then().assertThat().statusCode(200);
    }

    @Test
    public void listFileWithoutAuthorizationTestIT() throws Exception {
        RestAssuredMockMvc
                .given()
                .when().get("/api/file")
                .then().assertThat().statusCode(401);
    }

    @Test
    public void uploadFileTestIT() throws IOException {
        // String text = "This is a test";
        String filename = "filename";
        File file = new File(filename);
        file.createNewFile();
        RestAssuredMockMvc
                .given().cookie(auth_token).multiPart("file", file)
                .when().post("/api/file")
                .then().assertThat().statusCode(200)
                .and().body("message", equalTo("File " + filename + " uploaded successfully"));
        file.delete();
        Query query = new Query(Criteria.where("filename").is(filename + ".zip"));
        GridFSFile doc = template.findOne(query);
        if (doc != null)
            fileRepository.deleteByFileId(doc.getObjectId().toString());
        template.delete(query);
    }

    @Test
    public void downloadFileTestIT() throws Exception {
        String filename = "filename";
        File file = new File(filename);
        file.createNewFile();

        RestAssuredMockMvc
                .given().cookie(auth_token).multiPart("file", file)
                .when().post("/api/file")
                .then().assertThat().statusCode(200)
                .and().body("message", equalTo("File " + filename + " uploaded successfully"));
        file.delete();

        Query query = new Query(Criteria.where("filename").is(filename + ".zip"));
        GridFSFile doc = template.findOne(query);

        RestAssuredMockMvc
                .given().cookie(auth_token)
                .when().get("/api/file/" + doc.getObjectId().toString())
                .then().assertThat().statusCode(200);

        if (doc != null)
            fileRepository.deleteByFileId(doc.getObjectId().toString());
        template.delete(query);
    }

    @Test
    public void deleteFileTestIT() throws Exception {
        String filename = "filename";
        File file = new File(filename);
        file.createNewFile();

        RestAssuredMockMvc
                .given().cookie(auth_token).multiPart("file", file)
                .when().post("/api/file")
                .then().assertThat().statusCode(200)
                .and().body("message", equalTo("File " + filename + " uploaded successfully"));
        file.delete();

        Query query = new Query(Criteria.where("filename").is(filename + ".zip"));
        GridFSFile doc = template.findOne(query);

        RestAssuredMockMvc
                .given().cookie(auth_token)
                .when().delete("/api/file/" + doc.getObjectId().toString())
                .then().assertThat().statusCode(200);
    }

}