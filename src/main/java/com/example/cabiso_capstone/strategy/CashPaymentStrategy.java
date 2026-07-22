package com.example.cabiso_capstone.strategy;

//Concrete strategy for cash payments.Cash payments do not require a transaction reference number.

public class CashPaymentStrategy
        implements PaymentMethodStrategy {

    @Override
    public String getMethodName() {
        return "CASH";
    }

    @Override
    public String validateReferenceNumber(
            String referenceNumber
    ) {

        if (referenceNumber == null || referenceNumber.isBlank()) {

            return null;
        }

        return referenceNumber.trim();
    }
}