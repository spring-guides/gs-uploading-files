package com.example.uploadingfiles

import com.example.uploadingfiles.storage.StorageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.multipart.MultipartFile

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileUploadIntegrationTests {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @MockBean
    private lateinit var storageService: StorageService

    @LocalServerPort
    private var port: Int = 0

    @Test
    fun shouldUploadFile() {
        val resource = ClassPathResource("testupload.txt", javaClass)
        // When redirecting to GET "/" the controller calls storageService.loadAll(); stub it to avoid errors
        given(storageService.loadAll()).willReturn(java.util.stream.Stream.empty())

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val map: MultiValueMap<String, Any> = LinkedMultiValueMap<String, Any>()
        map.add("file", resource)

        val requestEntity = HttpEntity(map, headers)

        // Create a TestRestTemplate that does not follow redirects (Spring Boot 3.5 defaults to following redirects)
        val client = java.net.http.HttpClient.newBuilder()
            .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
            .build()
        val requestFactory = org.springframework.http.client.JdkClientHttpRequestFactory(client)
        val noRedirect = org.springframework.boot.web.client.RestTemplateBuilder()
            .rootUri("http://localhost:$port")
            .requestFactory { _: org.springframework.boot.web.client.ClientHttpRequestFactorySettings -> requestFactory }
            .build()
        val response = noRedirect.postForEntity(
            "/",
            requestEntity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
        assertThat(response.headers.location.toString())
            .startsWith("http://localhost:$port/")
    }

    @Test
    fun shouldDownloadFile() {
        val resource = ClassPathResource("testupload.txt", javaClass)
        given(storageService.loadAsResource("testupload.txt")).willReturn(resource)

        val response = restTemplate.getForEntity(
            "/files/{filename}",
            String::class.java,
            "testupload.txt"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.getFirst(HttpHeaders.CONTENT_DISPOSITION))
            .isEqualTo("attachment; filename=\"testupload.txt\"")
        assertThat(response.body).isEqualTo("Spring Framework")
    }
}