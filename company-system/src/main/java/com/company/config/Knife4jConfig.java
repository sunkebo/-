package com.company.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("公司综合管理系统 API")
                        .version("1.0.0")
                        .description("公司综合管理系统——部门、岗位、员工、考勤、薪资管理")
                        .contact(new Contact().name("管理员")));
    }
}
