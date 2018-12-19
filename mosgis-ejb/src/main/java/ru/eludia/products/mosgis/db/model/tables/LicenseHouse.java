package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;


public class LicenseHouse extends EnTable {

    public enum Columns implements ColEnum {

        FIASHOUSEGUID                       (VocBuilding.class,         "Глобальный уникальный идентификатор дома по ФИАС"),
        HOUSEADRESS                         (VocBuilding.class,         "Адрес дома"),
        LICENSE                             (License.class,     null,   "Лицензия"), 
        CONTRACT                            (Contract.class,    null,   "Информация о договоре управления");

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private Columns (Type type, Object... p) {col = new Col (this, type, p);}
        private Columns (Class c,   Object... p) {col = new Ref (this, c, p);} 
        
    }
        
    public LicenseHouse () {
        super ("tb_licence_houses", "Информация о доме");
        
        cols   (Columns.class);
        
        key    ("licensehouseguid", LicenseHouse.Columns.FIASHOUSEGUID);
        key    ("licensehouseguid", LicenseHouse.Columns.LICENSE);
        key    ("licensehouseguid", LicenseHouse.Columns.CONTRACT);

    }
    
}
