package com.hospital.management.repository;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AppointmentRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    public void findByReason_returnsAppointments_whenExists() {
        Patient patient1 = new Patient("Test1", "123");
        Patient patient2 = new Patient("Test2", "456");
        testEntityManager.persistAndFlush(patient1);
        testEntityManager.persistAndFlush(patient2);

        Appointment appointment1 = new Appointment("Checkup", LocalDate.now() ,patient1);
        Appointment appointment2 = new Appointment("CHECKUP", LocalDate.now(), patient2);
        testEntityManager.persistAndFlush(appointment1);
        testEntityManager.persistAndFlush(appointment2);
        testEntityManager.clear();

        List<Appointment> result = appointmentRepository.findByReasonIgnoreCase("checkup");
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        for(Appointment a : result) {
            assertThat(a.getReason()).isEqualToIgnoringCase("checkup");
        }
    }

    @Test
    public void findByReason_returnsEmpty_whenNotExists() {
        List<Appointment> result = appointmentRepository.findByReasonIgnoreCase("checkup");
        assertThat(result).isEmpty();
    }

    @Test
    public void findTopByPatientOrderByDateDesc_returnsAppointments_whenExists() {
        Patient patient = new Patient("Test", "1234");
        testEntityManager.persistAndFlush(patient);

        Appointment appointment1 = new Appointment("checkup - 1", LocalDate.of(2026,1,1), patient);
        Appointment appointment2 = new Appointment("checkup - 2", LocalDate.of(2025,1,1), patient);
        testEntityManager.persistAndFlush(appointment1);
        testEntityManager.persistAndFlush(appointment2);
        testEntityManager.clear();

        Optional<Appointment> result = appointmentRepository.findTopByPatientOrderByDateDesc(patient);
        assertThat(result).isPresent();
        assertThat(result.get().getReason()).isEqualTo("checkup - 1");
    }

    @Test
    public void findTopByPatientOrderByDateDesc_returnsEmpty_whenNotExists() {
        Patient patient = new Patient("Test", "1234");
        testEntityManager.persistAndFlush(patient);
        testEntityManager.clear();

        Optional<Appointment> result = appointmentRepository.findTopByPatientOrderByDateDesc(patient);
        assertThat(result).isEmpty();
    }

    @Test
    public void deleteByPatientSsn_success_whenExists() {
        Patient patient = new Patient("Test", "123");
        testEntityManager.persistAndFlush(patient);

        Appointment appointment1 = new Appointment("checkup - 1", LocalDate.of(2026,1,1), patient);
        Appointment appointment2 = new Appointment("checkup - 2", LocalDate.of(2026,1,2), patient);
        testEntityManager.persistAndFlush(appointment1);
        testEntityManager.persistAndFlush(appointment2);
        testEntityManager.clear();

        long result = appointmentRepository.deleteByPatientSsn(patient.getSsn());
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void deleteByPatientSsn_success_whenNotExists() {
        Patient patient = new Patient("Test", "123");
        testEntityManager.persistAndFlush(patient);
        testEntityManager.clear();

        long result = appointmentRepository.deleteByPatientSsn(patient.getSsn());
        assertThat(result).isEqualTo(0);
    }
}
