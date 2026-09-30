package com.hospital.management.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import com.hospital.management.dto.AppointmentDTO;
import com.hospital.management.dto.BulkAppointmentRequestDTO;
import com.hospital.management.service.HospitalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api")
@Slf4j
public class AppointmentController {

    private final HospitalService hospitalService;

    public AppointmentController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    /**
     * Example: {
     *      "patientName" : "Austin Jackson",
     *      "ssn" : "xyz-123",
     *      "appointments": [
     *          {"reason": "Checkup", "date": "2025-02-01"},
     *          {"reason": "X-Ray", "date": "2025-10-15"}
     *       ]
     * }
     */
    @PostMapping("/bulk-appointments")
    public ResponseEntity<List<AppointmentDTO>> createBulkAppointments( @RequestBody @Valid BulkAppointmentRequestDTO payload ) {
        List<AppointmentDTO> result = hospitalService.bulkCreateAppointments(payload);
        // Defensive guard: unreachable today since @NotEmpty guarantees a non-empty list
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/appointments-by-reason")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByReason( @RequestParam String keyword ) {
        List<AppointmentDTO> result = hospitalService.getAppointmentsByReason(keyword);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/patients/{ssn}/appointments")
    public ResponseEntity<String> deleteAppointmentsBySSN( @PathVariable String ssn ) {
        long result = hospitalService.deleteAppointmentsBySSN(ssn);
        if(result < 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>( HttpStatus.NO_CONTENT);
    }

    @GetMapping("/appointments/latest")
    public ResponseEntity<AppointmentDTO> getLatestAppointment( @RequestParam String ssn ) {
        Optional<AppointmentDTO> result = hospitalService.findLatestAppointmentBySSN(ssn);
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result.get(), HttpStatus.OK);
    }
}
