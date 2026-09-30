package com.hospital.management.utils;

import com.hospital.management.dto.AppointmentDTO;
import com.hospital.management.dto.PatientDTO;
import com.hospital.management.model.Appointment;
import com.hospital.management.model.Patient;

public class DTOMapper {

    public static AppointmentDTO toAppointmentDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getReason(),
                appointment.getDate(),
                appointment.getPatient().getName()
                );
    }

    public static PatientDTO toPatientDTO( Patient patient) {
        return new PatientDTO (
                patient.getId(),
                patient.getName(),
                patient.getSsn()
        );
    }
}
