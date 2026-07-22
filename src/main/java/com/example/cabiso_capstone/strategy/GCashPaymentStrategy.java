package com.example.cabiso_capstone.strategy;

//Concrete strategy for GCash payments.GCash transactions require a reference number.

public class GCashPaymentStrategy
        implements PaymentMethodStrategy {

    @Override
    public String getMethodName() {
        return "GCASH";
    }

    @Override
    public String validateReferenceNumber(
            String referenceNumber
    ) {

        String cleanedReference =
                referenceNumber == null
                        ? ""
                        : referenceNumber.trim();

        if (cleanedReference.isEmpty()) {

            throw new IllegalArgumentException(
                    "GCash payments require a reference number."
            );
        }

        if (cleanedReference.length() < 6
                || cleanedReference.length() > 100) {

            throw new IllegalArgumentException(
                    "Enter a valid GCash reference number."
            );
        }

        return cleanedReference;
    }
}