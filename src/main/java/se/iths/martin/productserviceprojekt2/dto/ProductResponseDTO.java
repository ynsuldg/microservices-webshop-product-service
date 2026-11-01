package se.iths.martin.productserviceprojekt2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.iths.martin.productserviceprojekt2.model.Category;

import java.math.BigDecimal;

/**
 * Innehåller produktuppgifterna som returneras av API:et.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private Category category;
    private String imageUrl;
}
