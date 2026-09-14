package za.ac.cput.medisnyc.service;

/* DoctorService.java
   Module 2: doctor directory - lookup for the patient-facing booking flow,
   and creation for admins adding a doctor to the practice.
   Author: Lisakhanya Mpahla
*/

import za.ac.cput.medisnyc.domain.Doctor;
import za.ac.cput.medisnyc.factory.DoctorFactory;
import za.ac.cput.medisnyc.repository.jpa.DoctorJpaRepository;
import za.ac.cput.medisnyc.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorJpaRepository doctorRepository;

    @Autowired
    public DoctorService(DoctorJpaRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> getAll() {
        return doctorRepository.findAll();
    }

    public Doctor getById(String doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
    }

    public Doctor create(String firstName, String lastName, String specialization,
                         String phoneNumber, String email) {
        String doctorId = Helper.generateId("DOC");
        Doctor doctor = DoctorFactory.createDoctor(doctorId, firstName, lastName, specialization, phoneNumber, email);
        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Couldn't create doctor - check first/last name, specialization, phone number and email are all valid.");
        }
        return doctorRepository.save(doctor);
    }
}
