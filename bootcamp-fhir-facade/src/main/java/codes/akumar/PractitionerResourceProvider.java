package codes.akumar;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;

//import jakarta.annotation.Resource;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.List;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class PractitionerResourceProvider implements IResourceProvider {
 
    private Connection connection;

    /**
    * Constructor
    */
    public PractitionerResourceProvider(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Practitioner.class;
    }

    private Practitioner convertResultSetToPractitioner(ResultSet result) throws SQLException {
        int id = result.getInt("id");
        String first_name = result.getString("first_name");
        String last_name = result.getString("last_name");
        String prefix = result.getString("prefix");
        String suffix = result.getString("suffix");
        String sex = result.getString("sex");

        Practitioner practitioner = new Practitioner();
        practitioner.setId(Integer.toString(id));
        practitioner.addName().setFamily(last_name).addGiven(first_name);
        practitioner.addName().addPrefix(prefix);
        practitioner.addName().addSuffix(suffix);
        
        //translate database codes to FHIR codes for Practioner Sex
        if(sex.length() > 0){
            if (sex.equalsIgnoreCase("m")) {
                practitioner.setGender(Enumerations.AdministrativeGender.MALE);
            } else 
            if(sex.equalsIgnoreCase("f")) {
                practitioner.setGender(Enumerations.AdministrativeGender.FEMALE);
            }
        }
        return practitioner;
    }

    @Read()
   public Practitioner read(@IdParam IdType theId) throws SQLException {
      
      Statement statement = connection.createStatement();
      ResultSet result = statement.executeQuery("SELECT * FROM practitioners WHERE id = " + theId.getIdPart());

      if (result.next()) {
         Practitioner practitioner = convertResultSetToPractitioner(result);
         return practitioner;
      }
      else {
         throw new ResourceNotFoundException(theId);
      }
   }

   @Search
   public List<Practitioner> getAllPractitioners() throws SQLException {
      List<Practitioner> practitioners = new ArrayList<>();
      Statement statement = connection.createStatement();
      ResultSet result = statement.executeQuery("SELECT * FROM practitioners");

      while (result.next()) {
         Practitioner practitioner = convertResultSetToPractitioner(result);
         practitioners.add(practitioner);
      }
      return practitioners;
   }

   @Search
   public List<Practitioner> getPractitionerByName(@RequiredParam(name = Practitioner.SP_NAME) String name) throws SQLException {
      List<Practitioner> practitioners = new ArrayList<>();
      Statement statement = connection.createStatement();

      String Query = "SELECT * FROM practitioners WHERE first_name like '%" + name + "%' OR last_name like '%" + name + "%'";
      ResultSet result = statement.executeQuery(Query);

      while (result.next()) {
         Practitioner practitioner = convertResultSetToPractitioner(result);
         practitioners.add(practitioner);
      }
      return practitioners;
   }
}