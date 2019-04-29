package ru.eludia.products.mosgis.ws.soap.impl.base;

public enum Errors {
    AUT011002 ("Организация не найдена"),
    AUT011003 ("Доступ запрещен для поставщика данных"),
    INT002000 ("Значение в поле отсутствует в реестре"),
    INT002012 ("Нет объектов для экспорта"),
    INT002013 ("Запрос не найден"),
    INT002034 ("Идентификатор объекта ссылается на несуществующий или неактуальный элемент жилищного фонда"),
    INT004025 ("Данные нежилого помещения должны быть в NonResidentialPremiseToCreate"),
    INT004026 ("Данные подъезда должны быть в EntranceToCreate"),
    INT004027 ("Данные жилого помещения должны быть в ResidentialPremiseToCreate"),
    INT004028 ("Данные комнаты должны быть в LivingRoomToCreate"),
    INT004057 ("Помещение с номером <номер помещения> встречается 2 или более раз"),
    INT004059 ("Комната с номером <номер комнаты> в помещении <номер помещения> встречается 2 или более раз"),
    INT004065 ("Комната с номером <номер комнаты> встречается 2 или более раз"),
    INT004093 ("Данные блока должны быть в BlockToCreate"),
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
