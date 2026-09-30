package com.hospital.management.repository;

import com.hospital.management.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void findBySsn_returnsPatient_whenExists() {
        Patient patient = new Patient("Test", "123");
        testEntityManager.persistAndFlush(patient);
        testEntityManager.clear();

        Optional<Patient> result = patientRepository.findBySsn("123");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test");
        assertThat(result.get().getSsn()).isEqualTo("123");
    }

    @Test
    void findBySsn_returnsEmpty_whenNotExists() {
        Optional<Patient> result = patientRepository.findBySsn("test");
        assertThat(result).isEmpty();
    }
}
