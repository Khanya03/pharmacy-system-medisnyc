package za.ac.cput.medisnyc.service;

import org.junit.jupiter.api.Test;
import za.ac.cput.medisnyc.domain.Appointment;
import za.ac.cput.medisnyc.domain.AppointmentStatus;
import za.ac.cput.medisnyc.dto.CreateAppointmentRequest;
import za.ac.cput.medisnyc.repository.jpa.AppointmentJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.DoctorJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {

    @Test
    void booksAppointmentWhenDoctorAndPatientExist() {
        AppointmentJpaRepository appointments = mock(AppointmentJpaRepository.class);
        DoctorJpaRepository doctors = mock(DoctorJpaRepository.class);
        PatientJpaRepository patients = mock(PatientJpaRepository.class);
        when(doctors.existsById("DOC-1")).thenReturn(true);
        when(patients.existsById("PAT-1")).thenReturn(true);
        when(appointments.save(any(Appointment.class))).thenAnswer(call -> call.getArgument(0));

        AppointmentService service = new AppointmentService(appointments, doctors, patients);
        CreateAppointmentRequest request = request("PAT-1", "DOC-1");

        Appointment saved = service.bookAppointment(request);

        assertTrue(saved.getAppointmentId().startsWith("APT-"));
        assertEquals("PAT-1", saved.getPatientId());
        assertEquals("DOC-1", saved.getDoctorId());
        assertEquals(AppointmentStatus.SCHEDULED, saved.getStatus());
        assertEquals("Follow-up", saved.getReason());
        verify(appointments).save(any(Appointment.class));
    }

    @Test
    void rejectsBookingWhenDoctorDoesNotExist() {
        AppointmentJpaRepository appointments = mock(AppointmentJpaRepository.class);
        DoctorJpaRepository doctors = mock(DoctorJpaRepository.class);
        PatientJpaRepository patients = mock(PatientJpaRepository.class);
        when(doctors.existsById("DOC-MISSING")).thenReturn(false);

        AppointmentService service = new AppointmentService(appointments, doctors, patients);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.bookAppointment(request("PAT-1", "DOC-MISSING")));

        assertTrue(ex.getMessage().contains("Doctor not found"));
        verify(appointments, never()).save(any());
    }

    @Test
    void rejectsBookingWhenPatientDoesNotExist() {
        AppointmentJpaRepository appointments = mock(AppointmentJpaRepository.class);
        DoctorJpaRepository doctors = mock(DoctorJpaRepository.class);
        PatientJpaRepository patients = mock(PatientJpaRepository.class);
        when(doctors.existsById("DOC-1")).thenReturn(true);
        when(patients.existsById("PAT-MISSING")).thenReturn(false);

        AppointmentService service = new AppointmentService(appointments, doctors, patients);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.bookAppointment(request("PAT-MISSING", "DOC-1")));

        assertTrue(ex.getMessage().contains("Patient not found"));
        verify(appointments, never()).save(any());
    }

    @Test
    void cancelsScheduledAppointment() {
        AppointmentJpaRepository appointments = mock(AppointmentJpaRepository.class);
        Appointment existing = new Appointment.Builder()
                .setAppointmentId("APT-1")
                .setPatientId("PAT-1")
                .setDoctorId("DOC-1")
                .setAppointmentDate(LocalDateTime.now().plusDays(1))
                .setStatus(AppointmentStatus.SCHEDULED)
                .build();
        when(appointments.findById("APT-1")).thenReturn(Optional.of(existing));
        when(appointments.save(any(Appointment.class))).thenAnswer(call -> call.getArgument(0));

        AppointmentService service = new AppointmentService(appointments, mock(DoctorJpaRepository.class),
                mock(PatientJpaRepository.class));

        Appointment cancelled = service.cancelAppointment("APT-1");

        assertEquals(AppointmentStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void refusesInvalidStatusTransition() {
        AppointmentJpaRepository appointments = mock(AppointmentJpaRepository.class);
        Appointment existing = new Appointment.Builder()
                .setAppointmentId("APT-2")
                .setPatientId("PAT-1")
                .setDoctorId("DOC-1")
                .setAppointmentDate(LocalDateTime.now().plusDays(1))
                .setStatus(AppointmentStatus.COMPLETED)
                .build();
        when(appointments.findById("APT-2")).thenReturn(Optional.of(existing));

        AppointmentService service = new AppointmentService(appointments, mock(DoctorJpaRepository.class),
                mock(PatientJpaRepository.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.updateStatus("APT-2", AppointmentStatus.CANCELLED));

        assertTrue(ex.getMessage().contains("cannot change"));
        verify(appointments, never()).save(any());
    }

    private static CreateAppointmentRequest request(String patientId, String doctorId) {
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDate(LocalDateTime.now().plusDays(2));
        request.setReason("Follow-up");
        request.setNotes("Bring previous script");
        return request;
    }
}
