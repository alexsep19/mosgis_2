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
        BIC             (Type.NUMERIC, 9,        "БИК"),
        REGN            (Type.STRING,            "Регистрационный номер (№ банковской лицензии + возможно, постфикс)"),
        NAMEP           (Type.STRING,            "Наименование банка (филиала)"),
        IND             (Type.NUMERIC, 6,        "Индекс отделения почтовой связи"),
        TNP             (Type.STRING,            "Тип населённого пункта"),
        NNP             (Type.STRING,            "Наименование населённого пункта"),
        ADR             (Type.STRING,            "Адрес"),
        DATEIN          (Type.DATE,              "Дата регистрации Банком России"),
        ACCOUNT         (Type.NUMERIC, 20,       "Номер корреспондентскиого счёта"),
        ACCOUNTCBRBIC   (Type.NUMERIC, 9,        "БИК ПБР, обслуживающего счет участника перевода"),
        CODE_VC_NSI_237 (Type.STRING,  20, null, "Код региона РФ (НСИ 237)"),
        IS_DELETED      (Type.BOOLEAN,  FALSE,   "1, если запись не актуальна; иначе 0")
        ;        
                                                                                    @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);}
    
        static c forName (String s) {
            String u = s.toUpperCase ();
            for (c i: values ()) if (u.equals (i.name ())) return i;
            return null;
        }
    
    }

//Адрес - Tnp+Nnp+Adr -

    public VocBic () {
        super (TABLE_NAME, "Справочник БИК / корреспондентских счетов банков РФ");
        cols  (c.class);        
        pk    (c.BIC);
    }
    
    public static class SAXHandler extends DefaultHandler {
        
        List<Map<String, Object>> corrAccounts = new ArrayList<> ();
        
        Map<String, Object> r = null;

        public List<Map<String, Object>> getCorrAccounts () {
            return corrAccounts;
        }
        
        private String toNsi237 (String okato) {
            switch (okato) {
                case "79": return "1"; //	Республика Адыгея
                case "80": return "2"; //	Республика Башкортостан
                case "81": return "3"; //	Республика Бурятия
                case "84": return "4"; //	Республика Алтай
                case "82": return "5"; //	Республика Дагестан
                case "26": return "6"; //	Республика Ингушетия
                case "83": return "7"; //	Кабардино-Балкарская республика
                case "85": return "8"; //	Республика Калмыкия
                case "91": return "9"; //	Карачаево-Черкесская республика
                case "86": return "10"; //	Республика Карелия
                case "87": return "11"; //	Республика Коми
                case "88": return "12"; //	Республика Марий Эл
                case "89": return "13"; //	Республика Мордовия
                case "98": return "14"; //	Республика Саха (Якутия)
                case "90": return "15"; //	Республика Северная Осетия — Алания
                case "92": return "16"; //	Республика Татарстан
                case "93": return "17"; //	Республика Тыва
                case "94": return "18"; //	Удмуртская республика
                case "95": return "19"; //	Республика Хакасия
                case "96": return "20"; //	Чеченская республика
                case "97": return "21"; //	Чувашская республика
                case "01": return "22"; //	Алтайский край
                case "03": return "23"; //	Краснодарский край
                case "04": return "24"; //	Красноярский край
                case "05": return "25"; //	Приморский край
                case "07": return "26"; //	Ставропольский край
                case "08": return "27"; //	Хабаровский край
                case "10": return "28"; //	Амурская область
                case "11": return "29"; //	Архангельская область
                case "12": return "30"; //	Астраханская область
                case "14": return "31"; //	Белгородская область
                case "15": return "32"; //	Брянская область
                case "17": return "33"; //	Владимирская область
                case "18": return "34"; //	Волгоградская область
                case "19": return "35"; //	Вологодская область
                case "20": return "36"; //	Воронежская область
                case "24": return "37"; //	Ивановская область
                case "25": return "38"; //	Иркутская область
                case "27": return "39"; //	Калининградская область
                case "29": return "40"; //	Калужская область
                case "30": return "41"; //	Камчатский край
                case "32": return "42"; //	Кемеровская область
                case "33": return "43"; //	Кировская область
                case "34": return "44"; //	Костромская область
                case "37": return "45"; //	Курганская область
                case "38": return "46"; //	Курская область
                case "41": return "47"; //	Ленинградская область
                case "42": return "48"; //	Липецкая область
                case "44": return "49"; //	Магаданская область
                case "46": return "50"; //	Московская область
                case "47": return "51"; //	Мурманская область
                case "22": return "52"; //	Нижегородская область
                case "49": return "53"; //	Новгородская область
                case "50": return "54"; //	Новосибирская область
                case "52": return "55"; //	Омская область
                case "53": return "56"; //	Оренбургская область
                case "54": return "57"; //	Орловская область
                case "56": return "58"; //	Пензенская область
                case "57": return "59"; //	Пермский край
                case "58": return "60"; //	Псковская область
                case "60": return "61"; //	Ростовская область
                case "61": return "62"; //	Рязанская область
                case "36": return "63"; //	Самарская область
                case "63": return "64"; //	Саратовская область
                case "64": return "65"; //	Сахалинская область
                case "65": return "66"; //	Свердловская область
                case "66": return "67"; //	Смоленская область
                case "68": return "68"; //	Тамбовская область
                case "28": return "69"; //	Тверская область
                case "69": return "70"; //	Томская область
                case "70": return "71"; //	Тульская область
                case "71": return "72"; //	Тюменская область
                case "73": return "73"; //	Ульяновская область
                case "75": return "74"; //	Челябинская область
                case "76": return "75"; //	Забайкальский край
                case "78": return "76"; //	Ярославская область
                case "45": return "77"; //	Москва
                case "40": return "78"; //	Санкт-Петербург
                case "99": return "79"; //	Еврейская автономная область
                case "77": return "87"; //	Чукотский автономный округ
                case "35": return "91"; //	Республика Крым
                case "67": return "92"; //	Севастополь
                case "111": 
                case "118": 
                    return "83"; //	Ненецкий автономный округ
                case "7110": 
                case "718": 
                    return "86"; //	Ханты-Мансийский автономный округ - Югра
                case "7114": 
                case "719": 
                    return "89"; //	Ямало-Ненецкий автономный округ                
                default: return null;
            }
        }

        @Override
        public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {

            switch (qName) {
                
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

        private void copy (Attributes attributes) {
            
            for (int i = 0; i < attributes.getLength (); i ++) {
                
                String name = attributes.getLocalName (i);
                String value = attributes.getValue (i);
                c k;
                
                switch (name) {
                    case "Rgn":
                        k = c.CODE_VC_NSI_237;
                        value = toNsi237 (value);
                        break;
                    case "AccountStatus":
                        k = c.IS_DELETED;
                        value = "ACAC".equals (value) ? "0" : "1";
                        break;
                    default:
                        k = c.forName (name);
                }
                                                
                if (k != null) r.put (k.lc (), value);                
                                
            }
            
        }
        
        private void parseParticipantInfo (Attributes attributes) {
            
            switch (attributes.getValue ("PtType")) {
                case "20":
                case "30":
                    copy (attributes);
                    break;
                default:
                    r = null;
                    return;
            }
            
        }

        private void parseAccounts (Attributes attributes) {
            if (r == null) return;
            if (!"CRSA".equals (attributes.getValue ("RegulationAccountType"))) return;
            copy (attributes);
            corrAccounts.add (r);
            r = null;
        }
        
    }    

}