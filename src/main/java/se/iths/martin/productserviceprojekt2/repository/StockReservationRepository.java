package se.iths.martin.productserviceprojekt2.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.iths.martin.productserviceprojekt2.model.StockReservation;

import java.util.Optional;
import java.util.UUID;

/**
 * Hanterar databasåtkomst och låsning av lagerreservationer.
 */
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
    /**
     * Låser reservationen så att samtidiga återställningar inte behandlar den flera gånger.
     *
     * Ingår i betalningsflödet.
     *
     * @param id id för den efterfrågade posten eller reservationen
     * @return den matchande posten, eller ett tomt Optional om den saknas
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from StockReservation r where r.id = :id")
    Optional<StockReservation> findLockedById(@Param("id") UUID id);
}
