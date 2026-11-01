package se.iths.martin.productserviceprojekt2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;
import se.iths.martin.productserviceprojekt2.model.Product;
import java.util.Optional;

/**
 * Hanterar databasåtkomst och låsning av produkter.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /**
     * Låser produkten under transaktionen innan dess lagersaldo ändras.
     *
     * Ingår i betalningsflödet.
     *
     * @param id id för den efterfrågade posten eller reservationen
     * @return den matchande posten, eller ett tomt Optional om den saknas
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findLockedById(@Param("id") Long id);
}
