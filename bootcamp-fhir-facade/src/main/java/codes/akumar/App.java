package codes.akumar;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) throws Exception 
    {
         // Create a server that listens on port 8080.
        Server server = new Server(8091);

        FhirServlet fhirServlet = new FhirServlet();

        ServletContextHandler handler = new ServletContextHandler();
        handler.addServlet(new ServletHolder(fhirServlet),"/*");
        
        server.setHandler(handler);        

        server.start();
        server.join();
        
    }
}
