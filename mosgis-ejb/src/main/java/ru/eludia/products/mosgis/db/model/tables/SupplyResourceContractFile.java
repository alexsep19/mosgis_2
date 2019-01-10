package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.voc.VocSupplyResourceContractFileType;

public class SupplyResourceContractFile extends AttachTable {

    public enum c implements EnColEnum {

        UUID_SR_CTR  (SupplyResourceContract.class,         "Ссылка на договор ресурсоснабжения"),
        ID_TYPE      (VocSupplyResourceContractFileType.class, VocSupplyResourceContractFileType.getDefault (), "Тип"),
        ID_LOG       (SupplyResourceContractFileLog.class,  "Последнее событие редактирования")
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

    public SupplyResourceContractFile () {

        super  ("tb_sr_ctr_files", "Файлы, приложенные к договорам ресурсоснабжения");

        cols   (c.class);

        key    ("parent", c.UUID_SR_CTR);
        key    ("attachmentguid", AttachTable.c.ATTACHMENTGUID);

        trigger ("BEFORE UPDATE", "BEGIN " + CHECK_LEN + "END;");

    }
}
