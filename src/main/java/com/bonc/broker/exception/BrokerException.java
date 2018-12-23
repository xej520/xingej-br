package com.bonc.broker.exception;

import org.springframework.http.HttpStatus;

/**
 * @author xingej
 */
public class BrokerException extends Exception {
    private HttpStatus code;

    public BrokerException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }

    public BrokerException(String message) {
        super(message);
    }

    public HttpStatus getCode() {
        return code;
    }

    public void setCode(HttpStatus code) {
        this.code = code;
    }

}
