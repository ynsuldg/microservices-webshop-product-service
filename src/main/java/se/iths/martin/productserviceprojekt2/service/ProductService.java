package se.iths.martin.productserviceprojekt2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.iths.martin.productserviceprojekt2.dto.ProductInfoDTO;
import se.iths.martin.productserviceprojekt2.dto.ProductRequestDTO;
import se.iths.martin.productserviceprojekt2.dto.ProductResponseDTO;
import se.iths.martin.productserviceprojekt2.dto.ProductStockRequest;
import se.iths.martin.productserviceprojekt2.exception.InsufficientStockException;
import se.iths.martin.productserviceprojekt2.exception.ProductNotFoundException;
import se.iths.martin.productserviceprojekt2.mapper.ProductMapper;
import se.iths.martin.productserviceprojekt2.model.Product;
import se.iths.martin.productserviceprojekt2.repository.ProductRepository;

import java.util.List;
import java.util.Comparator;

/**
 * Affärslogik för hantering av produkter. Ansvarar för CRUD operationer på produkter samt för att minska lagersaldo vid orderläggning.
 */
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    /**
     * Hämtar samtliga produkter.
     *
     * @return lista med alla produkter,tom lista om inga finns
     */
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponseDTO)
                .toList();
    }

    /**
     * Hämtar en specifik produkter via id.
     *
     * @param id produktens id
     * @return produkten
     * @throws ProductNotFoundException om ingen produkt med angivet id existerar
     */
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toResponseDTO(product);
    }

    /**
     * Borttagning av produkt via id.
     *
     * @param id produktens id
     * @throws ProductNotFoundException om ingen produkt med angivet id existerar
     */
    public void deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Skapar en ny produkt.
     *
     * @param request produktdata från klienten
     * @return den skapade produkten
     */
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        Product newProduct = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(newProduct);
        return productMapper.toResponseDTO(savedProduct);
    }

    /**
     * Minskar lagersaldo för en eller flera produkter. Samtliga produkter i förfrågan hämtas och valideras.
     * Om valideringen misslyckas för någon produkt kastas ett exception och ingen produkt i listan uppdateras,
     * tack vare {@link Transactional}.
     *
     * @param stockRequests lista av produkter av datatypen {@link ProductStockRequest}
     * @return info om varje uppdaterad produkt
     * @throws ProductNotFoundException   om någon produkt inte existerar
     * @throws InsufficientStockException om efterfrågad kvantitet överstiger tillgängligt lager för någon produkt
     */
    @Transactional
    public List<ProductInfoDTO> decreaseStock(List<ProductStockRequest> stockRequests) {
        List<Product> products = stockRequests.stream()
                .sorted(Comparator.comparing(ProductStockRequest::getProductId)).map(req -> {
            Product pro = productRepository.findLockedById(req.getProductId()).orElseThrow(() -> new ProductNotFoundException(req.getProductId()));
            if (pro.getStock() < req.getQuantity()) {
                throw new InsufficientStockException(pro.getName(), req.getQuantity(), pro.getStock());
            }
            return pro;
        }).toList();

        List<ProductInfoDTO> updated = stockRequests.stream()
                .map(request -> {
                    Product product = products.stream()
                            .filter(p -> p.getId().equals(request.getProductId()))
                            .findFirst()
                            .get();

                    product.setStock(product.getStock() - request.getQuantity());
                    productRepository.save(product);
                    return ProductInfoDTO.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .price(product.getPrice())
                            .quantity(request.getQuantity())
                            .build();
                })
                .toList();

        return updated;
    }
}
