package com.cymelle.ops.fare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Data
@Configuration
@ConfigurationProperties(prefix = "fare")
public class FareProperties {

    private BigDecimal baseFare;

    private BigDecimal ratePerKm;

    private BigDecimal minimumFare;

    private BigDecimal defaultSurgeMultiplier;

}
