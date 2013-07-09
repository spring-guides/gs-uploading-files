<#assign project_id="gs-uploading-files">

# Getting Started: Uploading a File


What you'll build
-----------------

This Getting Started guide will walk you through the process of creating a server that can receive multi-part file uploads. You will also build a simple client to upload the file.


What you'll need
----------------

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>
 

## <@how_to_complete_this_guide jump_ahead='Creating a Configuration Class'/>


<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>

### Create a Maven POM

    <@snippet path="pom.xml" prefix="initial"/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Creating a Configuration Class
------------------------------

Uploading files with Servlet 3.0 containers is really simple. You just need to register a `MultipartConfigElement` (which would be `<multipart-config>` in web.xml) to turn on multi-part file upload support.

<@snippet path="src/main/java/hello/Application.java" prefix="initial"/>

This class is used to configure our application, thanks to the `@Configuration` annotation.

You will soon build a Spring MVC controller which is why you need `@EnableWebMvc` `@ComponentScan`. That activates many key features while also enabling the ability to find the controller class.

By using `@EnableAutoConfiguration`, the application will detect the `MultipartConfigElement` bean and automatically rig the application for file uploads.

> **Note:** [MultipartConfigElement](http://tomcat.apache.org/tomcat-7.0-doc/servletapi/javax/servlet/MultipartConfigElement.html) is a Servlet 3.0 standard element that defines the limits on uploading files. This component is supported by all compliant containers like Tomcat and Jetty. Here it's configured to upload to the folder our app runs in with no limits, but you can override these settings if you wish.


Creating a File Upload Controller
---------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following code provides the web app with the ability to upload files.

<@snippet path="src/main/java/hello/FileUploadController.java" prefix="complete"/>

First of all, this entire class is marked up with `@Controller` so Spring MVC can pick it up and look for routes.

Next, each method has been tagged with `@RequestMapping` to flag the path and the REST action. In this case, `GET` will return back a very simple message indicating the `POST` operation is available.

The `handleFileUpload` method is where the key parts are. First of all, you have it geared to handle a two-part message: `name` and `file`. It checks to make sure the file is not empty, and if not, it grabs the bytes. Next, it writes them out through a `BufferedOutputStream`. Finally, it appends **-uploaded** to the target filename to clearly see when a file has been uploaded.

> **Note**: In a real world solution, you would more likely store the files in some temporary location, a database, of perhaps a NoSQL store like [Mongo's GridFS](http://docs.mongodb.org/manual/core/gridfs/). You would also need some controls in place to avoid filling up the filesystem while also protecting yourself from vulnerabilities such as uploading executables as well as overwriting existing files.


Make the application executable
-------------------------------

Although it is possible to package this service as a traditional _web application archive_ or [WAR][u-war] file for deployment to an external application server, the simpler approach demonstrated below creates a _standalone application_. You package everything in a single, executable JAR file, driven by a good old Java `main()` method. And along the way, you use Spring's support for embedding the [Tomcat][u-tomcat] servlet container as the HTTP runtime, instead of deploying to an external instance.

### Create a main class

<@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

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

Now run the following to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ mvn package
```

[maven-shade-plugin]: https://maven.apache.org/plugins/maven-shade-plugin


Run the service
---------------

Run your service with `java -jar` at the command line:

```sh
$ java -jar target/${project_id}-complete-0.1.0.jar
```

Logging output is displayed. The service should be up and running within a few seconds.


Creating a file uploading client
--------------------------------

The easiest way to create a file uploader is using Spring MVC's `RestTemplate`.

<@snippet path="src/main/java/hello/FileUploader.java" prefix="complete"/>

You create a `RestTemplate` and then load up a `MultiValueMap` with the name and the file. This leverages Spring's `FileSystemResource` to properly load the bytes for the file. Then it `POST`s it to the server. Because the server was coded to write a textual response straight into the HTTP response, it prints it out to the screen.

> **Note**: In more sophisticated applications, you probably want to use real HTML and some type of file chooser component to pick the file for upload.


Uploading a file to the server
------------------------------

With the server running in one window, you need to open another window and run the client.

```sh
$ mvn exec:java
```

It should produce some output like this in the client window:

```sh
You successfully upload sample.txt into sample.txt-uploaded !
```

The controller itself doesn't print anything out, but instead returns the message posted to the client.


Summary
-------

Congratulations! You have just written a client and server that both handle uploading files using Spring.


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
