package com.facecloud.facecloudserverapp.exception;

public class DataValidationException extends Exception {

    public DataValidationException(Throwable e) {
        super(e);
    }

    public DataValidationException(String mess) {
        super(mess);
    }
}
