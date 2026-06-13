package com.cymelle.ops.fare.controller;

import com.cymelle.ops.fare.config.FareProperties;
import com.cymelle.ops.fare.dto.FareRequest;
import com.cymelle.ops.fare.dto.FareResponse;
import com.cymelle.ops.fare.service.FareCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/fare")
@RequiredArgsConstructor
@Tag(name = "Fare Calculation", description = "Endpoints for calculating trip fares")
public class FareController {

    private final FareCalculationService fareCalculationService;

    /**
     * Calculate fare for a trip.
     * GET /fare/calculate?distanceKm=10&surgeMultiplier=1.5
     */
    @GetMapping("/calculate")
    @Operation(summary = "Calculate fare", description = "Calculate fare based on distance and optional surge multiplier")
    public ResponseEntity<FareResponse> calculateFare(
            @Valid @ModelAttribute FareRequest request) {
        log.info("GET /fare/calculate - Calculating fare for distance: {} km", request.getDistanceKm());

        BigDecimal finalFare = fareCalculationService.calculateFare(
                request.getDistanceKm(),
                request.getSurgeMultiplier());

        FareProperties config = fareCalculationService.getFareConfiguration();
        BigDecimal distanceCharge = BigDecimal.valueOf(request.getDistanceKm())
                .multiply(config.getRatePerKm());

        FareResponse response = FareResponse.builder()
                .distanceKm(request.getDistanceKm())
                .baseFare(config.getBaseFare())
                .distanceCharge(distanceCharge)
                .surgeMultiplier(request.getSurgeMultiplier() != null 
                        ? request.getSurgeMultiplier() 
                        : "1.0")
                .minimumFare(config.getMinimumFare())
                .finalFare(finalFare)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate fare via POST (for complex scenarios).
     * POST /fare/calculate
     */
    @PostMapping("/calculate")
    @Operation(summary = "Calculate fare (POST)", description = "Calculate fare using POST request")
    public ResponseEntity<FareResponse> calculateFarePost(
            @Valid @RequestBody FareRequest request) {
        log.info("POST /fare/calculate - Calculating fare for distance: {} km", request.getDistanceKm());

        BigDecimal finalFare = fareCalculationService.calculateFare(
                request.getDistanceKm(),
                request.getSurgeMultiplier());

        FareProperties config = fareCalculationService.getFareConfiguration();
        BigDecimal distanceCharge = BigDecimal.valueOf(request.getDistanceKm())
                .multiply(config.getRatePerKm());

        FareResponse response = FareResponse.builder()
                .distanceKm(request.getDistanceKm())
                .baseFare(config.getBaseFare())
                .distanceCharge(distanceCharge)
                .surgeMultiplier(request.getSurgeMultiplier() != null 
                        ? request.getSurgeMultiplier() 
                        : "1.0")
                .minimumFare(config.getMinimumFare())
                .finalFare(finalFare)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get current fare configuration.
     * GET /fare/config
     */
    @GetMapping("/config")
    @Operation(summary = "Get fare configuration", description = "Retrieve current fare calculation configuration")
    public ResponseEntity<FareProperties> getFareConfig() {
        log.info("GET /fare/config - Fetching fare configuration");
        return ResponseEntity.ok(fareCalculationService.getFareConfiguration());
    }

}
