package se.iths.martin.productserviceprojekt2.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Sparar en lagerreservation och produktuppgifterna vid reservationstillfället. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class StockReservation {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String customerName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Item> items = new ArrayList<>();

    /**
     * Anger om lagerreservationen är aktiv, frigjord eller förbrukad.
     */
    public enum Status { ACTIVE, RELEASED, CONSUMED }

    /** Produkt-id, antal och pris sparas så att reservationen kan återställas eller läsas igen. */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Item {
        private Long productId;
        private String name;
        private BigDecimal price;
        private int quantity;
    }
}
