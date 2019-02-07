package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.gosuslugi.dom.schema.integration.house_management.AccountType;

public class AccountItem extends EnTable {

    public enum c implements EnColEnum {
        
        UUID_ACCOUNT           (Account.class,  "Лицевой счёт"),
        
	FIASHOUSEGUID          (VocBuilding.class,   null,       "Глобальный уникальный идентификатор дома по ФИАС"),
	UUID_PREMISE           (Premise.class,       null,       "Помещение"),
        SHAREPERCENT           (Type.NUMERIC, 5, 2,  null,       "Доля внесения платы, размер доли в %"),

        ID_LOG                 (AccountItemLog.class,            "Последнее событие редактирования"),
        
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                case UUID_ACCOUNT:
                    return false;
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
    
    static AccountType.Accommodation toAccommodation (Map<String, Object> r) {
        
        final AccountType.Accommodation result = DB.to.javaBean (AccountType.Accommodation.class, r);
        
        if (result.getLivingRoomGUID () != null) {
            result.setFIASHouseGuid (null);
            result.setPremisesGUID (null);
        }
        
        if (result.getPremisesGUID () != null) {
            result.setFIASHouseGuid (null);
            result.setLivingRoomGUID (null);
        }
        
        if (result.getFIASHouseGuid () != null) {
            result.setPremisesGUID (null);
            result.setLivingRoomGUID (null);
        }

        return result;
        
    }

}