Getting Started: Uploading a File
========================================

This Getting Started guide will walk you through the process of creating a server that can receive multi-part file uploads as well as building a client to upload a file.

To help you get started, we've provided an initial project structure as well as the completed project for you in GitHub:

```sh
$ git clone https://github.com/springframework-meta/gs-upload-file.git
```

In the `start` folder, you'll find a bare project, ready for you to copy-n-paste code snippets from this document. In the `complete` folder, you'll find the complete project code.

Before we can write code a file uploader, there's some initial project setup that's required. Or, you can skip straight to the [fun part]().

Selecting Dependencies
----------------------
The sample in this Getting Started Guide will leverage Spring MVC and Jetty's embedded servlet container. Therefore, the following library dependencies are needed in the project's build configuration:

 -	org.springframework:spring-webmvc:3.2.2.RELEASE
 -	org.eclipse.jetty:jetty-server:8.1.10.v20130312
 -	org.eclipse.jetty:jetty-servlet:8.1.10.v20130312

Refer to the [Gradle Getting Started Guide]() or the [Maven Getting Started Guide]() for details on how to include these dependencies in your build.

Setting Up ServletContext
----------------------------
To receive uploaded files, we need a server process. While we could use a full-blown servlet container installation, it's easier to spin up an embedded instance of Jetty.

```java
package fileupload;

import javax.servlet.MultipartConfigElement;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class ServletContext {
	
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		ServletHolder holder = new ServletHolder(DispatcherServlet.class);
		holder.getRegistration().setMultipartConfig(new MultipartConfigElement(""));
		holder.setInitParameter("contextClass", AnnotationConfigWebApplicationContext.class.getName());
		holder.setInitParameter("contextConfigLocation", Config.class.getName());
		holder.setInitOrder(1);
		context.addServlet(holder, "/*");
		
		server.start();
		server.join();
	}

}
```

Our Jetty server is configured to listen on port 8080. 

We are also registering an instance of Spring's `DispatcherServlet`. 

We have to add in a `MultipartConfigElement` (which would be `<multipart-config>` in web.xml) to turn on multi-part file upload support. 

Finally, we need to set a couple of init parameters in order to keep all configuration in pure Java.


Creating a Configuration Class
------------------------------
Now that we have setup `ServletContext` as a simple, runnable application, we need to configure the Spring application context.

In our Spring configuration, we'll need to enable annotation-oriented Spring MVC. And we'll also need to tell Spring where it can find our endpoint controller class. The following configuration class takes care of both of those things:

```java
package fileupload;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan
public class Config {
	
	@Bean
	public StandardServletMultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}
	
}
```

`@ComponentScan` will find the controllers based on `@Controller` annotations without having to specify them here.

To support multi-part file uploads, we need to register a `StandardServletMultipartResolver` as shown above.

Now that we've configured everything, let's create the endpoint controller that will serve it.

Creating a File Upload Controller
------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following Spring MVC controller handles a `GET /upload` request by returning a simple message:

```java
package fileupload;

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
				return "redirect:uploadFailure";
			}
		} else {
			return "redirect:uploadFailure";
		}
	}
	
}
```

First of all, this entire class is marked up with `@Controller` so Spring MVC can pick it up and look for routes.

Next, each method has been tagged with `@RequestMapping` to flag the path and the REST action. In this case, `GET` will return back a very simple message indicating the `POST` operation is available.

The `handleFileUpload` method is where the key parts are. First of all, we have it gears to handle a two-part message: name and file. We check to make sure the file is not empty, and if not, we grab the bytes. Next, we write them out through a `BufferedOutputStream`. Finally, we append **-uploaded** to the target filename to clearly see when a file has been uploaded.

> In a real world solution, we would more likely store the files in some temporary location, a database, of perhaps a NoSQL store like Mongo's GridFS. We would also need some controls in place to avoid filling up the filesystem while also protecting us from vulnerabilities such as uploading executables.

Building and Running the File Upload Server
-------------------------------------------
With everything in place, let's launch our server application!

```sh
./gradlew -b server.gradle run
```

It should produce some output like this:

```sh
2013-04-23 15:53:26.171:INFO:oejs.Server:jetty-8.1.10.v20130312
2013-04-23 15:53:26.385:INFO:/:Initializing Spring FrameworkServlet 'org.springframework.web.servlet.DispatcherServlet-1177138282'
Apr 23, 2013 3:53:26 PM org.springframework.web.servlet.FrameworkServlet initServletBean
INFO: FrameworkServlet 'org.springframework.web.servlet.DispatcherServlet-1177138282': initialization started
Apr 23, 2013 3:53:26 PM org.springframework.context.support.AbstractApplicationContext prepareRefresh
INFO: Refreshing WebApplicationContext for namespace 'org.springframework.web.servlet.DispatcherServlet-1177138282-servlet': startup date [Tue Apr 23 15:53:26 CDT 2013]; root of context hierarchy
Apr 23, 2013 3:53:26 PM org.springframework.web.context.support.AnnotationConfigWebApplicationContext loadBeanDefinitions
INFO: Successfully resolved class for [fileupload.Config]
Apr 23, 2013 3:53:26 PM org.springframework.beans.factory.support.DefaultListableBeanFactory preInstantiateSingletons
INFO: Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@28a7f23: defining beans [org.springframework.context.annotation.internalConfigurationAnnotationProcessor,org.springframework.context.annotation.internalAutowiredAnnotationProcessor,org.springframework.context.annotation.internalRequiredAnnotationProcessor,org.springframework.context.annotation.internalCommonAnnotationProcessor,config,org.springframework.context.annotation.ConfigurationClassPostProcessor.importAwareProcessor,fileUploadController,org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration,requestMappingHandlerMapping,mvcContentNegotiationManager,viewControllerHandlerMapping,beanNameHandlerMapping,resourceHandlerMapping,defaultServletHandlerMapping,requestMappingHandlerAdapter,mvcConversionService,mvcValidator,httpRequestHandlerAdapter,simpleControllerHandlerAdapter,handlerExceptionResolver,multipartResolver]; root of factory hierarchy
Apr 23, 2013 3:53:26 PM org.springframework.web.servlet.handler.AbstractHandlerMethodMapping registerHandlerMethod
INFO: Mapped "{[/upload],methods=[GET],params=[],headers=[],consumes=[],produces=[],custom=[]}" onto public java.lang.String fileupload.FileUploadController.provideUploadInfo()
Apr 23, 2013 3:53:26 PM org.springframework.web.servlet.handler.AbstractHandlerMethodMapping registerHandlerMethod
INFO: Mapped "{[/upload],methods=[POST],params=[],headers=[],consumes=[],produces=[],custom=[]}" onto public java.lang.String fileupload.FileUploadController.handleFileUpload(java.lang.String,org.springframework.web.multipart.MultipartFile)
Apr 23, 2013 3:53:27 PM org.springframework.web.servlet.FrameworkServlet initServletBean
INFO: FrameworkServlet 'org.springframework.web.servlet.DispatcherServlet-1177138282': initialization completed in 647 ms
2013-04-23 15:53:27.051:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:8080
```

Great! Now we have a working server that will accept file uploads. Next, let's build a client to send files.

Creating a file uploading client
--------------------------------
The easiest way to create a file uploader is using Spring MVC's `RestTemplate`.

```java
package fileupload;

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

We create a `RestTemplate` and then load up a `MultiValueMap` with the name and the file. We leverage Spring's `FileSystemResource` to properly load the bytes for our file. Then we `POST` it to the server. Because the server was coded to write a textual response straight into the HTTP response, we can print it out to the screen.

> In more sophisticated applications, it's possible to implement views on the server, in which case we would be seeing a page of HTML.

Uploading a file to the server
------------------------------
With the server running in one window, let's open another window and run the client.

```sh
./gradlew -b client.gradle run -Pargs="sample.txt"
```

It should produce some output like this in the client window:

```sh
You successfully upload sample.txt into sample.txt-uploaded !
```

Our controller doesn't print anything out, but returns the message posted to the client.

Next Steps
----------
Congratulations! You have just written a client and server that both handle uploading files using Spring.

