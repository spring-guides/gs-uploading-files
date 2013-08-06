
What you'll build
-----------------

This guide walks you through the process of creating a server that can receive multi-part file uploads. You will also build a simple client to upload a file.


What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Maven 3.0][mvn] or later

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[mvn]: http://maven.apache.org/download.cgi
 

How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [git](/understanding/git):
`git clone https://github.com/springframework-meta/gs-uploading-files.git`
 - cd into `gs-uploading-files/initial`.
 - Jump ahead to [Create a configuration class](#initial).

**When you're finished**, you can check your results against the code in `gs-uploading-files/complete`.
[zip]: https://github.com/springframework-meta/gs-uploading-files/archive/master.zip


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Maven](https://maven.apache.org) and [Gradle](http://gradle.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Maven](/guides/gs/maven/content) or [Building Java Projects with Gradle](/guides/gs/gradle/content).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Maven POM

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-uploading-files</artifactId>
    <version>0.1.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>0.5.0.BUILD-SNAPSHOT</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>http://repo.springsource.org/libs-milestone</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/libs-snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>http://repo.springsource.org/libs-milestone</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
        <pluginRepository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/libs-snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```

This guide is using [Spring Boot's starter POMs](/guides/gs/spring-boot/content).

Note to experienced Maven users who are unaccustomed to using an external parent project: you can take it out later, it's just there to reduce the amount of code you have to write to get started.


<a name="initial"></a>
Create a configuration class
------------------------------

To upload files with Servlet 3.0 containers, you need to register a `MultipartConfigElement` class (which would be `<multipart-config>` in web.xml).

`src/main/java/hello/Application.java`
```java
package hello;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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

}
```

This class is used to configure the server application that will receive file uploads, thanks to the `@Configuration` annotation.

You will soon add a Spring MVC controller which is why you need `@EnableWebMvc` `@ComponentScan`. It activates many key features while also enabling the ability to find the controller class.

Using `@EnableAutoConfiguration`, the application will detect the `MultipartConfigElement` bean and rig itself for file uploads.

> **Note:** [MultipartConfigElement](http://tomcat.apache.org/tomcat-7.0-doc/servletapi/javax/servlet/MultipartConfigElement.html) is a Servlet 3.0 standard element that defines the limits on uploading files. This component is supported by all compliant containers like Tomcat and Jetty. Here it's configured to upload to the folder the app runs in with no limits, but you can override these settings if you wish.


Create a file upload controller
---------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following code provides the web app with the ability to upload files.

`src/main/java/hello/FileUploadController.java`
```java
package hello;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileUploadController {
	
	@RequestMapping(value="/upload", method=RequestMethod.GET)
	public @ResponseBody String provideUploadInfo() {
		return "You can upload a file by posting to this same URL.";
	}
	
	@RequestMapping(value="/upload", method=RequestMethod.POST)
	public @ResponseBody String handleFileUpload(@RequestParam("name") String name, 
			@RequestParam("file") MultipartFile file){
		if (!file.isEmpty()) {
			try {
				byte[] bytes = file.getBytes();
				BufferedOutputStream stream = 
						new BufferedOutputStream(new FileOutputStream(new File(name + "-uploaded")));
				stream.write(bytes);
				stream.close();
				return "You successfully upload " + name + " into " + name + "-uploaded !";
			} catch (Exception e) {
				return "You failed to upload " + name + " => " + e.getMessage();
			}
		} else {
			return "You failed to upload " + name + " because the file was empty.";
		}
	}
	
}
```

The entire class is marked up with `@Controller` so Spring MVC can pick it up and look for routes.

Each method is tagged with `@RequestMapping` to flag the path and the REST action. In this case, `GET` returns a very simple message indicating the `POST` operation is available.

The `handleFileUpload` method is geared to handle a two-part message: `name` and `file`. It checks to make sure the file is not empty, and if it is empty, the method grabs the bytes. Next, it writes them out through a `BufferedOutputStream`. Finally, it appends **-uploaded** to the target filename to clearly show when a file has been uploaded.

> **Note**: In a production scenario, you more likely would store the files in a temporary location, a database, or perhaps a NoSQL store like [Mongo's GridFS](http://docs.mongodb.org/manual/core/gridfs/). You also need controls in place to avoid filling up the filesystem while also protecting yourself from vulnerabilities such as uploading executables and overwriting existing files.


Make the application executable
-------------------------------

Although it is possible to package this service as a traditional [WAR][u-war] file for deployment to an external application server, the simpler approach demonstrated below creates a _standalone application_. You package everything in a single, executable JAR file, driven by a good old Java `main()` method. And along the way, you use Spring's support for embedding the [Tomcat][u-tomcat] servlet container as the HTTP runtime, instead of deploying to an external instance.

### Create a main class

`src/main/java/hello/Application.java`
```java
package hello;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
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
```

The `main()` method defers to the [`SpringApplication`][] helper class, providing `Application.class` as an argument to its `run()` method. This tells Spring to read the annotation metadata from `Application` and to manage it as a component in the _[Spring application context][u-application-context]_.

The `@ComponentScan` annotation tells Spring to search recursively through the `hello` package and its children for classes marked directly or indirectly with Spring's [`@Component`][] annotation. This directive ensures that Spring finds and registers the `FileUploadController`, because it is marked with `@Controller`, which in turn is a kind of `@Component` annotation.

The [`@EnableAutoConfiguration`][] annotation switches on reasonable default behaviors based on the content of your classpath. For example, because the application depends on the embeddable version of Tomcat (tomcat-embed-core.jar), a Tomcat server is set up and configured with reasonable defaults on your behalf. And because the application also depends on Spring MVC (spring-webmvc.jar), a Spring MVC [`DispatcherServlet`][] is configured and registered for you — no `web.xml` necessary! Because there is a `MultipartConfigElement`, it configured the `DispatcherServlet` with multipart file upload functionality. Auto-configuration is a powerful, flexible mechanism. See the [API documentation][`@EnableAutoConfiguration`] for further details.

### Build an executable JAR

Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Add the following configuration to your existing Maven POM:

`pom.xml`
```xml
    <properties>
        <start-class>hello.Application</start-class>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
            </plugin>
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>exec-maven-plugin</artifactId>
				<version>1.2.1</version>
				<executions>
					<execution>
						<goals>
							<goal>java</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<mainClass>hello.FileUploader</mainClass>
					<arguments>
						<argument>sample.txt</argument>
					</arguments>
				</configuration>
			</plugin>
        </plugins>
    </build>
```

The `start-class` property tells Maven to create a `META-INF/MANIFEST.MF` file with a `Main-Class: hello.Application` entry. This entry enables you to run the jar with `java -jar`.

The [Maven Shade plugin][maven-shade-plugin] extracts classes from all jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.

Because this example has both a server and a client, you need maven's exec plugin to run the file uploading client.

Now run the following to produce a single executable JAR file that contains all necessary dependency classes and resources:

```sh
$ mvn package
```

[maven-shade-plugin]: https://maven.apache.org/plugins/maven-shade-plugin

Run the service
-------------------
Run your service with `java -jar` at the command line:

```sh
$ java -jar target/gs-uploading-files-0.1.0.jar
```


Logging output is displayed. The service should be up and running within a few seconds.


Create a client and upload a file
----------------------------------

Up to this point, you have built a server application capable of receiving file uploads. It would not be of much use unless you also build a client application to upload a file. The easiest way to do that is by using Spring MVC's `RestTemplate`.

`src/main/java/hello/FileUploader.java`
```java
package hello;

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
```

This client application creates a `RestTemplate` and then loads up a `MultiValueMap` with the name and the file. This leverages Spring's `FileSystemResource` class to properly load the bytes for the file. Then the template uses its `postForObject` method to `POST` the file to the server. Because the server was coded to write a textual message straight into the HTTP response, the client application prints that message out to the console.

> **Note**: In more sophisticated applications, you probably want to use real HTML and some type of file chooser component to pick the file for upload.

With the server running in one window, you need to open another window to run the client.

    $ mvn exec:java

It should produce some output like this in the client window:

    You successfully upload sample.txt into sample.txt-uploaded !

The controller itself doesn't print anything out, but instead returns the message posted to the client.


Summary
-------

Congratulations! You have just written a client and server that use Spring to handle file uploads.


[u-rest]: /understanding/rest
[u-war]: /understanding/war
[u-tomcat]: /understanding/tomcat
[u-application-context]: /understanding/application-context
[`@Controller`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Controller.html
[`SpringApplication`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/SpringApplication.html
[`@EnableAutoConfiguration`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/context/annotation/SpringApplication.html
[`@Component`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
[`@ResponseBody`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ResponseBody.html
[`DispatcherServlet`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/web/servlet/DispatcherServlet.html
