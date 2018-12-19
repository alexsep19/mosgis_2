package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocDocumentStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class LicenseAccompanyingDocument extends EnTable {
    
    public enum Columns implements ColEnum {

        UUID_LICENSE              (License.class,                 "Лицензия"),
        
        //Document(Реквизиты размещаемого документа)
        DOC_TYPE                  (STRING,                  20,   "Тип документа (НСИ 75)"),
        REG_DATE                  (DATE,                    null, "Дата включения в реестр"),
        UUID_ORG_DECISION         (VocOrganization.class,   null, "Наименование организации, принявшей решение"),
        NAME                      (STRING,                        "Наименование документа"),
        NUM                       (STRING,                  null, "Номер документа"),
        ID_STATUS                 (VocDocumentStatus.class,       "Статус документа"),

        //BaseDocument(Реквизиты документа, являющегося основанием для размещения информации)
        BASE_DOC_TYPE             (STRING, 20,   null, "Тип документа (НСИ 75)"),
        BASE_DOC_DECISIONORG      (STRING,       null, "Наименование организации, принявшей решение"),
        BASE_DOC_NAME             (STRING,       null, "Наименование документа"),
        BASE_DOC_NUMBER           (STRING,       null, "Номер документа"),
        BASE_DOC_DATE             (DATE,         null, "Дата документа"),
        BASE_DOC_DATE_FROM        (DATE,         null, "Дата вступления документа в силу"),
        BASE_DOC_ADDITIONAL_INFO  (STRING, 2000, null, "Дополнительная информация");


        @Override
        public Col getCol () {return col;}
        private Col col;        
        private Columns (Type type, Object... p) {col = new Col (this, type, p);}
        private Columns (Class c,   Object... p) {col = new Ref (this, c, p);} 
        
    }
        
    public LicenseAccompanyingDocument () {
        super ("tb_lic_accompanying_document", "Документы лицензионного дела");
        
        cols   (Columns.class);
    }
}
