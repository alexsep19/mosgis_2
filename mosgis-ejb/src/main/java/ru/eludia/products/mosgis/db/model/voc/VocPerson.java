package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;

public class VocPerson extends EnTable {

    public enum c implements EnColEnum {
        UUID_XL        (InXlFile.class, null, "Источник импорта"),

        UUID_ORG       (VocOrganization.class,          "Поставщик информации"),
        
        IS_FEMALE      (Type.BOOLEAN,       null,       "Пол"),
        BIRTHDATE      (Type.DATE,          null,       "Дата рождения"),
        
        SNILS          (Type.NUMERIC, 11,   null,       "СНИЛС"),
        
        CODE_VC_NSI_95 (Type.STRING,  20,   null,      "Код документа, удостоверяющего личность (НСИ 95)"),
        SERIES         (Type.STRING,  45,   null,      "Серия документа, удостоверяющего дичность"),
        NUMBER_        (Type.STRING,  45,   null,      "Номер документа, удостоверяющего личность"),
        ISSUEDATE      (Type.DATE,          null,      "Дата выдачи документа, удостоверяющего личность"),
        ISSUER         (Type.STRING,        null,      "Кем выдан документ, удостоверяющий личность"),
        
        PLACEBIRTH     (Type.STRING,  255,  null,      "Место рождения"),
        
        SURNAME        (Type.STRING,  256,             "Фамилия"),
        FIRSTNAME      (Type.STRING,  256,             "Имя"),
        PATRONYMIC     (Type.STRING,  256,  null,      "Отчество"),
        
        LABEL          (Type.STRING,     new Virt("DECODE(\"PATRONYMIC\", NULL, (\"SURNAME\" || ' ' || \"FIRSTNAME\"), (\"SURNAME\" || ' ' || \"FIRSTNAME\" || ' ' || \"PATRONYMIC\"))"), "ФИО"),
        LABEL_UC       (Type.STRING,     new Virt("UPPER(\"LABEL\")"), "ФИО (в верхнем регистре)"),
        
        SEX            (Type.STRING,  1, new Virt("DECODE(IS_FEMALE, 1, 'F', 0, 'M', NULL)"), "Пол (ГИС)"),
        
        ID_LOG         (VocPersonLog.class, null,      "Последнее событие редактирования"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

	@Override
	public boolean isLoggable() {
	    switch (this) {
	    case ID_LOG:
	    case UUID_ORG:
	    case LABEL:
	    case LABEL_UC:
		return false;
	    default:
		return true;
	    }
	}

	public boolean isToXlImport() {

	    switch (this) {
	    case UUID_XL:
	    case SURNAME:
	    case FIRSTNAME:
	    case PATRONYMIC:
	    case IS_FEMALE:
	    case BIRTHDATE:
	    case SNILS:
	    case CODE_VC_NSI_95:
	    case SERIES:
	    case NUMBER_:
	    case ISSUEDATE:
		return true;
	    default:
		return false;
	    }

	}
    }

    public VocPerson () {
        
        super ("vc_persons", "Физические лица");
        
        cols(c.class);

        key ("uuid_org", "uuid_org");
    }
    
}
