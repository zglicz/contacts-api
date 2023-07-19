package com.zglicz.contactsapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
	@Value("${zglicz.openapi.dev-url}")
	private String devUrl;

	@Bean
	public OpenAPI myOpenAPI() {
		Server devServer = new Server();
		devServer.setUrl(devUrl);
		devServer.setDescription("Server URL in Development environment");

		Contact contact = new Contact();
		contact.setEmail("mzglicz@gmail.com");
		contact.setName("zglicz");

		Info info = new Info()
				.title("Contacts management API")
				.description("This API allows to manage contacts and skills")
				.contact(contact)
				.version("1.0");

		return new OpenAPI().info(info).servers(List.of(devServer));
	}
}
