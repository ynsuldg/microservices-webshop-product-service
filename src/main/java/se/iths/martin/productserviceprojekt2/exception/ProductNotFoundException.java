package se.iths.martin.productserviceprojekt2.exception;

/**
 * Signalerar att den efterfrågade produkten saknas.
 */
public class ProductNotFoundException extends RuntimeException {
    /**
     * Skapar ProductNotFoundException med de beroenden och inställningar som tjänsten behöver.
     *
     * @param id id för den efterfrågade posten eller reservationen
     */
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
