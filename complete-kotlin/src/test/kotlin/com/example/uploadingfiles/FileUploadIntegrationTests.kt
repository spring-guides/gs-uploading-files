package com.example.uploadingfiles

import com.example.uploadingfiles.storage.StorageService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import org.springframework.util.LinkedMultiValueMap
import java.net.http.HttpClient
import java.util.stream.Stream

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class FileUploadIntegrationTests @Autowired constructor(private val client: RestTestClient) {

  @MockitoBean
  private lateinit var storageService: StorageService

  @LocalServerPort
  private val port = 0

  @Test
  fun shouldUploadFile() {
    val resource = ClassPathResource("testupload.txt", javaClass)

    // In case of redirect, GET "/" calls storageService.loadAll(); stub it to avoid errors
    BDDMockito.given(this.storageService.loadAll()).willReturn(Stream.empty())

    val map = LinkedMultiValueMap<String, Any>()
    map.add("file", resource)

    // Build a RestTestClient that does NOT follow redirects so we can assert 302/Location
    val requestFactory: ClientHttpRequestFactory = ClientHttpRequestFactoryBuilder.jdk()
      .withHttpClientCustomizer({ customizer -> customizer.followRedirects(HttpClient.Redirect.NEVER) })
      .build()
    val noRedirect = RestTestClient
      .bindToServer(requestFactory)
      .baseUrl("http://localhost:${this.port}")
      .build()

    noRedirect.post().uri("/").contentType(MediaType.MULTIPART_FORM_DATA)
      .body(map).exchange()
      .expectStatus().isFound()
      .expectHeader().valueMatches(HttpHeaders.LOCATION, "http://localhost:${this.port}/.*")
  }

  @Test
  fun shouldDownloadFile() {
    val resource = ClassPathResource("testupload.txt", javaClass)
    BDDMockito.given<Resource?>(this.storageService.loadAsResource("testupload.txt")).willReturn(resource)

    this.client.get().uri("/files/{filename}", "testupload.txt").exchangeSuccessfully()
      .expectHeader().contentDisposition(ContentDisposition.attachment().filename("testupload.txt").build())
      .expectBody<String>()
      .isEqualTo("Spring Framework")
  }
}