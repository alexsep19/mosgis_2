package ru.eludia.products.mosgis.db.model.tables;

import static ru.eludia.base.model.def.Bool.FALSE;

import java.util.UUID;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class Sender extends EnTable {
    
    public static final UUID MOS_GIS_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID MOS_GIS_SALT = UUID.fromString("06b0804e-4459-497c-8add-c99fbc79e8b6");
	
	public enum c implements EnColEnum {

        ID_LOG               (SenderLog.class,    "Последнее событие редактирования"),
        LABEL_FULL           (Type.STRING, null,  "Наименование системы"),
        LABEL                (Type.STRING,        "Сокращенное наименование"),
        CONTACT              (Type.STRING, null,  "Ответственный за интеграцию"),
        LOGIN                (Type.STRING, null,  "Логин"),
        IS_LOCKED            (Type.BOOLEAN, FALSE,  "1, если запись заблокирована; иначе 0"),        
        SALT                 (Type.UUID, null,        "Соль для пароля"),
        SHA1                 (Type.BINARY, 20, null,  "SHA1 от пароля с учётом salt"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class<?> c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                    return false;
                default: 
                    return true;
            }
        }

    } 

    public Sender () {        
        super ("tb_senders", "Поставщики данных");                
        cols   (c.class);        
        unique ("login", "login");
        
        //Поставщик информации МосГИС
        item(EnTable.c.UUID, MOS_GIS_UUID, 
        		c.LABEL_FULL, "Региональный сегмент", 
        		c.LABEL, "Регсегмент", 
        		c.SALT, MOS_GIS_SALT);
    }
    
}