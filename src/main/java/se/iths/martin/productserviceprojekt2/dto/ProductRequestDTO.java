package se.iths.martin.productserviceprojekt2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import se.iths.martin.productserviceprojekt2.model.Category;

import java.math.BigDecimal;

/**
 * Innehåller validerade uppgifter för att skapa en produkt.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Image is required")
    private String imageUrl;
}
