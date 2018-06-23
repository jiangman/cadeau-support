package com.spldeolin.cadeau.support.doc.exception;

import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/23
 */
@Log4j2
public class ParseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
