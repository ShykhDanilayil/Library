# Library
My Spring Pet-Project

### Overview
Stack technologies: `Spring MVC`, `Spring Boot`, `RESTful`, `Spring Data JPA`, `JUnit`, `Mockito`, `MockMvc`, `Hamcrest Matchers`, `Swagger Documentation`, `MySQL`, `JaCoCO`, `Docker`.
- added basic validation to DTO classes 
- used mapstruct to map DTOs 
- implemented custom common and unified error handling functionality used `Spring MVC` components 
- added the `Spring Boot Actuator`
- extended service RESTful endpoints by `Swagger Documentation`    
- created custom validation annotations used `ConstraintValidator` 
- used annotation: `@Query`, `@NamedNativeQuery`. 

### JaCoCO
JaCoCo report could be bound under `target/site/jacoco/index.html`. To generate it just run `mvn clean verify`.


### Starting project locally with docker-compose
You can start this application with a single command `docker-compose up`.


### API Documentation Tool - Swagger UI
Once your application is started, you can go to `http://localhost:8080/swagger-ui.html#` and start use API.
