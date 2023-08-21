package com.Ecommerce.ProductService.Service;

import com.Ecommerce.ProductService.Response.ValidationErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidationService {

    public ValidationErrorResponse buildValidationErrorResponse(BindingResult bindingResult) {
        List<ValidationErrorResponse.FieldError> errors = bindingResult.getFieldErrors().stream()
                .map(fieldError -> new ValidationErrorResponse.FieldError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()))
                .collect(Collectors.toList());

        return new ValidationErrorResponse(errors);
    }
}
