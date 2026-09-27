package com.bank.customer.domain.exception;

public class DuplicateDocumentException extends RuntimeException {
    public DuplicateDocumentException(String documentType, String documentNumber) {
        super("Document already registered: " + documentType + " " + documentNumber);
    }
}
