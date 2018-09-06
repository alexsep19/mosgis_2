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
        
    }

    @Override
    public final String getSQL () {
        
        String l = "s.formalname" +
            "    || ' ' || s.shortname" +
            "    ||DECODE(b.house_label,NULL,NULL,', ' || b.house_label)" +
            "    ||DECODE(b.build_label,NULL,NULL,', ' || b.build_label)" +
            "    ||DECODE(ss.shortname,NULL,NULL,', ' || ss.shortname || '. ' || b.strucnum)";
        

        return "SELECT" +
            "  b.houseguid" +
            "  , b.livestatus" +
            "  , b.postalcode" +
            "  , b.eststatus" +
            "  , s.formalname" +
            "  , " + l + " label " +
            "  , UPPER(" + l + ") label_uc " +
            " FROM " +
            "  vc_buildings b" +
            "  INNER JOIN vc_streets s ON b.aoguid = s.aoguid" +
            "  LEFT  JOIN vc_fias_strstat ss ON b.strstatus = ss.strstatid";

    }

}