package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;

public class InXlHouseInfo extends EnTable {
    
    public enum c implements ColEnum {
        
        UUID_XL                 (InXlFile.class, "Файл импорта"),
        
        ORD                     (Type.NUMERIC, 5, "Номер строки"),
        
        ADDRESS                 (Type.STRING, "Адрес"),
        
        KIND                    (Type.STRING, "Параметр"),
        NUMBER_OF_RESIDENTS     (Type.INTEGER, null, "Количество проживающих"),
        HAS_UNDERGROUND_PARKING (Type.BOOLEAN, null, "Наличие подземного паркинга"),
        
        ERR                             (Type.STRING,  null,  "Ошибка")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
    }
    
}
