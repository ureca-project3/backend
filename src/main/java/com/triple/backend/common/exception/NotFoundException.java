package com.triple.backend.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    static private final String ENTITY_NOT_FOUND = "찾으시는 %s(이)가 없습니다.";

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public static NotFoundException entityNotFound(String entityName) {
        return new NotFoundException(String.format(ENTITY_NOT_FOUND, entityName));
    }
}
