package com.example.uploadingfiles

import com.example.uploadingfiles.storage.StorageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.net.http.HttpClient
import java.util.stream.Stream

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileUploadIntegrationTests {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @MockitoBean
    private lateinit var storageService: StorageService

    @LocalServerPort
    private var port: Int = 0

    @Test
    fun shouldUploadFile() {
        val resource = ClassPathResource("testupload.txt", javaClass)

        given(storageService.loadAll()).willReturn(Stream.empty())

        val headers = HttpHeaders().apply {
            contentType = MediaType.MULTIPART_FORM_DATA
        }

        val map = LinkedMultiValueMap<String, Any>().apply {
            add("file", resource)
        }
        val requestEntity = HttpEntity(map, headers)

        // Create RestTemplate directly
        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()
        val jdkRequestFactory = JdkClientHttpRequestFactory(client)
        val noRedirect = RestTemplate(jdkRequestFactory)

        val response = noRedirect.postForEntity(
            "http://localhost:$port/",
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