package com.easterfg.takeaway.exception;

/**
 * @author EasterFG on 2022/10/24
 */
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 5008289388903249213L;

    public AccessDeniedException(String message) {
        super(message);
    }
}
