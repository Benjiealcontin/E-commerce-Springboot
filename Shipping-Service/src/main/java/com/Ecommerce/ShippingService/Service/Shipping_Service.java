package com.Ecommerce.ShippingService.Service;

import com.Ecommerce.ShippingService.Dto.MessageResponse;
import com.Ecommerce.ShippingService.Entity.ShippingOption;
import com.Ecommerce.ShippingService.Exception.ShippingAlreadyExistsException;
import com.Ecommerce.ShippingService.Exception.ShippingMethodNotFoundException;
import com.Ecommerce.ShippingService.Exception.ShippingOptionNotFoundException;
import com.Ecommerce.ShippingService.Repository.ShippingOptionRepository;
import com.Ecommerce.ShippingService.Request.ProductTotalAmountRequest;
import com.Ecommerce.ShippingService.Request.ShippingOptionRequest;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Shipping_Service {
    private final ShippingOptionRepository shippingOptionRepository;
    private final ModelMapper modelMapper;

    public Shipping_Service(ShippingOptionRepository shippingOptionRepository, ModelMapper modelMapper) {
        this.shippingOptionRepository = shippingOptionRepository;
        this.modelMapper = modelMapper;
    }

    //Add Shipping Option
    public MessageResponse addShippingOption(ShippingOptionRequest shippingOptionRequest) {
        String ShippingName = shippingOptionRequest.getShippingName();

        if (shippingOptionRepository.existsByShippingName(ShippingName)) {
            throw new ShippingAlreadyExistsException("Product with name " + ShippingName + " already exists");
        }

        ShippingOption shippingOption = mapShippingOptionRequestToShippingOption(shippingOptionRequest);
        shippingOptionRepository.save(shippingOption);

        return new MessageResponse("Shipping Option Added Successfully!");
    }

    //Map the ShippingOptionRequest and ShippingOption
    private ShippingOption mapShippingOptionRequestToShippingOption(ShippingOptionRequest shippingOptionRequest) {
        return modelMapper.map(shippingOptionRequest, ShippingOption.class);
    }

    //Get Shipping Option
    public List<ShippingOption> getShippingOption() {
        List<ShippingOption> shippingOptions = shippingOptionRepository.findAll();

        if (shippingOptions.isEmpty()) {
            throw new ShippingOptionNotFoundException("No shipping options found");
        }

        return shippingOptions;
    }

    //Calculate the Shipping and Product
    public double calculateTotalCost(ProductTotalAmountRequest productTotalAmountRequest) {
        String selectedShippingMethod = productTotalAmountRequest.getShoppingMethod();
        ShippingOption shippingCost = shippingOptionRepository.findByShippingName(selectedShippingMethod);

        if (shippingCost != null) {
            return productTotalAmountRequest.getTotalAmount() + shippingCost.getPrice();
        }

        // Throw an exception if the shipping method doesn't exist
        throw new ShippingMethodNotFoundException("Shipping method not found: " + selectedShippingMethod);
    }

    //Delete Shipping Method
    public void deleteShippingMethod(Long id){
        if (!shippingOptionRepository.existsById(id)) {
            throw new ShippingOptionNotFoundException("Shipping Method with ID " + id + " not found.");
        }

        shippingOptionRepository.deleteById(id);
    }

    public void updateShippingOption(long id, ShippingOptionRequest updateRequest) {
        ShippingOption existingShipping = shippingOptionRepository.findById(id)
                .orElseThrow(() -> new ShippingOptionNotFoundException("Product with id " + id + " not found"));

        // Update the fields
        modelMapper.map(updateRequest, existingShipping);

        shippingOptionRepository.save(existingShipping);
    }
}
