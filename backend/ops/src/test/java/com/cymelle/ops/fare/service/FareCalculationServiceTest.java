package com.cymelle.ops.fare.service;

import com.cymelle.ops.fare.config.FareProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Fare Calculation Service Tests")
public class FareCalculationServiceTest {

    @Mock
    private FareProperties fareProperties;

    @InjectMocks
    private FareCalculationService fareCalculationService;

    @BeforeEach
    void setUp() {
        // Setup default fare configuration
        when(fareProperties.getBaseFare()).thenReturn(new BigDecimal("50.00"));
        when(fareProperties.getRatePerKm()).thenReturn(new BigDecimal("10.00"));
        when(fareProperties.getMinimumFare()).thenReturn(new BigDecimal("100.00"));
        when(fareProperties.getDefaultSurgeMultiplier()).thenReturn(BigDecimal.ONE);
    }

    @Test
    @DisplayName("Should calculate normal fare without surge")
    void shouldCalculateNormalFare() {
        // Given: 10 km distance
        Double distanceKm = 10.0;
        
        // When: Calculate fare without surge
        BigDecimal fare = fareCalculationService.calculateFare(distanceKm);
        
        // Then: fare = 50 + (10 * 10) = 150
        assertThat(fare).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should calculate fare with surge multiplier")
    void shouldCalculateSurgeFare() {
        // Given: 10 km distance and 1.5x surge
        Double distanceKm = 10.0;
        BigDecimal surgeMultiplier = new BigDecimal("1.5");
        
        // When: Calculate fare with surge
        BigDecimal fare = fareCalculationService.calculateFare(distanceKm, surgeMultiplier);
        
        // Then: fare = (50 + (10 * 10)) * 1.5 = 150 * 1.5 = 225
        assertThat(fare).isEqualTo(new BigDecimal("225.00"));
    }

    @Test
    @DisplayName("Should apply minimum fare when calculated fare is below minimum")
    void shouldApplyMinimumFare() {
        // Given: 1 km distance (very short trip)
        Double distanceKm = 1.0;
        
        // When: Calculate fare (50 + (1 * 10) = 60, below minimum 100)
        BigDecimal fare = fareCalculationService.calculateFare(distanceKm);
        
        // Then: Should return minimum fare of 100
        assertThat(fare).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should apply minimum fare even with surge multiplier")
    void shouldApplyMinimumFareWithSurge() {
        // Given: 2 km distance with 1.2x surge
        Double distanceKm = 2.0;
        BigDecimal surgeMultiplier = new BigDecimal("1.2");
        
        // When: Calculate fare (50 + (2 * 10)) * 1.2 = 70 * 1.2 = 84, below minimum 100
        BigDecimal fare = fareCalculationService.calculateFare(distanceKm, surgeMultiplier);
        
        // Then: Should return minimum fare of 100
        assertThat(fare).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should calculate high surge correctly")
    void shouldCalculateHighSurge() {
        // Given: 5 km distance with 2.0x surge
        Double distanceKm = 5.0;
        BigDecimal surgeMultiplier = new BigDecimal("2.0");
        
        // When: Calculate fare (50 + (5 * 10)) * 2.0 = 100 * 2.0 = 200
        BigDecimal fare = fareCalculationService.calculateFare(distanceKm, surgeMultiplier);
        
        // Then: fare = 200
        assertThat(fare).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Should use default surge multiplier when not provided")
    void shouldUseDefaultSurgeMultiplier() {
        // Given: 10 km distance, no surge multiplier
        Double distanceKm = 10.0;
        
        // When: Calculate fare without surge
        BigDecimal fare = fareCalculationService.calculateFare(distanceKm, (BigDecimal) null);
        
        // Then: Should use default surge of 1.0, fare = 150
        assertThat(fare).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should throw exception for null distance")
    void shouldThrowExceptionForNullDistance() {
        // Given: null distance
        Double distanceKm = null;
        
        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> fareCalculationService.calculateFare(distanceKm))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Distance must be greater than 0");
    }

    @Test
    @DisplayName("Should throw exception for zero distance")
    void shouldThrowExceptionForZeroDistance() {
        // Given: zero distance
        Double distanceKm = 0.0;
        
        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> fareCalculationService.calculateFare(distanceKm))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Distance must be greater than 0");
    }

    @Test
    @DisplayName("Should throw exception for negative distance")
    void shouldThrowExceptionForNegativeDistance() {
        // Given: negative distance
        Double distanceKm = -5.0;
        
        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> fareCalculationService.calculateFare(distanceKm))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Distance must be greater than 0");
    }

    @Test
    @DisplayName("Should handle very small positive distance")
    void shouldHandleVerySmallDistance() {
        // Given: 0.1 km distance
        Double distanceKm = 0.1;
        
        // When: Calculate fare (50 + (0.1 * 10) = 51, below minimum 100)
        BigDecimal fare = fareCalculationService.calculateFare(distanceKm);
        
        // Then: Should return minimum fare of 100
        assertThat(fare).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should handle large distance correctly")
    void shouldHandleLargeDistance() {
        // Given: 100 km distance
        Double distanceKm = 100.0;
        
        // When: Calculate fare (50 + (100 * 10) = 1050)
        BigDecimal fare = fareCalculationService.calculateFare(distanceKm);
        
        // Then: fare = 1050
        assertThat(fare).isEqualTo(new BigDecimal("1050.00"));
    }

    @Test
    @DisplayName("Should parse surge multiplier from string")
    void shouldParseSurgeMultiplierFromString() {
        // Given: 10 km and surge multiplier as string "1.5"
        Double distanceKm = 10.0;
        String surgeMultiplierStr = "1.5";
        
        // When: Calculate fare
        BigDecimal fare = fareCalculationService.calculateFare(distanceKm, surgeMultiplierStr);
        
        // Then: fare = (50 + 100) * 1.5 = 225
        assertThat(fare).isEqualTo(new BigDecimal("225.00"));
    }

    @Test
    @DisplayName("Should handle invalid surge multiplier string")
    void shouldHandleInvalidSurgeMultiplierString() {
        // Given: 10 km and invalid surge multiplier string
        Double distanceKm = 10.0;
        String surgeMultiplierStr = "invalid";
        
        // When: Calculate fare (should use default surge)
        BigDecimal fare = fareCalculationService.calculateFare(distanceKm, surgeMultiplierStr);
        
        // Then: Should use default surge of 1.0, fare = 150
        assertThat(fare).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should return fare configuration")
    void shouldReturnFareConfiguration() {
        // When: Get fare configuration
        FareProperties config = fareCalculationService.getFareConfiguration();
        
        // Then: Should return the mocked properties
        assertThat(config).isNotNull();
        assertThat(config.getBaseFare()).isEqualTo(new BigDecimal("50.00"));
        assertThat(config.getRatePerKm()).isEqualTo(new BigDecimal("10.00"));
        assertThat(config.getMinimumFare()).isEqualTo(new BigDecimal("100.00"));
    }

}
