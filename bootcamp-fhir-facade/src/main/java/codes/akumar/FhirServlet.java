package codes.akumar;

//import org.eclipse.jetty.io.Connection;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import jakarta.servlet.ServletException;

import java.sql.Connection;
import java.sql.DriverManager;

public class FhirServlet extends RestfulServer{
    @Override
    protected void initialize() throws ServletException{
        // Create a context for the appropriate version
		setFhirContext(FhirContext.forR4());

        String jdbcUrl = "jdbc:postgresql://localhost:15432/postgres";
        String username = "postgres";
        String password = "postgres";

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            
            registerProvider(new PatientResourceProvider(connection));
            registerProvider(new PractitionerResourceProvider(connection));
            registerProvider(new ObservationResourceProvider(connection));

		    registerInterceptor(new ResponseHighlighterInterceptor());
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
