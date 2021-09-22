package app.flowkind.microservices.core.product.services;

import app.flowkind.microservices.api.core.product.Product;
import app.flowkind.microservices.core.product.persistence.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "serviceAddress", ignore = true)
    Product productEntityToProductApi(ProductEntity productEntity) ;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    ProductEntity productApiToProductEntity(Product product);
}
