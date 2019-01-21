package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;

public class VocBuildingAddress extends View {

    public VocBuildingAddress () {
        
        super  ("vc_build_addresses", "Адреса домов/сооружений (слитые)");
        
        pk    ("houseguid",    Type.UUID,                                         "Код здания в ФИАС");        
        
        fk    ("eststatus",    VocBuildingEstate.class,                           "Признак владения");
        col   ("postalcode",   Type.INTEGER, 6, null,                             "Почтовый индекс");
        col   ("livestatus",   Type.BOOLEAN, Bool.TRUE,                           "1 для актуальных записей, 0 для удалённых");

        col   ("label",        Type.STRING, null,                                 "Адрес здания (без индекса)");
        col   ("label_uc",     Type.STRING, null,                                 "АДРЕС ЗДАНИЯ В ВЕРХНЕМ РЕГИСТРЕ (БЕЗ ИНДЕКСА)");

        col   ("is_condo",     Type.BOOLEAN, null,                                "Тип дома (при наличии): 1 для МКД, 0 для ЖД");
        col   ("uuid_house",   Type.UUID,  null,                                  "Ссылка на паспорт дома (при наличии)");
    }

    @Override
    public final String getSQL () {

        return "SELECT" +
            "  b.houseguid" +
            "  , b.livestatus" +
            "  , b.postalcode" +
            "  , b.eststatus" +
            "  , b.label" +
            "  , b.label_uc" +
            "  , h.is_condo" +
            "  , h.uuid uuid_house" +
            " FROM " +
            "  vc_buildings b" +
            " LEFT JOIN tb_houses h ON h.fiashouseguid = b.houseguid";

    }

}