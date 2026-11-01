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

    /**
     * Innehåller kunden och produkterna för en lagerreservation.
     */
    public record ReservationRequest(@NotBlank String customerName,
                                     @NotEmpty List<@NotNull @Valid ProductStockRequest> items) { }
    /**
     * Identifierar kunden som äger lagerreservationen.
     */
    public record CustomerRequest(@NotBlank String customerName) { }

    /**
     * Reserverar produkter för kunden eller returnerar samma uppgifter vid ett upprepat anrop.
     *
     * Ingår i betalningsflödet.
     *
     * @param id id för den efterfrågade posten eller reservationen
     * @param request uppgifter för anropet
     * @return reserverade produkter med pris och antal
     */
    @PostMapping("/{id}")
    public List<ProductInfoDTO> reserve(@PathVariable UUID id, @Valid @RequestBody ReservationRequest request) {
        return service.reserve(id, request.customerName(), request.items());
    }

    /**
     * Återställer reservationen utan att upprepade anrop ökar lagret igen.
     *
     * Ingår i betalningsflödet.
     *
     * @param id id för den efterfrågade posten eller reservationen
     * @param request uppgifter för anropet
     * @return HTTP-svar med statuskod och eventuell svarskropp
     */
    @PostMapping("/{id}/release")
    public ResponseEntity<Void> release(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        service.release(id, request.customerName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Markerar reservationen som använd när den betrodda betalningstjänsten bekräftat betalning.
     *
     * Ingår i betalningsflödet.
     *
     * @param id id för den efterfrågade posten eller reservationen
     * @param request uppgifter för anropet
     * @return HTTP-svar med statuskod och eventuell svarskropp
     */
    @PostMapping("/{id}/consume")
    public ResponseEntity<Void> consume(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        service.consume(id, request.customerName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Returnerar ett tydligt klientfel för ogiltiga reservationsuppgifter.
     *
     * Ingår i betalningsflödet.
     *
     * @param exception felet som ska översättas till ett HTTP-svar
     * @return HTTP-svar med statuskod och eventuell svarskropp
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> invalidReservation(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    /**
     * Returnerar konflikt när reservationen redan avslutats eller används för en annan varukorg.
     *
     * Ingår i betalningsflödet.
     *
     * @param exception felet som ska översättas till ett HTTP-svar
     * @return HTTP-svar med statuskod och eventuell svarskropp
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> conflictingReservation(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }
}
