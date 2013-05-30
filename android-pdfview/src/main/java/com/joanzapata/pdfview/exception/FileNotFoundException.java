package com.joanzapata.pdfview.exception;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
