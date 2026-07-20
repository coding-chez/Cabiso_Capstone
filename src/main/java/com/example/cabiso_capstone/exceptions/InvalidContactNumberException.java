package com.example.cabiso_capstone.exceptions;

public class InvalidContactNumberException
        extends ValidationException {

    public InvalidContactNumberException() {
        super(
                "Contact number must contain exactly 11 digits "
                        + "and must start with 09."
        );
    }
}