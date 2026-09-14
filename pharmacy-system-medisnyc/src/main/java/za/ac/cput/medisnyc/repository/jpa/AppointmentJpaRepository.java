package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.Appointment;
import za.ac.cput.medisnyc.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentJpaRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findByPatientId(String patientId);
    List<Appointment> findByDoctorId(String doctorId);
    List<Appointment> findByStatus(AppointmentStatus status);
}
