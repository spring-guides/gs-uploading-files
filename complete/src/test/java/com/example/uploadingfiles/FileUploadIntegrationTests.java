package com.example.uploadingfiles;

import java.net.http.HttpClient;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.mockito.BDDMockito.given;

import com.example.uploadingfiles.storage.StorageService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class FileUploadIntegrationTests {

  @Autowired
  private RestTestClient client;

  @MockitoBean
  private StorageService storageService;

  @LocalServerPort
  private int port;

  @Test
  public void shouldUploadFile() throws Exception {
    ClassPathResource resource = new ClassPathResource("testupload.txt", getClass());

    // In case of redirect, GET "/" calls storageService.loadAll(); stub it to avoid errors
    given(this.storageService.loadAll()).willReturn(Stream.empty());

    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", resource);

    // Build a RestTestClient that does NOT follow redirects so we can assert 302/Location
    ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.jdk()
        .withHttpClientCustomizer(customizer -> customizer.followRedirects(HttpClient.Redirect.NEVER))
        .build();
    RestTestClient noRedirect = RestTestClient
        .bindToServer(requestFactory)
        .baseUrl("http://localhost:" + this.port)
        .build();

    noRedirect.post().uri("/").contentType(MediaType.MULTIPART_FORM_DATA)
        .body(map).exchange()
        .expectStatus().isFound()
        .expectHeader().valueMatches(HttpHeaders.LOCATION, "http://localhost:" + this.port + "/.*");
  }

  @Test
  public void shouldDownloadFile() {
    ClassPathResource resource = new ClassPathResource("testupload.txt", getClass());
    given(this.storageService.loadAsResource("testupload.txt")).willReturn(resource);

    this.client.get().uri("/files/{filename}", "testupload.txt").exchangeSuccessfully()
        .expectHeader().contentDisposition(ContentDisposition.attachment().filename("testupload.txt").build())
        .expectBody(String.class)
        .isEqualTo("Spring Framework");
  }

}
