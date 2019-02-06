package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;

public class AccountItem extends EnTable {

    public enum c implements EnColEnum {
        
        UUID_ACCOUNT           (Account.class,  "Лицевой счёт"),
        
	FIASHOUSEGUID          (VocBuilding.class,   null,       "Глобальный уникальный идентификатор дома по ФИАС"),
	UUID_PREMISE           (Premise.class,       null,       "Помещение"),
        SHAREPERCENT           (Type.NUMERIC, 5, 2,  null,       "Доля внесения платы, размер доли в %"),

//        ID_LOG                 (AccountLog.class,                    "Последнее событие редактирования"),
        
//        ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
//        ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {
            switch (this) {
//                case ID_LOG:
//                    return false;
                default:
                    return true;
            }
        }        

    }

    public AccountItem () {
        
        super  ("tb_account_items", "Лицевые счета / помещения");
        cols   (c.class);
        key    ("uuid_account", c.UUID_ACCOUNT.lc ());
        
    }

}