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

//        DOCUMENTGUID                    (Type.UUID,                  null,  "UUID документа в системе"),
//        REGDATE                         (DATE,                              "Дата включения в реестр"),
        LICENSE                         (License.class,              null,  "Лицензия"),
        
        //Document(Реквизиты размещаемого документа)
        DOCTYPE                         (STRING,    20,                     "Тип документа (НСИ 75)"),
        REG_DATE                        (DATE,                              "Дата включения в реестр"),
        DECISIONORG                     (VocOrganization.class,     null,   "Наименование организации, принявшей решение"),
        //todo - 2000?
        NAME                            (STRING,    2000,                   "Наименование документа"),
        NUMBER                          (STRING,    2000,                   "Номер документа"),
        DOCUMENT_STATUS                 (VocDocumentStatus.class,     null,   "Статус документа"),

        //BaseDocument(Реквизиты документа, являющегося основанием для размещения информации)
        BASE_DOCTYPE                    (STRING,    20,                     "Тип документа (НСИ 75)"),
        BASE_DECISIONORG                (VocOrganization.class,     null,   "Наименование организации, принявшей решение"),
        //todo - 2000?
        BASE_DOC_NAME                   (STRING,    2000,                   "Наименование документа"),
        BASE_DOC_NUMBER                 (STRING,    2000,                   "Номер документа"),
        BASE_DOC_DATE                   (DATE,                              "Дата документа"),
        DATE_FROM                       (DATE,                              "Дата вступления документа в силу"),
        ADDITIONAL_INFO                 (STRING,    2000,                   "Дополнительная информация"),
        ;


        @Override
        public Col getCol () {return col;}
        private Col col;        
        private Columns (Type type, Object... p) {col = new Col (this, type, p);}
        private Columns (Class c,   Object... p) {col = new Ref (this, c, p);} 
        
    }
        
    public LicenseAccompanyingDocument () {
        super ("tb_lic_accompanying_document", "Документы лицензионного дела");
        
        cols   (Columns.class);
        
//        key    ("documentguid", AccompanyingDocument.Columns.DOCUMENTGUID);
        
    }
}
