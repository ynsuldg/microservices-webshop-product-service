package se.iths.martin.productserviceprojekt2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.iths.martin.productserviceprojekt2.dto.ProductRequestDTO;
import se.iths.martin.productserviceprojekt2.dto.ProductResponseDTO;
import se.iths.martin.productserviceprojekt2.model.Product;

/**
 * Omvandlar mellan produktentiteten och API:ets DTO:er.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {
    /**
     * från dto till entitet
     *
     * @param requestDTO produktuppgifterna från klienten
     * @return den skapade entiteten
     */
    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequestDTO requestDTO);

    /**
     * från entitet till dto
     *
     * @param entity produkten som ska omvandlas
     * @return DTO:n för API-svaret
     */
    ProductResponseDTO toResponseDTO(Product entity);
}
