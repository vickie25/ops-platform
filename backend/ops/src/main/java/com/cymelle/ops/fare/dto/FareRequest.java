package com.cymelle.ops.fare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRequest {

    @NotNull(message = "Distance cannot be null")
    @Positive(message = "Distance must be greater than 0")
    private Double distanceKm;

    private String surgeMultiplier;

}
