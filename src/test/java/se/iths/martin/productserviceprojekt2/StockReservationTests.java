package se.iths.martin.productserviceprojekt2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import se.iths.martin.productserviceprojekt2.dto.ProductStockRequest;
import se.iths.martin.productserviceprojekt2.exception.InsufficientStockException;
import se.iths.martin.productserviceprojekt2.model.Category;
import se.iths.martin.productserviceprojekt2.model.Product;
import se.iths.martin.productserviceprojekt2.repository.ProductRepository;
import se.iths.martin.productserviceprojekt2.repository.StockReservationRepository;
import se.iths.martin.productserviceprojekt2.service.StockReservationService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockReservationTests {
    @Autowired private StockReservationService service;
    @Autowired private ProductRepository products;
    @Autowired private StockReservationRepository reservations;
    private Product product;

    // Skapar en produkt i H2 och rensar endast testdatabasens tidigare reservationer.
    @BeforeEach
    void setUp() {
        reservations.deleteAll();
        products.deleteAll();
        product = products.save(Product.builder().name("Test product").price(new BigDecimal("12.50"))
                .stock(10).category(Category.ELECTRONICS).imageUrl("test.jpg").build());
    }

    // Kontrollerar att samma reservation varken minskar eller återställer lagret flera gånger.
    @Test
    void retriesDoNotChangeStockTwice() {
        var id = UUID.randomUUID();
        var items = List.of(new ProductStockRequest(product.getId(), 2));
        var first = service.reserve(id, "customer", items);
        service.reserve(id, "customer", items);
        assertEquals(8, stock());
        assertEquals(product.getId(), first.get(0).getId());
        assertEquals(new BigDecimal("12.50"), first.get(0).getPrice());

        service.release(id, "customer");
        service.release(id, "customer");
        assertEquals(10, stock());
        assertThrows(IllegalStateException.class, () -> service.reserve(id, "customer", items));
    }

    // Kontrollerar att betalning inte minskar lagret igen och blockerar senare återställning.
    @Test
    void consumedReservationCannotBeReleased() {
        var id = UUID.randomUUID();
        service.reserve(id, "customer", List.of(new ProductStockRequest(product.getId(), 2)));
        service.consume(id, "customer");
        service.consume(id, "customer");
        assertThrows(IllegalStateException.class, () -> service.release(id, "customer"));
        assertEquals(8, stock());
    }

    // Kontrollerar att ett reservations-id inte kan användas av en annan kund eller för ett ändrat antal.
    @Test
    void rejectsChangedCartAndWrongOwner() {
        var id = UUID.randomUUID();
        service.reserve(id, "customer", List.of(new ProductStockRequest(product.getId(), 2)));
        assertThrows(IllegalStateException.class, () -> service.reserve(id, "customer",
                List.of(new ProductStockRequest(product.getId(), 3))));
        assertThrows(AccessDeniedException.class, () -> service.release(id, "other-customer"));
        assertEquals(8, stock());
    }

    // Kontrollerar att otillräckligt lager inte lämnar någon reservation eller lagerminskning.
    @Test
    void insufficientStockRollsBackReservation() {
        assertThrows(InsufficientStockException.class, () -> service.reserve(UUID.randomUUID(), "customer",
                List.of(new ProductStockRequest(product.getId(), 11))));
        assertEquals(10, stock());
        assertEquals(0, reservations.count());
    }

    // Kontrollerar att två samtidiga återställningar tillsammans bara ökar lagret en gång.
    @Test
    void concurrentReleaseRestoresStockOnce() throws Exception {
        var id = UUID.randomUUID();
        service.reserve(id, "customer", List.of(new ProductStockRequest(product.getId(), 2)));
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> service.release(id, "customer"));
            var second = executor.submit(() -> service.release(id, "customer"));
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
            assertEquals(10, stock());
        } finally {
            executor.shutdownNow();
        }
    }

    // Kontrollerar att ett avbrott som kommer före reservationen blockerar ett försenat reservationsanrop.
    @Test
    void releaseBeforeReservePreventsLateStockDeduction() {
        var id = UUID.randomUUID();
        service.release(id, "customer");
        assertThrows(IllegalStateException.class, () -> service.reserve(id, "customer",
                List.of(new ProductStockRequest(product.getId(), 2))));
        assertEquals(10, stock());
    }

    // Hämtar det aktuella lagersaldot från testdatabasen.
    private int stock() {
        return products.findById(product.getId()).orElseThrow().getStock();
    }
}
