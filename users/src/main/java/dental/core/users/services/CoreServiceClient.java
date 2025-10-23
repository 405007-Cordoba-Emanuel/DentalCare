package dental.core.users.services;

import dental.core.users.dto.CreateDentistFromUserRequest;
import dental.core.users.dto.CreatePatientFromUserRequest;
import dental.core.users.dto.DentistResponse;
import dental.core.users.dto.PatientResponse;

public interface CoreServiceClient {
    
    DentistResponse createDentistFromUser(CreateDentistFromUserRequest request);
    
    PatientResponse createPatientFromUser(CreatePatientFromUserRequest request);
}
