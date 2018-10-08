package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;

public class NonResidentialPremise extends Passport {
    
    public NonResidentialPremise () {
        
        super  ("tb_premises_nrs", "Нежилые помещения");
        
        pk     ("uuid",               Type.UUID,   NEW_UUID,           "Ключ");
        col    ("terminationdate",    Type.DATE,           null,       "Дата прекращения существования объекта");
        col    ("is_deleted",         Type.BOOLEAN,        Bool.FALSE, "1, если запись удалена; иначе 0");
        col    ("code_vc_nsi_330",    Type.STRING,  20,    null,       "Причина аннулирования");
        col    ("annulmentinfo",      Type.STRING,         null,       "Причина аннулирования. Дополнительная информация");

        col    ("annulmentreason",    Type.STRING,         new Virt ("''||\"CODE_VC_NSI_330\""),  "Причина аннулирования");
        col    ("is_annuled",         Type.BOOLEAN,        new Virt ("DECODE(\"CODE_VC_NSI_330\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");
        
        ref    ("uuid_house",         House.class,                     "Дом");
        
        col    ("premisesnum",        Type.STRING, 255,    null,       "Номер помещения");
        col    ("cadastralnumber",    Type.STRING,         null,       "Кадастровый номер");
        col    ("totalarea",          Type.NUMERIC, 25, 4, null,       "Общая площадь жилого помещения");
        col    ("iscommonproperty",   Type.BOOLEAN,        Bool.FALSE, "1, если помещение составляет общее имущество в многоквартирном доме; иначе 0");
        col    ("floor",              Type.STRING,         null,       "Этаж");
        
        col    ("gis_unique_number",       Type.STRING,                     "Уникальный номер");
        col    ("gis_modification_date",   Type.TIMESTAMP,      null,       "Дата модификации данных в ГИС ЖКХ");
        col    ("information_confirmed",   Type.BOOLEAN,        Bool.TRUE,  "Информация подтверждена поставщиком");

        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "
            + "IF :NEW.premisesnum IS NULL        THEN raise_application_error (-20000, '#premisesnum#: Необходимо указать номер помещения.'); END IF; "
            + "IF  NVL (:NEW.totalarea, 0) <= 0 AND :OLD.totalarea > 0  THEN raise_application_error (-20000, '#totalarea#: Необходимо указать размер общей плошади.'); END IF; "
        + "END;");
        
    }
        
    @Override
    public void addNsiFields (DB db) throws SQLException {

        boolean isVirgin = refTables.isEmpty ();
        
            db.forEach (model.select (VocPassportFields.class, "*").where ("is_for_premise_nrs", 1).and ("id_type IS NOT NULL"), rs -> {

                if (rs.getInt ("is_multiple") == 1) {
                    
                    if (!isVirgin) return;
                    
                    MultipleRefTable refTable = new MultipleRefTable (this, rs.getString ("id"), remark + ": " + rs.getString ("label"));

                    db.adjustTable (refTable);
                    
                    refTables.add (refTable);
                    
                }
                else {

                    Col col = VocRdColType.i.forId (rs.getInt ("id_type")).getColDef ("f_" + rs.getString ("id"), rs.getString ("label"));

                    if (col == null) return;
                    if (columns.containsKey (col.getName ())) return;

                    add (col);

                }

            });
            
            db.adjustTable (this);
            
    }
    
}