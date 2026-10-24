package com.carpool.backend.exception;

public class DupPhoneNumberExp extends RuntimeException {
    public DupPhoneNumberExp(String msg) {
        super(msg);
    }
}
