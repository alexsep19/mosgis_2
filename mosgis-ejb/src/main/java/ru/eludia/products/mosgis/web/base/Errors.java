/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.eludia.products.mosgis.web.base;

/**
 *
 * @author Aleksei
 */
public enum Errors {
    INT002012("Нет объектов для экспорта"),
    INT002013("Запрос не найден");

    private Errors(String message) {
        this.message = message;
    }
    
    private String message;
    
    public String getMessage() {
        return message;
    }
    
    public String getMessageWithCode() {
        return this.name() + ": " + this.message;
    }
}