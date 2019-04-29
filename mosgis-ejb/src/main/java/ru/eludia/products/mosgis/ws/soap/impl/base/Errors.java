package ru.eludia.products.mosgis.ws.soap.impl.base;

public enum Errors {
    AUT011002 ("Организация не найдена"),
    AUT011003 ("Доступ запрещен для поставщика данных"),
    INT002000 ("Значение в поле отсутствует в реестре"),
    INT002012 ("Нет объектов для экспорта"),
    INT002013 ("Запрос не найден"),
    INT004132 ("Объект уже размещен в МосГИС"),
    FMT001300 ("Некорректный XML"),
    EXP001000 ("Внутренняя ошибка");

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
