package com.hospital.management.service;

import com.hospital.management.dto.AppointmentDTO;
import com.hospital.management.dto.AppointmentMinimalDTO;
import com.hospital.management.dto.BulkAppointmentRequestDTO;
import com.hospital.management.model.Appointment;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.AppointmentRepository;
import com.hospital.management.repository.PatientRepository;
import com.hospital.management.utils.HospitalUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HospitalServiceTest {

    @Mock
    private PatientRepository patientRepo;
    @Mock
    private AppointmentRepository appointmentRepo;
    @Mock
    private HospitalUtils hospitalUtils;

    @InjectMocks
    private HospitalService hospitalService;

    private final Patient patient = new Patient(
            "Test",
            "123"
    );
    private final AppointmentMinimalDTO appointmentMinimalDTO = new AppointmentMinimalDTO(
            "checkup",
            LocalDate.of(2026,10,1)
    );
    private final BulkAppointmentRequestDTO bulkDTO = new BulkAppointmentRequestDTO(
            patient.getName(),
            patient.getSsn(),
            List.of(appointmentMinimalDTO)
    );
    private final Appointment appointment = new Appointment(
            "checkup",
            LocalDate.of(2026,10,1),
            patient
    );

    @Test
    public void bulkCreateAppointments_createNewUser_ifNotExists() {
        when(patientRepo.findBySsn("123")).thenReturn(Optional.empty());
        hospitalService.bulkCreateAppointments(bulkDTO);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepo).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Test");
        assertThat(captor.getValue().getSsn()).isEqualTo("123");
    }

    @Test
    public void bulkCreateAppointments_findUser_ifExists() {
        when(patientRepo.findBySsn("123")).thenReturn(Optional.of(patient));
        hospitalService.bulkCreateAppointments(bulkDTO);

        verify(patientRepo, never()).save(any());
    }

    @Test
    public void bulkCreateAppointments_saveAll_verifyArgument() {
        when(patientRepo.findBySsn("123")).thenReturn(Optional.of(patient));
        when(appointmentRepo.saveAll(any())).thenReturn(List.of(appointment));
        hospitalService.bulkCreateAppointments(bulkDTO);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Appointment>> captor = ArgumentCaptor.forClass(List.class);
        verify(appointmentRepo).saveAll(captor.capture());
        List<Appointment> savedAppointments = captor.getValue();

        assertThat(savedAppointments).hasSize(1);
        assertThat(savedAppointments.getFirst().getReason()).isEqualTo("checkup");
        assertThat(savedAppointments.getFirst().getDate()).isEqualTo(LocalDate.of(2026,10,1));
        assertThat(savedAppointments.getFirst().getPatient().getName()).isEqualTo("Test");
        assertThat(savedAppointments.getFirst().getPatient().getSsn()).isEqualTo("123");
    }

    @Test
    public void bulkCreateAppointments_saveAll_verifyResults() {
        List<AppointmentDTO> result = hospitalService.bulkCreateAppointments(bulkDTO);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().reason()).isEqualTo("checkup");
        assertThat(result.getFirst().date()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(result.getFirst().patientName()).isEqualTo("Test");
    }

    @Test
    public void bulkCreateAppointments_saveAll_invokeRecordUsage() {
        hospitalService.bulkCreateAppointments(bulkDTO);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(hospitalUtils).recordUsage(captor.capture());
        assertThat(captor.getValue()).isEqualTo("Bulk create appointments");
    }

    @Test
    public void getAppointmentsByReason_verifyResults() {
        when(appointmentRepo.findByReasonIgnoreCase("checkup")).thenReturn(List.of(appointment));

        List<AppointmentDTO> result = hospitalService.getAppointmentsByReason("checkup");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().reason()).isEqualTo("checkup");
    }

    @Test
    public void getAppointmentsByReason_verifyEmptyResults() {
        List<AppointmentDTO> result = hospitalService.getAppointmentsByReason("checkup");
        assertThat(result).isEmpty();
    }
    @Test
    public void getAppointmentsByReason_invokeRecordUsage() {
        when(appointmentRepo.findByReasonIgnoreCase(any())).thenReturn(List.of(appointment));

        hospitalService.getAppointmentsByReason("test");
        verify(hospitalUtils).recordUsage(anyString());
    }

    @Test
    public void deleteAppointmentBySSN_notFound() {
        long result = hospitalService.deleteAppointmentsBySSN("123");
        assertThat(result).isEqualTo(-1);
        verify(appointmentRepo, never()).deleteByPatientSsn(any());
    }

    @Test
    public void deleteAppointmentBySSN_found() {
        when(patientRepo.findBySsn(anyString())).thenReturn(Optional.of(patient));
        when(appointmentRepo.deleteByPatientSsn(anyString())).thenReturn(1L);
        long result = hospitalService.deleteAppointmentsBySSN("123");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(appointmentRepo).deleteByPatientSsn(captor.capture());
        assertThat(captor.getValue()).isEqualTo("123");
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void deleteAppointmentBySSN_foundWithZeroAppointments() {
        Patient test = new Patient("test", "333");
        when(patientRepo.findBySsn(any())).thenReturn(Optional.of(test));

        long result = hospitalService.deleteAppointmentsBySSN("333");
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void findLatestAppointmentBySSN_notFound() {
        Optional<AppointmentDTO> result = hospitalService.findLatestAppointmentBySSN("123");

        verify(patientRepo).findBySsn("123");
        verify(appointmentRepo, never()).findTopByPatientOrderByDateDesc(any());
        assertThat(result).isEmpty();
    }

    @Test
    public void findLatestAppointmentBySSN_foundZeroAppointments() {
        when(patientRepo.findBySsn("123")).thenReturn(Optional.of(patient));
        Optional<AppointmentDTO> result = hospitalService.findLatestAppointmentBySSN("123");

        verify(patientRepo).findBySsn("123");
        verify(appointmentRepo).findTopByPatientOrderByDateDesc(any());
        assertThat(result).isEmpty();
    }

    @Test
    public void findLatestAppointmentBySSN_foundWithAppointments() {
        when(patientRepo.findBySsn("123")).thenReturn(Optional.of(patient));
        when(appointmentRepo.findTopByPatientOrderByDateDesc(any())).thenReturn(Optional.of(appointment));
        Optional<AppointmentDTO> result = hospitalService.findLatestAppointmentBySSN("123");

        verify(patientRepo).findBySsn("123");
        verify(appointmentRepo).findTopByPatientOrderByDateDesc(any());
        assertThat(result).isNotEmpty();
        assertThat(result.get().reason()).isEqualTo("checkup");
        assertThat(result.get().date()).isEqualTo(LocalDate.of(2026,10,1));
    }
}
