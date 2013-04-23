package fileupload;

import java.io.FileNotFoundException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class FileUploader {
	
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length == 0) {
			System.out.println("Usage: Requires the name of a file to upload.");
			System.exit(1);
		}
		
		RestTemplate template = new RestTemplate();
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("name", args[0]);
		parts.add("file", new FileSystemResource(args[0]));
		String response = template.postForObject("http://localhost:8080/upload", parts, String.class);
		System.out.println(response);
	}

}
