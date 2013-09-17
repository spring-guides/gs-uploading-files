<#assign project_id="gs-uploading-files">
This guide walks you through the process of creating a server application that can receive multi-part file uploads.

What you'll build
-----------------

You will create a Spring MVC application that accepts file uploads. You will also build a simple client to upload a test file.


What you'll need
----------------

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>
 

## <@how_to_complete_this_guide jump_ahead='Create a configuration class'/>


<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>

<@create_both_builds/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Create a configuration class
------------------------------

To upload files with Servlet 3.0 containers, you need to register a `MultipartConfigElement` class (which would be `<multipart-config>` in web.xml).

    <@snippet path="src/main/java/hello/Application.java" prefix="initial"/>

This class is used to configure the server application that will receive file uploads, thanks to the `@Configuration` annotation.

You will soon add a Spring MVC controller, which is why you need both `@EnableAutoConfiguration` and `@ComponentScan`. Normally, you would use `@EnableWebMvc` for a Spring MVC application, but Spring Boot automatically adds this annotation when it detects **spring-webmvc** on your classpath. `@ComponentScan` makes it possible to automatically find `@Controller`-marked classes.

Using `@EnableAutoConfiguration`, the application will also detect the `MultipartConfigElement` bean and make itself ready for file uploads.

> **Note:** [MultipartConfigElement](http://tomcat.apache.org/tomcat-7.0-doc/servletapi/javax/servlet/MultipartConfigElement.html) is a Servlet 3.0 standard element that defines the limits on uploading files. This component is supported by all compliant containers like Tomcat and Jetty. Here it's configured to upload to the folder the application runs in with no limits, but you can override these settings if you wish.


Create a file upload controller
---------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following code provides the web app with the ability to upload files.

    <@snippet path="src/main/java/hello/FileUploadController.java" prefix="complete"/>

The entire class is marked up with `@Controller` so Spring MVC can pick it up and look for routes.

Each method is tagged with `@RequestMapping` to flag the path and the REST action. In this case, `GET` returns a very simple message indicating the `POST` operation is available.

The `handleFileUpload` method is geared to handle a two-part message: `name` and `file`. It checks to make sure the file is not empty, and if it is empty, the method grabs the bytes. Next, it writes them out through a `BufferedOutputStream`. Finally, it appends **-uploaded** to the target filename to clearly show when a file has been uploaded.

> **Note**: In a production scenario, you more likely would store the files in a temporary location, a database, or perhaps a NoSQL store like [Mongo's GridFS](http://docs.mongodb.org/manual/core/gridfs/). You also need controls in place to avoid filling up the filesystem while also protecting yourself from vulnerabilities such as uploading executables and overwriting existing files.


Make the application executable
-------------------------------

Although it is possible to package this service as a traditional [WAR][u-war] file for deployment to an external application server, the simpler approach demonstrated below creates a _standalone application_. You package everything in a single, executable JAR file, driven by a good old Java `main()` method. And along the way, you use Spring's support for embedding the [Tomcat][u-tomcat] servlet container as the HTTP runtime, instead of deploying to an external instance.

### Create an Application class

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

The `main()` method defers to the [`SpringApplication`][] helper class, providing `Application.class` as an argument to its `run()` method. This tells Spring to read the annotation metadata from `Application` and to manage it as a component in the _[Spring application context][u-application-context]_.

The `@ComponentScan` annotation tells Spring to search recursively through the `hello` package and its children for classes marked directly or indirectly with Spring's [`@Component`][] annotation. This directive ensures that Spring finds and registers the `FileUploadController`, because it is marked with `@Controller`, which in turn is a kind of `@Component` annotation.

The [`@EnableAutoConfiguration`][] annotation switches on reasonable default behaviors based on the content of your classpath. For example, because the application depends on the embeddable version of Tomcat (tomcat-embed-core.jar), a Tomcat server is set up and configured with reasonable defaults on your behalf. And because the application also depends on Spring MVC (spring-webmvc.jar), a Spring MVC [`DispatcherServlet`][] is configured and registered for you — no `web.xml` necessary! Because there is a `MultipartConfigElement`, it configured the `DispatcherServlet` with multipart file upload functionality. Auto-configuration is a powerful, flexible mechanism. See the [API documentation][`@EnableAutoConfiguration`] for further details.

<@build_an_executable_jar_subhead/>
<@build_an_executable_jar_with_both/>

<@run_the_application_with_both module="service"/>

That runs the server-side piece that receives file uploads. Logging output is displayed. The service should be up and running within a few seconds.


Create a client and upload a file
----------------------------------

So far, you have built a server application capable of receiving file uploads. It would not be of much use unless you also build a client application to upload a file. The easiest way to do that is by using Spring MVC's `RestTemplate`.

    <@snippet path="src/main/java/hello/FileUploader.java" prefix="complete"/>

This client application creates a `RestTemplate` and then loads up a `MultiValueMap` with the name and the file. This leverages Spring's `FileSystemResource` class to properly load the bytes for the file. Then the template uses its `postForObject` method to `POST` the file to the server. Because the server was coded to write a textual message straight into the HTTP response, the client application prints that message out to the console.

> **Note**: In more sophisticated applications, you probably want to use real HTML and some type of file chooser component to pick the file for upload.

You just coded some client code to upload a sample file. To run the code, add this to your Gradle build file:

```groovy
apply plugin: 'application'
mainClassName = "hello.FileUploader"
run {
    args 'sample.txt'
}
```

> **Note:** If you clicked on the link up above to view the final `build.gradle` file, you will have already seen this. There is similar material added to the `pom.xml` file.

With the server running in one window, you need to open another window to run the client.

```sh
$ ./gradlew run
```

> **Note:** If you are using Maven, you can run the client by typing `mvn package exec:java`.

It should produce some output like this in the client window:

```sh
You successfully uploaded sample.txt into sample.txt-uploaded !
```

The controller itself doesn't print anything out, but instead returns the message posted to the client.


Summary
-------

Congratulations! You have just written a client and server that use Spring to handle file uploads.


<@u_rest/>
<@u_war/>
<@u_tomcat/>
<@u_application_context/>
[`@Controller`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Controller.html
[`SpringApplication`]: http://docs.spring.io/spring-boot/docs/0.5.0.M3/api/org/springframework/boot/SpringApplication.html
[`@EnableAutoConfiguration`]: http://docs.spring.io/spring-boot/docs/0.5.0.M3/api/org/springframework/boot/autoconfigure/EnableAutoConfiguration.html
[`@Component`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
[`@ResponseBody`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ResponseBody.html
[`DispatcherServlet`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/DispatcherServlet.html
