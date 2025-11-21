package com.example.uploadingfiles

import com.example.uploadingfiles.storage.StorageFileNotFoundException
import com.example.uploadingfiles.storage.StorageService
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.nio.file.Paths
import java.util.stream.Stream

@AutoConfigureMockMvc
@SpringBootTest
class FileUploadTests {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockitoBean
    private lateinit var storageService: StorageService

    @Test
    fun shouldListAllFiles() {
        given(storageService.loadAll())
            .willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")))

        mvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(
                model().attribute(
                    "files",
                    Matchers.contains(
                        "http://localhost/files/first.txt",
                        "http://localhost/files/second.txt"
                    )
                )
            )
    }

    @Test
    fun shouldSaveUploadedFile() {
        val multipartFile = MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "Spring Framework".toByteArray()
        )

        mvc.perform(multipart("/").file(multipartFile))
            .andExpect(status().isFound)
            .andExpect(header().string("Location", "/"))

        then(storageService).should().store(multipartFile)
    }

    @Test
    fun should404WhenMissingFile() {
        given(storageService.loadAsResource("test.txt"))
            .willThrow(StorageFileNotFoundException::class.java)

        mvc.perform(get("/files/test.txt"))
            .andExpect(status().isNotFound)
    }
}