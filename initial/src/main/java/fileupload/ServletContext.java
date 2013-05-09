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
