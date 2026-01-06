package com.ecommerce.mapper;

import com.ecommerce.model.dto.ProductListResponse;
import com.ecommerce.model.dto.ProductRequest;
import com.ecommerce.model.dto.ProductResponse;
import com.ecommerce.model.entity.Product;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ProductMapper {

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductRequest request);

    List<ProductResponse> toResponseList(List<Product> products);

    default ProductListResponse toProductListResponseList(Page<Product> productPage) {
        if (productPage == null) {
            return null;
        } else {
            return new ProductListResponse(
                    toResponseList(productPage.getContent()),
                    productPage.getTotalElements(),
                    productPage.getTotalPages(),
                    productPage.getNumber()
            );
        }
    }

    @Mapping(target = "category.id", source = "categoryId")
    void updateEntity(@MappingTarget Product product, ProductRequest request);
}
