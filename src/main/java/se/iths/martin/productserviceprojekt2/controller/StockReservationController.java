package se.iths.martin.productserviceprojekt2.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.iths.martin.productserviceprojekt2.dto.ProductInfoDTO;
import se.iths.martin.productserviceprojekt2.dto.ProductStockRequest;
import se.iths.martin.productserviceprojekt2.service.StockReservationService;

import java.util.List;
import java.util.UUID;

/** Interna lageranrop som skyddas av tjänstens separata säkerhetskedja. */
@RestController
@RequestMapping("/internal/stock-reservations")
@RequiredArgsConstructor
public class StockReservationController {
    private final StockReservationService service;

    public record ReservationRequest(@NotBlank String customerName,
                                     @NotEmpty List<@NotNull @Valid ProductStockRequest> items) { }
    public record CustomerRequest(@NotBlank String customerName) { }

    // Reserverar produkter för kunden eller returnerar samma uppgifter vid ett upprepat anrop.
    @PostMapping("/{id}")
    public List<ProductInfoDTO> reserve(@PathVariable UUID id, @Valid @RequestBody ReservationRequest request) {
        return service.reserve(id, request.customerName(), request.items());
    }

    // Återställer reservationen utan att upprepade anrop ökar lagret igen.
    @PostMapping("/{id}/release")
    public ResponseEntity<Void> release(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        service.release(id, request.customerName());
        return ResponseEntity.noContent().build();
    }

    // Markerar reservationen som använd när den betrodda betalningstjänsten bekräftat betalning.
    @PostMapping("/{id}/consume")
    public ResponseEntity<Void> consume(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        service.consume(id, request.customerName());
        return ResponseEntity.noContent().build();
    }

    // Returnerar ett tydligt klientfel för ogiltiga reservationsuppgifter.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> invalidReservation(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    // Returnerar konflikt när reservationen redan avslutats eller används för en annan varukorg.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> conflictingReservation(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }
}
