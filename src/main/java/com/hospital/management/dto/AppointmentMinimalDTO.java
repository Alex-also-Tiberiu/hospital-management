package com.hospital.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AppointmentMinimalDTO(
        @NotBlank String reason,
        @NotNull LocalDate date
) {
}
