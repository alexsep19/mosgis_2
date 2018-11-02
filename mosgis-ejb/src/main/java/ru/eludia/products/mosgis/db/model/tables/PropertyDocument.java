package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.NUMERIC;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocProtertyDocumentType;

public class PropertyDocument extends EnTable {

    public enum c implements ColEnum {
        
        UUID_ORG                  (VocOrganization.class,         "Поставщик даных"),
        UUID_PREMISE              (Premise.class,                 "Помещение"),
        UUID_ORG_OWNER            (VocOrganization.class,         "Собственник-организация"),
        UUID_PERSON_OWNER         (VocPerson.class,               "Собственник-физлицо"),
        ID_TYPE                   (VocProtertyDocumentType.class, "Тип документа"),
        PRC                       (NUMERIC,       5, 2, null,     "Размер доли в праве собственности на помещение, %"),
        NO                        (STRING,        null,           "Номер документа"),
        DT                        (DATE,          null,           "Дата документа"),
        ISSUER                    (STRING,        null,           "Кем выдан документ"),
        DT_TO                     (DATE,          null,           "Дата прекращения")        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
    }

    public PropertyDocument () {

        super ("tb_prop_docs", "Документы о правах собственности");
        
        cols   (c.class);
        
        key    ("uuid_org", c.UUID_ORG);                

    }
                
}