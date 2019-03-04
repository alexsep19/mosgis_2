package ru.eludia.products.mosgis.db.model.voc.nsi58;

import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;

public class VocNsi58 extends Table{

    public VocNsi58 () {
        super ("vc_nsi_58", "Справочник ГИС ЖКХ номер 58");
        
        pk     ("code",         Type.STRING,  20,                                                 "Код элемента справочника, уникальный в пределах справочника");
        
        col    ("f_7d0f481f17", Type.BOOLEAN,     null,                                           "Применимо к договорам управления");
        col    ("is_mgmt",      Type.BOOLEAN,                new Virt ("''||F_7D0F481F17"),       "Применимо к договорам управления (синоним)");
        
        col    ("guid",         Type.UUID,                                                        "Глобально-уникальный идентификатор элемента справочника");
        
        col    ("f_a175d0edd2", Type.STRING,      null,                                           "Основание заключения договора");
        col    ("label",        Type.STRING,                 new Virt ("''||F_A175D0EDD2"),       "Основание заключения договора (синоним)");
        
        col    ("f_101d7ea249", Type.BOOLEAN,     null,                                           "Применимо к договорам ресурсоснабжения");
        col    ("is_supply",    Type.BOOLEAN,                new Virt ("''||F_101D7EA249"),       "Применимо к договорам ресурсоснабжения (синоним)");
        
        col    ("isactual",     Type.BOOLEAN,     Bool.TRUE,                                      "Признак актуальности элемента справочника");
        
        data (i.class);
    }
    
    public enum i {
        
        MANAGEMENT_CONTRACT   ("3", "53EAF019C855424FB6920BB876A8EDD2",  "Договор управления",                    false, true,  true),
        CHARTER_1             ("4", "921F5C586D6C4F749750DFFCA6329267",  "Устав",                                 false, true,  true),
        CUSTOMER_APPLICATION  ("7", "8D8C5FB3986C4F14B7B06D437A078A3A",  "Заявление потребителя",                 false, true,  true),
        DECISION_OF_MEETING   ("1", "041C2E370EB645EEA9C63785C8C970D6",  "Решение собрания собственников",        true,  true,  true),
        OPEN_COMPETITION      ("2", "CC632E51F803426FBBA7A29B0A3F4768",  "Открытый конкурс",                      true,  true,  true),
        BOARD_DECISION        ("5", "FB8158330E9549629D9A067DE0475059",  "Решение правления",                     true,  false, true),
        DECISION_OF_DEVELOPER ("6", "C88DE6239BF447CF90D311F0FAC22B31",  "Решение органа управления застройщика", true,  false, true),
        PERMISSION_TO_MKD     ("9", "1AE1DADC86A34AD7ACF0A58A91EEC816",  "Разрешение на ввод МКД в эксплуатацию", true,  false, true),
        CHARTER_2             ("10", "61BCBD17417240DFA0A51A39EB72128E", "Устав",                                 true,  false, true),
        NORMATIVE_LEGAL_ACT   ("8", "C280FD9C30204FBAB79A297781084D8B",  "Нормативный правовой акт",              false, true,  true);
        
        String code;
        String guid;
        String f_a175d0edd2;
        boolean f_7d0f481f17;
        boolean f_101d7ea249;
        boolean isactual;
        
        public String getCode () {
            return code;
        }
        
        public String getGuid () {
            return guid;
        }
        
        public String getF_a175d0edd2 () {
            return f_a175d0edd2;
        }
        
        public boolean getF_7d0f481f17 () {
            return f_7d0f481f17;
        }
        
        public boolean getF_101d7ea249 () {
            return f_101d7ea249;
        }
        
        public boolean getIsactual () {
            return isactual;
        }

	public static i forLabel(String label) {
	    for (i i : values()) {
		if (i.f_a175d0edd2.equals(label)) {
		    return i;
		}
	    }
	    return null;
	}

	private i (String code, String guid, String description, boolean isManagement, boolean isResourceSupply, boolean isActual) {
            this.code = code;
            this.guid = guid;
            this.f_a175d0edd2 = description;
            this.f_7d0f481f17 = isManagement;
            this.f_101d7ea249 = isResourceSupply;
            this.isactual = isActual;
        }
    }
}
