package com.example.cabiso_capstone.strategy;

//Strategy interface for payment-method-specific rules.

public interface PaymentMethodStrategy {

    String getMethodName();

    String validateReferenceNumber(
            String referenceNumber
    );
}