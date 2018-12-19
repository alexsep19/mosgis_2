package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnTable;

public class AccompanyingDocument extends EnTable {
    
    public enum Columns implements ColEnum {

//        DOCUMENTGUID                    (Type.UUID,                  null,  "UUID документа в системе"),
        REGDATE                         (DATE,                              "Дата включения в реестр"),
        LICENSE                         (License.class,              null,  "Лицензия"),
        DOCTYPE                         (STRING,    20,                     "Тип документа (НСИ 75)");
//todo        DECISIONORG                     (LicenseOrganization.class,  null,  "Наименование организации, принявшей решение");

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private Columns (Type type, Object... p) {col = new Col (this, type, p);}
        private Columns (Class c,   Object... p) {col = new Ref (this, c, p);} 
        
    }
        
    public AccompanyingDocument () {
        super ("tb_accompanying_document", "Документы лицензионного дела");
        
        cols   (Columns.class);
        
//        key    ("documentguid", AccompanyingDocument.Columns.DOCUMENTGUID);
        
    }
}
