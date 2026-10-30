package se.iths.martin.productserviceprojekt2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iths.martin.productserviceprojekt2.dto.ProductInfoDTO;
import se.iths.martin.productserviceprojekt2.dto.ProductStockRequest;
import se.iths.martin.productserviceprojekt2.exception.InsufficientStockException;
import se.iths.martin.productserviceprojekt2.exception.ProductNotFoundException;
import se.iths.martin.productserviceprojekt2.model.StockReservation;
import se.iths.martin.productserviceprojekt2.repository.ProductRepository;
import se.iths.martin.productserviceprojekt2.repository.StockReservationRepository;

import java.util.*;

/** Intern grund för betalningsflödets reservationer; ännu inte exponerad som HTTP-endpoints. */
@Service
@RequiredArgsConstructor
@Transactional
public class StockReservationService {
    private final ProductRepository products;
    private final StockReservationRepository reservations;

    // Reserverar lagret en gång per reservations-id och returnerar produktuppgifter från backend.
    public List<ProductInfoDTO> reserve(UUID id, String customerName, List<ProductStockRequest> requests) {
        validate(id, customerName, requests);
        var existing = reservations.findLockedById(id);
        if (existing.isPresent()) {
            return repeatReservation(existing.get(), customerName, requests);
        }

        var ordered = requests.stream().sorted(Comparator.comparing(ProductStockRequest::getProductId)).toList();
        var lockedProducts = ordered.stream().map(request -> products.findLockedById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()))).toList();

        // Kontrollerar igen efter produktlåsen om ett parallellt anrop redan skapade reservationen.
        existing = reservations.findLockedById(id);
        if (existing.isPresent()) {
            return repeatReservation(existing.get(), customerName, requests);
        }

        var reservation = new StockReservation();
        reservation.setId(id);
        reservation.setCustomerName(customerName);
        for (int i = 0; i < ordered.size(); i++) {
            var request = ordered.get(i);
            var product = lockedProducts.get(i);
            if (product.getStock() < request.getQuantity()) {
                throw new InsufficientStockException(product.getName(), request.getQuantity(), product.getStock());
            }
            product.setStock(product.getStock() - request.getQuantity());
            var item = new StockReservation.Item();
            item.setProductId(product.getId());
            item.setName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(request.getQuantity());
            reservation.getItems().add(item);
        }
        reservations.saveAndFlush(reservation);
        return toProducts(reservation);
    }

    // Återställer en aktiv reservation exakt en gång; en betald reservation får inte återställas.
    public void release(UUID id, String customerName) {
        var reservation = ownedReservation(id, customerName);
        if (reservation.getStatus() == StockReservation.Status.RELEASED) {
            return;
        }
        if (reservation.getStatus() == StockReservation.Status.CONSUMED) {
            throw new IllegalStateException("A consumed reservation cannot be released.");
        }
        for (var item : reservation.getItems().stream()
                .sorted(Comparator.comparing(StockReservation.Item::getProductId)).toList()) {
            var product = products.findLockedById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
            product.setStock(Math.addExact(product.getStock(), item.getQuantity()));
        }
        reservation.setStatus(StockReservation.Status.RELEASED);
    }

    // Slutför en reservation efter betalning utan att minska lagret en gång till.
    public void consume(UUID id, String customerName) {
        var reservation = ownedReservation(id, customerName);
        if (reservation.getStatus() == StockReservation.Status.RELEASED) {
            throw new IllegalStateException("A released reservation cannot be consumed.");
        }
        reservation.setStatus(StockReservation.Status.CONSUMED);
    }

    // Hämtar och låser reservationen samt kontrollerar att den tillhör rätt kund.
    private StockReservation ownedReservation(UUID id, String customerName) {
        var reservation = reservations.findLockedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (!Objects.equals(reservation.getCustomerName(), customerName)) {
            throw new AccessDeniedException("Reservation belongs to another customer.");
        }
        return reservation;
    }

    // Tillåter upprepade anrop endast när kund, varukorg och aktiv reservation är oförändrade.
    private List<ProductInfoDTO> repeatReservation(StockReservation reservation, String customerName,
                                                  List<ProductStockRequest> requests) {
        if (!Objects.equals(reservation.getCustomerName(), customerName)) {
            throw new AccessDeniedException("Reservation belongs to another customer.");
        }
        var quantities = new HashMap<Long, Integer>();
        requests.forEach(item -> quantities.put(item.getProductId(), item.getQuantity()));
        if (reservation.getItems().size() != requests.size() || reservation.getItems().stream()
                .anyMatch(item -> !Objects.equals(quantities.get(item.getProductId()), item.getQuantity()))) {
            throw new IllegalStateException("Reservation ID is already used for a different cart.");
        }
        if (reservation.getStatus() != StockReservation.Status.ACTIVE) {
            throw new IllegalStateException("Reservation is no longer active.");
        }
        return toProducts(reservation);
    }

    // Validerar positiva antal och unika produkt-id:n innan något lager ändras.
    private void validate(UUID id, String customerName, List<ProductStockRequest> requests) {
        if (id == null || customerName == null || customerName.isBlank() || requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Reservation ID, customer and items are required.");
        }
        var ids = new HashSet<Long>();
        for (var item : requests) {
            if (item == null || item.getProductId() == null || item.getProductId() <= 0 ||
                    item.getQuantity() <= 0 || !ids.add(item.getProductId())) {
                throw new IllegalArgumentException("Items must have unique positive product IDs and positive quantities.");
            }
        }
    }

    // Returnerar de produktuppgifter och priser som sparades vid reservationen.
    private List<ProductInfoDTO> toProducts(StockReservation reservation) {
        return reservation.getItems().stream().map(item -> new ProductInfoDTO(
                item.getProductId(), item.getName(), item.getPrice(), item.getQuantity())).toList();
    }
}
