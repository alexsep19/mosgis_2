package ru.eludia.products.mosgis.db.model.incoming.json;

import ru.eludia.products.mosgis.db.model.incoming.soap.InSoap;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.*;

public class InImportHouses extends Table {

    public InImportHouses () {
        
        super  ("in_js_import_houses",     "Входящие пакетные запросы на регистрацию/изменение МКД/ЖД (переведённые в JSON)");
        
        pk     ("uuid",         Type.UUID, NEW_UUID, "Ключ");
        
        col    ("json",         Type.TEXT,           "Содержимое в формате JSON");
        
        ref    ("uuid_in_soap", InSoap.class,        "Ссылка на SOAP-пакет");

    }
    
}