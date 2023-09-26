package com.Ecommerce.ShippingService.Repository;

import com.Ecommerce.ShippingService.Entity.ShippingOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingOptionRepository extends JpaRepository<ShippingOption, Long> {
    boolean existsByShippingName(String shippingName);

    ShippingOption findByShippingName(String shippingName);

}
