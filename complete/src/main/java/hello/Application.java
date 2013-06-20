package hello;

import javax.servlet.MultipartConfigElement;

import org.springframework.bootstrap.SpringApplication;
import org.springframework.bootstrap.context.annotation.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan
@EnableAutoConfiguration
public class Application {

	@Bean
	MultipartConfigElement multipartConfigElement() {
		return new MultipartConfigElement("");
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
