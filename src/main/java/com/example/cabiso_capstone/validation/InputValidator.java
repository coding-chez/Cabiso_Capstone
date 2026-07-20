package com.example.cabiso_capstone.validation;

import com.example.cabiso_capstone.exceptions.InvalidContactNumberException;

public final class InputValidator {

    private InputValidator() {
        // Prevent object creation.
    }

    public static String validateContactNumber(
            String contactNumber
    ) throws InvalidContactNumberException {

        String cleanedContact =
                contactNumber == null
                        ? ""
                        : contactNumber.trim();

        if (!cleanedContact.matches(
                "^09\\d{9}$"
        )) {
            throw new InvalidContactNumberException();
        }

        return cleanedContact;
    }
}