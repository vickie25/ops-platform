package com.cymelle.ops.fare.service;

import com.cymelle.ops.fare.config.FareProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class FareCalculationService {

    private final FareProperties fareProperties;

    /**
     * Calculate fare with all factors.
     *
     * Calculation logic:
     * 1. fare = baseFare + (distanceKm * ratePerKm)
     * 2. fare = fare * surgeMultiplier
     * 3. if fare < minimumFare, return minimumFare
     *
     * @param distanceKm       Distance in kilometers (must be positive)
     * @param surgeMultiplier  Surge multiplier (optional, defaults to 1.0)
     * @return Calculated fare amount
     */
    public BigDecimal calculateFare(Double distanceKm, BigDecimal surgeMultiplier) {
        log.info("Calculating fare for distance: {} km, surge multiplier: {}", distanceKm, surgeMultiplier);

        // Validate distance
        if (distanceKm == null || distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be greater than 0");
        }

        // Use default surge multiplier if not provided
        if (surgeMultiplier == null) {
            surgeMultiplier = fareProperties.getDefaultSurgeMultiplier();
        }

        // Step 1: Calculate base fare + distance charge
        BigDecimal distanceBD = BigDecimal.valueOf(distanceKm);
        BigDecimal distanceCharge = distanceBD.multiply(fareProperties.getRatePerKm());
        BigDecimal fare = fareProperties.getBaseFare().add(distanceCharge);

        log.debug("Base fare: {}, Distance charge: {}, Subtotal: {}", 
                fareProperties.getBaseFare(), distanceCharge, fare);

        // Step 2: Apply surge multiplier
        fare = fare.multiply(surgeMultiplier);

        log.debug("After surge multiplier ({}): {}", surgeMultiplier, fare);

        // Step 3: Apply minimum fare
        if (fare.compareTo(fareProperties.getMinimumFare()) < 0) {
            log.debug("Fare {} is below minimum {}, applying minimum", fare, fareProperties.getMinimumFare());
            fare = fareProperties.getMinimumFare();
        }

        // Round to 2 decimal places
        fare = fare.setScale(2, RoundingMode.HALF_UP);

        log.info("Final calculated fare: {}", fare);
        return fare;
    }

    /**
     * Calculate fare without surge (uses default surge multiplier of 1.0).
     */
    public BigDecimal calculateFare(Double distanceKm) {
        return calculateFare(distanceKm, BigDecimal.ONE);
    }

    /**
     * Calculate fare with custom surge multiplier (from string).
     */
    public BigDecimal calculateFare(Double distanceKm, String surgeMultiplierStr) {
        BigDecimal surgeMultiplier = null;
        if (surgeMultiplierStr != null && !surgeMultiplierStr.isEmpty()) {
            try {
                surgeMultiplier = new BigDecimal(surgeMultiplierStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid surge multiplier: {}, using default", surgeMultiplierStr);
                surgeMultiplier = fareProperties.getDefaultSurgeMultiplier();
            }
        }
        return calculateFare(distanceKm, surgeMultiplier);
    }

    /**
     * Get current fare configuration.
     */
    public FareProperties getFareConfiguration() {
        return fareProperties;
    }

}
