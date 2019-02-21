package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceFileType;

public class MeteringDeviceFile extends AttachTable {
    
    public enum c implements EnColEnum {

        UUID_METER (MeteringDevice.class,            "Ссылка на прибор учёта"),
        ID_TYPE    (VocMeteringDeviceFileType.class, "Тип документа"),        
        ID_LOG     (MeteringDeviceFileLog.class,     "Последнее событие редактирования")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            return false;
        }

    }    

    public MeteringDeviceFile () {
        
        super  ("tb_meter_files", "Файлы, относящиеся к приборам учёта");
        
        cols   (c.class);
        
        key    ("parent", c.UUID_METER);        
        key    ("attachmentguid", AttachTable.c.ATTACHMENTGUID);
       
        trigger ("BEFORE UPDATE", "BEGIN "                
            + CHECK_LEN
        + "END;");        

    }
    
}