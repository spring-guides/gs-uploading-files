package hello;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.FileSystemUtils;

@SpringBootApplication
public class Application {

	public static String ROOT = "upload-dir";

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner init() {
		return (args) -> {
            FileSystemUtils.deleteRecursively(new File(ROOT));

            Files.createDirectory(Paths.get(ROOT));
		};
	}
}
