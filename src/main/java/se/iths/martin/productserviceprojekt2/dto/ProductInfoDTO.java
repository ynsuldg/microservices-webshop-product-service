package se.iths.martin.productserviceprojekt2.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Innehåller produktnamn, pris och antal för en order eller lagerreservation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInfoDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private int quantity;
}