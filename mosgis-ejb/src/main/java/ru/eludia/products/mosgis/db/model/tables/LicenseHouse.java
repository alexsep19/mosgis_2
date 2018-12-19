package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import static ru.eludia.base.model.Type.STRING;


public class LicenseHouse extends EnTable {

    public enum Columns implements ColEnum {

        FIASHOUSEGUID  (VocBuilding.class,         "Глобальный уникальный идентификатор дома по ФИАС"),
        HOUSEADRESS    (STRING,       null,   "Адрес дома"),
        UUID_LICENSE   (License.class,     null,   "Лицензия"), 
        UUID_CONTRACT  (Contract.class,    null,   "Информация о договоре управления");

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private Columns (Type type, Object... p) {col = new Col (this, type, p);}
        private Columns (Class c,   Object... p) {col = new Ref (this, c, p);} 
        
    }
        
    public LicenseHouse () {
        super ("tb_licence_houses", "Информация о доме в лицензии");
        
        cols   (Columns.class);
        
        key    ("fiashouseguid", LicenseHouse.Columns.FIASHOUSEGUID);
        key    ("uuid_license",  LicenseHouse.Columns.UUID_LICENSE);
        key    ("uuid_contract", LicenseHouse.Columns.UUID_CONTRACT);

    }
    
}
