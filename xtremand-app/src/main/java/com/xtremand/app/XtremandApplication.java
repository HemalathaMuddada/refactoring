package com.xtremand.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EntityScan(basePackages = "com.xtremand")
//@EnableJpaAuditing
@ComponentScan(basePackages = { "com.xtremand" })
@EnableJpaRepositories(basePackages = "com.xtremand") // if needed
@EnableScheduling
public class XtremandApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(XtremandApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(XtremandApplication.class, args);
		System.out.println("Application started");
	}
}