package com.easterfg.takeaway.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author EasterFG on 2022/10/19
 */
@EnableKnife4j
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .enable(true)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.easterfg.takeaway.controller"))
                .build();
    }

    @Bean
    public ApiInfo apiInfo() {
        Contact contact = new Contact("EasterFG", "https://github.com/easterfg", "eastefg@163.com");
        return new ApiInfoBuilder()
                .title("qoo外卖接口文档")
                .version("1.0.0")
                .contact(contact)
                .build();
    }

}
