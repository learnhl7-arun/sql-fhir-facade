package codes.akumar;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Quantity;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class ObservationResourceProvider implements IResourceProvider{

    private Connection connection;

    public ObservationResourceProvider(Connection connection) {
        this.connection = connection;

    }

    private Observation convertHeartRateToObservation(ResultSet result) throws SQLException {
        Observation observation = new Observation();

        //Get values of ResultSet into individual variables
        int id = result.getInt("id");
        int patient_id = result.getInt("patient_id");
        Date date = result.getDate("date");
        int heart_rate = result.getInt("rate");

        //Static Values
        observation.getCode().addCoding().setSystem("http://loinc.org").setCode("8867-4").setDisplay("Heart rate");
        observation.setStatus(Observation.ObservationStatus.FINAL);

        //Dynamic Values
        observation.setId("hr-" + String.valueOf(id));
        observation.getSubject().setReference("Patient/" + String.valueOf(patient_id));
        observation.setEffective(new DateTimeType(date));
        observation.getValueQuantity().setValue(heart_rate).setUnit("beats/min");
        return observation;
    }    

    private Observation convertBloodPressureToObservation(ResultSet result) throws SQLException {
        Observation observation = new Observation();

        //Get values of ResultSet into individual variables
        int id = result.getInt("id");
        int patient_id = result.getInt("patient_id");
        Date date = result.getDate("date");
        int systolic = result.getInt("systolic");
        int diastolic = result.getInt("diastolic");

        //Static Values
        observation.getCode().setText("Blood pressure panel with all children optional");
        observation.getCode().addCoding().setSystem("http://loinc.org").setCode("85354-9").setDisplay("Blood pressure panel with all children optional");
        observation.setStatus(Observation.ObservationStatus.FINAL);

        //Dynamic Values
        observation.setId("bp-" + String.valueOf(id));
        observation.getSubject().setReference("Patient/" + String.valueOf(patient_id));
        observation.setEffective(new DateTimeType(date));

        // Add Systolic Component
        ObservationComponentComponent systolicComponent = new ObservationComponentComponent();
        systolicComponent.getCode().addCoding().setSystem("http://loinc.org").setCode("8480-6").setDisplay("Systolic blood pressure");
        systolicComponent.setValue(new Quantity().setValue(systolic).setUnit("mmHg"));
        observation.addComponent(systolicComponent);

        // Add Diastolic Component
        ObservationComponentComponent diastolicComponent = new ObservationComponentComponent();
        diastolicComponent.getCode().addCoding().setSystem("http://loinc.org").setCode("8462-4").setDisplay("Diastolic blood pressure");
        diastolicComponent.setValue(new Quantity().setValue(diastolic).setUnit("mmHg"));
        observation.addComponent(diastolicComponent);
        return observation;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Observation.class;
    }

    @Search
    public List<Observation> getObservations() throws SQLException{

        List<Observation> observations = new ArrayList<Observation>();
        Statement statement = connection.createStatement();
        
        //Add Heart Rate Observations
        ResultSet hrResults = statement.executeQuery("select * from heart_rate");
        while(hrResults.next()){
            Observation observation = convertHeartRateToObservation(hrResults);
            observations.add(observation);
        }

        //Add Blood Pressure Observations
        ResultSet bpResults = statement.executeQuery("select * from blood_pressure");
        while(bpResults.next()){
            Observation observation = convertBloodPressureToObservation(bpResults);
            observations.add(observation);
        }
        return observations;
    }

    @Search
    public List<Observation> getObservationsForPatient(@RequiredParam(name = Observation.SP_PATIENT) String pat_id) throws SQLException {
        List<Observation> observations = new ArrayList<Observation>();
        Statement statement = connection.createStatement();

        //Add Heart Rate Observations
        String Query_hr = "select * from heart_rate where patient_id = '" + pat_id + "'";
        ResultSet hrResults = statement.executeQuery(Query_hr);

        while(hrResults.next()){
            Observation observation = convertHeartRateToObservation(hrResults);
            observations.add(observation);
        }

        //Add Blood Pressure Observations
        String Query_bp = "select * from blood_pressure where patient_id = '" + pat_id + "'";
        ResultSet bpResults = statement.executeQuery(Query_bp);
        while(bpResults.next()){
            Observation observation = convertBloodPressureToObservation(bpResults);
            observations.add(observation);
        }
        return observations;
    }

    @Read
    public Observation getObservation(@IdParam IdType id) throws SQLException {
    String[] parts = id.getIdPart().split("-");
    String prefix = parts[1];
    String suffix = parts[0];

    if (suffix.equals("bp")) {
      Statement statement = connection.createStatement();
      ResultSet result = statement.executeQuery("SELECT * from blood_pressure WHERE id = " + prefix + ";");
      if (result.next()) {
        return convertBloodPressureToObservation(result);
      }
    } else if (suffix.equals("hr")) {
      Statement statement = connection.createStatement();
      ResultSet result = statement.executeQuery("SELECT * from heart_rate WHERE id = " + prefix + ";");
      if (result.next()) {
        return convertHeartRateToObservation(result);
      }
    }
    throw new ResourceNotFoundException(id);
    }
}