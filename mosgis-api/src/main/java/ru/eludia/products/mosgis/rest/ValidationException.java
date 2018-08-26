package ru.eludia.products.mosgis.rest;

import javax.ws.rs.ServerErrorException;

public class ValidationException extends ServerErrorException {
    
    String fieldName;

    public String getFieldName () {
        return fieldName;
    }

    public ValidationException (String fieldName, String message) {
        super (message, 500);
        this.fieldName = fieldName;
    }
    
}