-- Get all Patients
select id, first_name, last_name, date_of_birth 
from patients p 

-- Get a single patient
select id, first_name, last_name, date_of_birth 
from patients p 
where p.id = 1

-- Get Heart Rate for Patient 1
select * from heart_rate hr where hr.patient_id = 1; 

select * from blood_pressure bp where bp.patient_id =1; 

-- Get a single Heart Rate record
select * from heart_rate hr where hr.id = 1;

