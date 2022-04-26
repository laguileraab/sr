<div align="center" id="top"> 
  <a href="" rel="noopener">

 <img width=200px height=200px src="https://i.imgur.com/6wj0hh6.jpg" alt="Api"></a>
  &#xa0;
</div>

<h1 align="center">Upload file Project</h1>

<p align="center">

  <img alt="Github top language" src="https://img.shields.io/github/languages/top/laguileraab/sr?color=56BEB8">

  <img alt="Github language count" src="https://img.shields.io/github/languages/count/laguileraab/sr?color=56BEB8">

  <img alt="Repository size" src="https://img.shields.io/github/repo-size/laguileraab/sr?color=56BEB8">

</p>
<p align="center">
  <a href="#dart-about">About</a> &#xa0; | &#xa0;
    <a href="#white_check_mark-requirements-for-compilation">Requirements</a> &#xa0; | &#xa0;
    <a href="#rocket-technologies">Technologies</a> &#xa0; | &#xa0;
  <a href="#checkered_flag-starting">Starting</a> &#xa0; | &#xa0;
  <a href="#sparkles-usage-of-the-app">Usage</a> &#xa0; | &#xa0;
    <a href="#test_tube-perform-tests">Tests</a> &#xa0; | &#xa0;
  <a href="#heavy_check_mark-run-with-docker">Docker</a> &#xa0; | &#xa0;
  <a href="https://test-engineers.herokuapp.com">Heroku</a> &#xa0; | &#xa0;
  <a href="https://github.com/laguileraab" target="_blank">Author</a>
</p>

<br>

## :dart: About ##

In this project i made a REST API that allow you to upload a file and download it from MongoDB using Grid File System embedded in Mongo. The files are limited for a 200mb files, but it can be changed in the application's properties, there is currently no restriction in the type of file to be uploaded, also every file is gonna be compressed in a zip format and re stored in the database.

The files are stored for a specific user and is necessary to be authenticated first with a previously sign up user.

## :white_check_mark: Requirements for compilation <a name = "requirement"></a>

You need to have Java >= 17 and Maven installed in order to run this project. Check if you have install it already with:

Java
```bash
java --version
```

Maven
```bash
mvn --version
```

## :rocket: Technologies <a name = "tech"></a>

The following tools were used in this project:

- [Java](https://www.java.com/)
- [Spring](https://spring.io/)
- [MongoDB](https://www.mongodb.com/)


## :checkered_flag: Starting ## <a name = "starting"></a>
We need to install maven dependencies and test will failed in this part, thats why we skip them because it needs to be created a connection database and User "user" for testing. [See here](https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/test-method.html#test-method-withuserdetails)


```bash
# Clone this project
$ git clone https://github.com/laguileraab/sr

# Access
$ cd sr

# Install dependencies and skip tests
$ mvn install -DskipTests

# Run the project
$ java -jar .\target\sr-0.0.1-SNAPSHOT.jar

# The app server will initialize in the <http://127.0.0.1>

```


#### Database
In the database we need a database called "upload".
Collections:
- users
- roles
- fileMetadata

This collections along with the GridFS will be automatically created by Spring Boot, but we need to create a Role "ROLE_USER" in the database, also even thought is not necessary we can create more Roles like "ROLE_ADMIN". Spring Boot require the addition of the prefix "ROLE_" in the Granted Authorities to match.

```bash
db.createCollection("roles");
db.roles.insertMany([
    {
        name:"ROLE_USER"
    },
    {
        name:"ROLE_ADMIN"
    }
])
```

After that we sign up with User "user" with role "user" and proceed to perform the tests.

```bash
curl --location --request POST 'localhost/api/auth/signup' \
--header 'Content-Type: application/json' \
--data-raw '{
  "username": "user",
  "email": "user@emailfortest.com",
  "password": "password1",
  "role":["user"]
}'
# Object
{
"username":"user",
"email":"email@userfortest.com",
"password":"supersecretpassword",
"role":["user"]
}
```

## :test_tube: Perform tests
For launching tests use:

```bash
$ mvn clean test integration-test verify
```

After that you can go to Jacoco site for review the Code Coverage of the tests inside the project.

```bash
./site/index.html
```

## :sparkles: Usage of the app <a name = "usage"></a>

In the project is been added Swagger 3 for documentation of the API, this allows also to perform test in the web application and an understanding of the required objects for every endpoint.

```bash
#Swagger 3
http://127.0.0.1/swagger-ui.html
```

```bash
#Example user object
{
"username":"admin",
"email":"email@admin.com",
"password":"supersecretpassword",
"role":["ROLE_ADMIN"]
}
```

Also been added Actuator for checking the status of the web service, useful for containers.

```bash
http://127.0.0.1/actuator #endpoints
http://127.0.0.1/actuator/health/ #component's status, ej: database
http://127.0.0.1/actuator/loggers/ #loggers
http://127.0.0.1/actuator/caches/ #elements in cache
```

## :heavy_check_mark: Spring Security and JWT ##

The app has authentication and authorization implemented with Spring Security and Management Session with Json Web Token.

## Login and roles
There are 2 roles defined and is necessary to be authenticated in the app in order to use it. For that we need to send a post request like this:

```bash
curl --location --request POST 'localhost/api/auth/signin' \
--header 'Content-Type: application/json' \
--data-raw '{
  "username": "user",
  "password": "supersecurepassword"
}'

# SignOut
curl -X POST -H 'Content-Type: application/json' -i 'http://127.0.0.1/api/auth/signout'
```

## Rest API File

```bash

# Upload file
curl --location --request POST 'localhost/api/file' \
--header 'Cookie: Cookie=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0MSIsImlhdCI6MTY1MDg5MTExNCwiZXhwIjoxNjUwOTc3NTE0fQ.6VVRphZfTonFbGhK19NIVBx48IbosjHYXAF-nQbRXRY_JpxTOUR4DcWgKLG7E4r6C3WZ9D3kgr1Sp5RuR0ka8Q' \
--form 'file=@"/C:/filename.pdf"'

# Display a list of files of the user.
curl --location --request GET 'localhost/api/file' \
--header 'Cookie: Cookie=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0MSIsImlhdCI6MTY1MDg5MTExNCwiZXhwIjoxNjUwOTc3NTE0fQ.6VVRphZfTonFbGhK19NIVBx48IbosjHYXAF-nQbRXRY_JpxTOUR4DcWgKLG7E4r6C3WZ9D3kgr1Sp5RuR0ka8Q'

# Given an id download the zipped file of the user.
curl --location --request GET 'localhost/api/file/6266997946953816f3f4d05b' \
--header 'Cookie: Cookie=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiaWF0IjoxNjUwNzM4Njg3LCJleHAiOjE2NTA4MjUwODd9.C4-yEKqL7xL5lc4yTHIp27UPUvWlfen96SKXyaGM9fnBwnJGQY55QhlbxtxnzL-QwFeL8vF557tHMyuq33krvA'

# Update filename
curl --location --request PUT 'localhost/api/file/6266997946953816f3f4d05b' \
--header 'Cookie: Cookie=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0MSIsImlhdCI6MTY1MDg5MTExNCwiZXhwIjoxNjUwOTc3NTE0fQ.6VVRphZfTonFbGhK19NIVBx48IbosjHYXAF-nQbRXRY_JpxTOUR4DcWgKLG7E4r6C3WZ9D3kgr1Sp5RuR0ka8Q' \
--form 'name="newFilename"'

# Delete file
curl --location --request DELETE 'localhost/api/file/6266997946953816f3f4d05b' \
--header 'Cookie: Cookie=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiaWF0IjoxNjUwNzM4Njg3LCJleHAiOjE2NTA4MjUwODd9.C4-yEKqL7xL5lc4yTHIp27UPUvWlfen96SKXyaGM9fnBwnJGQY55QhlbxtxnzL-QwFeL8vF557tHMyuq33krvA'

```

## :heavy_check_mark: Run with Docker <a name = "docker"></a>

In the project is a Dockerfile to dockerize the Spring Boot application and a Docker Composer file v3 for Docker deployment. You can be setup in one line:

```bash
docker-compose up
```

And you're good to go. See here:

```bash
http://localhost/actuator/health
```

and everything must be UP.

:triangular_flag_on_post: Important!

Also this project is currently deployed on [Heroku](https://test-engineers.herokuapp.com) and the database in MongoDB Atlas.


## ✍️ Author <a name = "author"></a>

- [@laguileraab](https://github.com/laguileraab)

&#xa0;

<a href="#top">Back to top</a>