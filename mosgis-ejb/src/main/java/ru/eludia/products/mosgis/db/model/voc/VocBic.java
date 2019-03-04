package ru.eludia.products.mosgis.db.model.voc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Bool.FALSE;

public class VocBic extends Table {
    
    private static final String TABLE_NAME = "vc_bic";    
    
    public enum c implements ColEnum {        
        BIC            (Type.NUMERIC, 9,        "БИК"),
        REGN           (Type.STRING,            "Регистрационный номер (№ банковской лицензии + возможно, постфикс)"),
        NAMEP          (Type.STRING,            "Наименование банка (филиала)"),
        IND            (Type.NUMERIC, 6,        "Индекс отделения почтовой связи"),
        TNP            (Type.STRING,            "Тип населённого пункта"),
        NNP            (Type.STRING,            "Наименование населённого пункта"),
        ADR            (Type.STRING,            "Адрес"),
        DATEIN         (Type.DATE,              "Дата регистрации Банком России"),
        ACCOUNT        (Type.NUMERIC, 20,       "Номер корреспондентскиого счёта"),
        CODE_VC_NSI_95 (Type.STRING,  20, null, "Код региона РФ (НСИ 237)"),
        IS_DELETED     (Type.BOOLEAN,  FALSE,   "1, если запись не актуальна; иначе 0")
        ;        
                                                                                    @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);}
    }

//Адрес - Tnp+Nnp+Adr -

    public VocBic () {
        super (TABLE_NAME, "Справочник БИК / корреспондентских счетов банков РФ");
        cols  (c.class);        
        pk    (c.BIC);
    }
    
    public static class SAXHandler extends DefaultHandler {
        
        List<Map<String, Object>> bics = new ArrayList<> ();
        
        Map<String, Object> r = null;

        @Override
        public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
            
            switch (localName) {
                
                case "BICDirectoryEntry":
                    r = DB.HASH (c.BIC, attributes.getValue ("BIC"));
                    break;
                    
                case "ParticipantInfo":
                    parseParticipantInfo (attributes);
                    break;
                    
                case "Accounts":
                    parseAccounts (attributes);
            }

        }

        private void parseParticipantInfo (Attributes attributes) {
            
            switch (attributes.getValue ("PtType")) {
                case "20":
                case "30":
                    
                    break;
                default:
                    r = null;
            }
            
        }

        private void parseAccounts (Attributes attributes) {
            if (r == null) return;
        }
        
    }    

}