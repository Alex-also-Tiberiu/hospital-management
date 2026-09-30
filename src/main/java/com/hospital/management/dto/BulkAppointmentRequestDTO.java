package com.hospital.management.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record BulkAppointmentRequestDTO(
        @NotBlank String patientName,
        @NotBlank String ssn,
        @Valid @NotEmpty List<AppointmentMinimalDTO> appointments
) {
}
