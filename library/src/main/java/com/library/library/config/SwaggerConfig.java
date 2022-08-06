package com.library.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.plugin.core.SimplePluginRegistry;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket libraryApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("library")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.library.library.controller"))
                .paths(PathSelectors.ant("/libraries/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("user")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.library.library.controller"))
                .paths(PathSelectors.ant("/users/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket authorApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("author")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.library.library.controller"))
                .paths(PathSelectors.ant("/authors/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket bookApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("book")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.library.library.controller"))
                .paths(PathSelectors.ant("/books/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("admin")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.library.library.controller"))
                .paths(PathSelectors.ant("/admin/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket librarianApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("librarian")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.library.library.controller"))
                .paths(PathSelectors.ant("/librarian/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public LinkDiscoverers discoverers() {
        List<LinkDiscoverer> plugins = new ArrayList<>();
        plugins.add(new CollectionJsonLinkDiscoverer());
        return new LinkDiscoverers(SimplePluginRegistry.create(plugins));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Profile Api")
                .contact(new Contact(
                        "",
                        "",
                        "danyil.shykh@gmail.com"
                ))
                .build();
    }

}
