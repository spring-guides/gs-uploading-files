package com.example.uploadingfiles;

import java.net.http.HttpClient;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.uploadingfiles.storage.StorageService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileUploadIntegrationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private StorageService storageService;

	@LocalServerPort
	private int port;

	@Test
	public void shouldUploadFile() throws Exception {
		ClassPathResource resource = new ClassPathResource("testupload.txt", getClass());

		// In case of redirect, GET "/" calls storageService.loadAll(); stub it to avoid errors
		given(this.storageService.loadAll()).willReturn(Stream.empty());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("file", resource);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

		// Build a RestTemplate that does NOT follow redirects so we can assert 302/Location
		RestTemplate noRedirect = new RestTemplateBuilder()
				.rootUri("http://localhost:" + this.port)
				.requestFactoryBuilder(ClientHttpRequestFactoryBuilder.jdk()
						.withHttpClientCustomizer(customizer ->
								customizer.followRedirects(HttpClient.Redirect.NEVER)))
				.build();

		ResponseEntity<String> response = noRedirect.postForEntity("/", requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		assertThat(response.getHeaders().getLocation().toString())
				.startsWith("http://localhost:" + this.port + "/");
	}

	@Test
	public void shouldDownloadFile() throws Exception {
		ClassPathResource resource = new ClassPathResource("testupload.txt", getClass());
		given(this.storageService.loadAsResource("testupload.txt")).willReturn(resource);

		ResponseEntity<String> response = this.restTemplate
				.getForEntity("/files/{filename}", String.class, "testupload.txt");

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
				.isEqualTo("attachment; filename=\"testupload.txt\"");
		assertThat(response.getBody()).isEqualTo("Spring Framework");
	}

}
