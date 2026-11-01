package se.iths.martin.productserviceprojekt2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Anger produkt-id och det antal som ska reserveras eller dras från lagret.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
