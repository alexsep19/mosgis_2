package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.NUMERIC;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ActualCaChObject extends View {

    public static final String TABLE_NAME = "vw_ca_ch_objects";

    public enum c implements ColEnum {
        
        UUID                      (Type.UUID,     null,           "Ключ"),        
        UUID_CONTRACT             (Contract.class,                "Ссылка на договор (NULL для объекта устава)"),
        UUID_CHARTER              (Charter.class,                 "Ссылка на устав (NULL для объекта договора)"),
        FIASHOUSEGUID             (House.class,                   "Глобальный уникальный идентификатор дома по ФИАС"),
        UUID_ORG                  (VocOrganization.class,         "Исполнитель/Организация"),
        ID_CTR_STATUS_GIS         (VocGisStatus.class,            "Статус объекта с точки зрения ГИС ЖКХ"),
        IS_OWN                    (BOOLEAN,                       "1, если объект даёт право редактирования паспорта; иначе 0"),
        DDT_M_START               (NUMERIC,  2,   null,  "Начало периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)"),
        DDT_M_START_NXT           (BOOLEAN,      Bool.FALSE,  "1, если начало периода ввода показаний ПУ в следующем месяце; иначе 0"),
        DDT_M_END                 (NUMERIC,  2,   null,  "Окончание периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)"),
        DDT_M_END_NXT             (BOOLEAN,      Bool.FALSE,  "1, если окончание периода ввода показаний ПУ в следующем месяце; иначе 0"),
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }    
    

    public ActualCaChObject () {        
        super  (TABLE_NAME, "Объекты договоров управления и уставов, определяющие права доступа в данный момент");
        cols   (c.class);
        pk     (c.UUID);        
    }

    @Override
    public final String getSQL () {
        
        QP qp = new QP ("SELECT ");
        for (c c: c.values ()) {
            qp.append (c.lc ());
            qp.append (',');
        }
        qp.setLastChar (' ');
        qp.append ("FROM ");
        
        String s = qp.toString ();
        
        return s + getName (ActualContractObject.class) + " UNION " + s + getName (ActualCharterObject.class);

    }
    
}