package com.chilterra.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${swagger.service.title}")
    private String title ;
    @Value("${swagger.service.description}")
    private String description ;
    @Value("${swagger.service.version}")
    private String version ;
    @Value("${swagger.service.termsOfServiceUrl}")
    private String termsOfServiceUrl ;
    @Value("${swagger.service.contact.name}")
    private String contactName ;
    @Value("${swagger.service.contact.website:}")
    private String contactWebsite ;
    @Value("${swagger.service.contact.email}")
    private String contactEmail ;

    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(this.buildInfo());
    }

    private Info buildInfo() {
        Info info = new Info();
        info.setTitle(this.title);
        info.setVersion(this.version);
        info.setDescription(this.description);
        info.setTermsOfService(this.termsOfServiceUrl);

        Contact contact = new Contact();
        contact.setName(this.contactName);
        contact.setUrl(this.contactWebsite);
        contact.setEmail(this.contactEmail);
        info.setContact(contact);

        return info;
    }
}
