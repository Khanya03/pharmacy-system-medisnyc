package za.ac.cput.medisnyc.repository;

/* IDoctorRepository.java
   Doctor repository interface
   Author: Lisakhanya Mpahla 230126669
   Date: 25 March 2026
*/

import za.ac.cput.medisnyc.domain.Doctor;
import java.util.List;

public interface DoctorRepository extends IRepository<Doctor, String> {
    List<Doctor> findBySpecialization(String specialization);
    List<Doctor> findByLastName(String lastName);
    List<Doctor> findByFirstName(String firstName);
}