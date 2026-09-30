package com.hospital.management.service;

import lombok.extern.slf4j.Slf4j;
import com.hospital.management.dto.AppointmentDTO;
import com.hospital.management.dto.AppointmentMinimalDTO;
import com.hospital.management.dto.BulkAppointmentRequestDTO;
import com.hospital.management.model.Appointment;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.AppointmentRepository;
import com.hospital.management.repository.PatientRepository;
import com.hospital.management.utils.DTOMapper;
import com.hospital.management.utils.HospitalUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class HospitalService {

    private final PatientRepository patientRepo;
    private final AppointmentRepository appointmentRepo;
    private final HospitalUtils hospitalUtils;

    public HospitalService(PatientRepository patientRepo, AppointmentRepository appointmentRepo, HospitalUtils hospitalUtils) {
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
        this.hospitalUtils = hospitalUtils;
    }

    @Transactional
    public List<AppointmentDTO> bulkCreateAppointments( BulkAppointmentRequestDTO bulkAppoint ) {
        Optional<Patient> opt = findPatientBySSN(bulkAppoint.ssn());
        Patient pat;
        if (opt.isEmpty()) {
            log.info("Creating new patient");
            pat = new Patient(bulkAppoint.patientName(), bulkAppoint.ssn());
            patientRepo.save(pat);
        } else {
            pat = opt.get();
            log.info("Existing patient");
        }

        List<Appointment> createdAppointments = new ArrayList<>();

        for (AppointmentMinimalDTO app: bulkAppoint.appointments()) {
            Appointment newAppoint = new Appointment(app.reason(), app.date(), pat);
            createdAppointments.add(newAppoint);
        }

        List<Appointment> saved = appointmentRepo.saveAll(createdAppointments);

        for (Appointment appointment : saved) {
            log.info("Created appointment for reason: {} [Date: {}] ", appointment.getReason(), appointment.getDate());
        }

        hospitalUtils.recordUsage("Bulk create appointments");

        return createdAppointments
                .stream()
                .map(DTOMapper::toAppointmentDTO)
                .toList();
    }

    public Optional<Patient> findPatientBySSN(String ssn) {
        Optional<Patient> patient = patientRepo.findBySsn(ssn);
        if(patient.isEmpty()) {
            log.debug("No patient found");
        }
        return  patient;
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByReason(String reasonKeyword) {
        List<Appointment> listAppointments = appointmentRepo.findByReasonIgnoreCase(reasonKeyword);
        if (ObjectUtils.isEmpty(listAppointments)) {
            return new ArrayList<>();
        }
        hospitalUtils.recordUsage("Get appointments by reason");

        return listAppointments
                .stream()
                .map(DTOMapper::toAppointmentDTO)
                .toList();
    }

    @Transactional
    public long deleteAppointmentsBySSN(String ssn) {
        Optional<Patient> patient = findPatientBySSN(ssn);
        if (patient.isEmpty()) {
            return - 1;
        }
        return appointmentRepo.deleteByPatientSsn(ssn);
    }

    @Transactional(readOnly = true)
    public Optional<AppointmentDTO> findLatestAppointmentBySSN( String ssn) {
        Optional<Patient> patient = findPatientBySSN(ssn);
        if(patient.isEmpty()) {
            return Optional.empty();
        }
        Optional<Appointment> latest = appointmentRepo.findTopByPatientOrderByDateDesc(patient.get());
        if(latest.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(DTOMapper.toAppointmentDTO(latest.get()));
    }
}
