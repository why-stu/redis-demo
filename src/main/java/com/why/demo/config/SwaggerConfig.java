package com.why.demo.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket loginApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("redis-demo")
                .apiInfo(apiInfo())
                .enable(true)
                .ignoredParameterTypes(HttpSession.class, HttpServletRequest.class, HttpServletResponse.class)
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("redis-demo API DOC")
                .description("The redis-demo service API")
                .version("1.0.0")
                .build();
    }
}
