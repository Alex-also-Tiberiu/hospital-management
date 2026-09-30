package com.hospital.management.repository;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Patient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @EntityGraph(attributePaths = "patient")
    public List<Appointment> findByReasonIgnoreCase(String reason);

    @EntityGraph(attributePaths = "patient")
    public Optional<Appointment> findTopByPatientOrderByDateDesc(Patient patient);

    public long deleteByPatientSsn(String ssn);
}
