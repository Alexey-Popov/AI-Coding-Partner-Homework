package com.supportiq.ticket.exception;

public class UnsupportedImportFormatException extends RuntimeException {

    public UnsupportedImportFormatException(String format) {
        super("Unsupported import format: " + format);
    }
}
