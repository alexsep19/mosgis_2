package ru.eludia.products.mosgis.ws.soap.impl.base;

public enum Errors {
    AUT011002 ("Организация не найдена"),
    AUT011003 ("Доступ запрещен для поставщика данных"),
    INT002012 ("Нет объектов для экспорта"),
    FMT001300 ("Некорректный XML"),
    INT002013 ("Запрос не найден");

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
