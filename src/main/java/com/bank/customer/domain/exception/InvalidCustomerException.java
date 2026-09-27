package com.bank.customer.domain.exception;

public class InvalidCustomerException extends RuntimeException {

    private final String errorCode;

    public InvalidCustomerException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
