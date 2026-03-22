package com.example.apistandardscan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger3 配置类（与业务代码完全分离）
 * 仅需配置Controller扫描路径，无需修改业务代码
 */
@Configuration
public class Swagger3Config {

    /**
     * 配置API文档基础信息（标题、版本、联系人等）
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("项目API文档")
                        .description("基于Swagger3(OpenAPI 3)生成的规范API文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@yourcompany.com")
                                .url("https://www.yourcompany.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    /**
     * 配置API分组和Controller扫描路径
     * 替换为你项目中Controller实际包路径
     */
    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder()
                .group("业务接口组")
                .packagesToScan("com.yourcompany.controller") // 核心：修改为实际Controller包
                .pathsToMatch("/api/**") // 可选：仅扫描/api开头的接口
                .build();
    }
}