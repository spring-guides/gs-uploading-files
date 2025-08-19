/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.uploadingfiles.storage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.abs
import kotlin.random.Random

/**
 * @author Dave Syer
 *
 */
class FileSystemStorageServiceTests {

    private val properties = StorageProperties()
    private lateinit var service: FileSystemStorageService

    @BeforeEach
    fun init() {
        properties.location = "target/files/${abs(Random.nextLong())}"
        service = FileSystemStorageService(properties)
        service.init()
    }

    @Test
    fun emptyUploadLocation() {
        properties.location = ""
        assertThrows(StorageException::class.java) {
            service = FileSystemStorageService(properties)
        }
    }

    @Test
    fun loadNonExistent() {
        assertThat(service.load("foo.txt")).doesNotExist()
    }

    @Test
    fun saveAndLoad() {
        service.store(
            MockMultipartFile(
                "foo", "foo.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World".toByteArray()
            )
        )
        assertThat(service.load("foo.txt")).exists()
    }

    @Test
    fun saveRelativePathNotPermitted() {
        assertThrows(StorageException::class.java) {
            service.store(
                MockMultipartFile(
                    "foo", "../foo.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "Hello, World".toByteArray()
                )
            )
        }
    }

    @Test
    fun saveAbsolutePathNotPermitted() {
        assertThrows(StorageException::class.java) {
            service.store(
                MockMultipartFile(
                    "foo", "/etc/passwd",
                    MediaType.TEXT_PLAIN_VALUE,
                    "Hello, World".toByteArray()
                )
            )
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    fun saveAbsolutePathInFilenamePermitted() {
        // Unix file systems (e.g. ext4) allows backslash '\' in file names.
        val fileName = "\\etc\\passwd"
        service.store(
            MockMultipartFile(
                fileName, fileName,
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World".toByteArray()
            )
        )
        assertTrue(
            Files.exists(
                Paths.get(properties.location).resolve(Paths.get(fileName))
            )
        )
    }

    @Test
    fun savePermitted() {
        service.store(
            MockMultipartFile(
                "foo", "bar/../foo.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World".toByteArray()
            )
        )
    }
}