package se.iths.martin.productserviceprojekt2.exception;

/**
 * Signalerar att lagersaldot inte räcker för det begärda antalet.
 */
public class InsufficientStockException extends RuntimeException {
    /**
     * Skapar InsufficientStockException med de beroenden och inställningar som tjänsten behöver.
     *
     * @param productName produktens namn
     * @param requested begärt antal
     * @param available tillgängligt antal
     */
    public InsufficientStockException(String productName, int requested, int available) {
        super("Insufficient stock for product '" + productName + "': requested " + requested + ", available " + available);
    }
}
