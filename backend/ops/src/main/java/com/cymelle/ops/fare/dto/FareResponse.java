package com.cymelle.ops.fare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareResponse {

    private Double distanceKm;

    private BigDecimal baseFare;

    private BigDecimal distanceCharge;

    private String surgeMultiplier;

    private BigDecimal minimumFare;

    private BigDecimal finalFare;

}
