package com.example.cabiso_capstone.strategy;

// Context class in the Strategy Pattern.It delegates payment-method validation to the currently selected strategy.

public class PaymentMethodContext {

    private PaymentMethodStrategy strategy;

    public void setStrategy(
            PaymentMethodStrategy strategy
    ) {

        if (strategy == null) {

            throw new IllegalArgumentException(
                    "A payment method must be selected."
            );
        }

        this.strategy = strategy;
    }

    public String validateReferenceNumber(
            String referenceNumber
    ) {

        if (strategy == null) {

            throw new IllegalStateException(
                    "Payment strategy has not been configured."
            );
        }

        return strategy.validateReferenceNumber(
                referenceNumber
        );
    }

    public String getMethodName() {

        if (strategy == null) {

            throw new IllegalStateException(
                    "Payment strategy has not been configured."
            );
        }

        return strategy.getMethodName();
    }
}