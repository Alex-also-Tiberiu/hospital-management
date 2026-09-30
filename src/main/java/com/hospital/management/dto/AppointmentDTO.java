package com.hospital.management.dto;

import java.time.LocalDate;

public record AppointmentDTO(
        Long id,
        String reason,
        LocalDate date,
        String patientName
) {}
