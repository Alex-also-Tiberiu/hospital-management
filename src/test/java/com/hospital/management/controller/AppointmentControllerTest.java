package com.hospital.management.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.dto.AppointmentDTO;
import com.hospital.management.dto.AppointmentMinimalDTO;
import com.hospital.management.dto.BulkAppointmentRequestDTO;
import com.hospital.management.service.HospitalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HospitalService hospitalService;

    @Autowired
    private ObjectMapper objectMapper;

    private final AppointmentDTO appointmentDTO = new AppointmentDTO(
            1L,
            "checkup",
            LocalDate.of(2025,2,1),
            "Test"
    );
    private final AppointmentMinimalDTO appointmentMinimalDTO = new AppointmentMinimalDTO("checkup", LocalDate.of(2025, 2, 1));

    private final BulkAppointmentRequestDTO bulkRequest = new BulkAppointmentRequestDTO(
            "Test",
            "123",
            List.of(appointmentMinimalDTO)
    );

    @Test
    public void bulkAppointments_return201WithResults() throws Exception {
        when(hospitalService.bulkCreateAppointments(any())).thenReturn(List.of(appointmentDTO));

        mockMvc.perform(
                post("/api/bulk-appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkRequest))
        ).andExpect(status().isCreated());

        verify(hospitalService).bulkCreateAppointments(any());
    }

    @Test
    public void bulkAppointments_return400_withPayloadValidation() throws Exception {
        BulkAppointmentRequestDTO invalidRequest = new BulkAppointmentRequestDTO(
                "",
                "123",
                List.of(appointmentMinimalDTO)
        );

        mockMvc.perform(
                post("/api/bulk-appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
        ).andExpect(status().isBadRequest());

        verify(hospitalService, never()).bulkCreateAppointments(any());
    }

    @Test
    public void bulkAppointments_return400_withMalformedPayload() throws Exception {
        String malformedJson = """                                                                                                                                                                                                                                                                                    
          {
              "patientName": "Test",
              "ssn": "123",
              "appointments": [
                  {"reason": "checkup", "date": "09/30/2026"}
              ]
          }
          """;

        mockMvc.perform(
                post("/api/bulk-appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson)
        ).andExpect(status().isBadRequest());

        verify(hospitalService, never()).bulkCreateAppointments(any());
    }

    @Test
    public void getAppointmentsByReason_returns200WithResults() throws Exception {
        when(hospitalService.getAppointmentsByReason("checkup")).thenReturn(List.of(appointmentDTO));

        mockMvc.perform(
                get("/api/appointments-by-reason")
                        .param("keyword", "checkup")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("checkup"));
    }

    @Test
    public void getAppointmentsByReason_returns200WithZeroResults() throws Exception {
        when(hospitalService.getAppointmentsByReason("checkup")).thenReturn(List.of());

        mockMvc.perform(
                get("/api/appointments-by-reason")
                        .param("keyword", "checkup")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void deleteAppointments_return204() throws Exception {
        when(hospitalService.deleteAppointmentsBySSN("123")).thenReturn(1L);

        mockMvc.perform(
                delete("/api/patients/{ssn}/appointments", "123")
        ).andExpect(status().isNoContent());
    }

    @Test
    public void deleteAppointments_return404() throws Exception {
        when(hospitalService.deleteAppointmentsBySSN("123")).thenReturn(-1L);

        mockMvc.perform(
                delete("/api/patients/{ssn}/appointments", "123")
        ).andExpect(status().isNotFound());
    }

    @Test
    public void latestAppointment_return200() throws Exception {
        when(hospitalService.findLatestAppointmentBySSN("123")).thenReturn(Optional.of(appointmentDTO));

        mockMvc.perform(
                get("/api/appointments/latest")
                        .param("ssn", "123")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void latestAppointment_return404() throws Exception {
        when(hospitalService.findLatestAppointmentBySSN(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/api/appointments/latest")
                        .param("ssn", "123")
        ).andExpect(status().isNotFound());
    }
}
