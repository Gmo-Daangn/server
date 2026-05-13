package com.ktcloud.daangn.common.exception;

import lombok.Getter;

@Getter
public class InvalidInputException extends RuntimeException {

    private final int statusCode;
    private final String message;

    public InvalidInputException(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

}
