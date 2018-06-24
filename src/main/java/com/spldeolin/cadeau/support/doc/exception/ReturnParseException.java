package com.spldeolin.cadeau.support.doc.exception;

import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/23
 */
@Log4j2
public class ReturnParseException extends ParseException {

    private static final long serialVersionUID = 1L;

    public ReturnParseException(String message) {
        super(message);
    }

    public ReturnParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
