package com.example.cabiso_capstone.strategy;

//Concrete strategy for bank-transfer payments. Bank transfers require a transaction reference.

public class BankTransferPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public String getMethodName() {
        return "BANK TRANSFER";
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
                    "Bank transfers require a reference number."
            );
        }

        if (cleanedReference.length() < 6
                || cleanedReference.length() > 100) {

            throw new IllegalArgumentException(
                    "Enter a valid bank transaction reference."
            );
        }

        return cleanedReference;
    }
}