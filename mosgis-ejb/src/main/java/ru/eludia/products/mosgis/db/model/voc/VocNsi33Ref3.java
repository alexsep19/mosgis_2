package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class VocNsi33Ref3 extends Table {

    public VocNsi33Ref3 () {
        
        super ("vc_nsi_33_d966dd6cbc", "Вид объекта коммунальной инфраструктуры: Вид коммунальной услуги");
        
        pk  ("guid_from", Type.UUID,    "Глобально-уникальный идентификатор элемента справочника");
        pk  ("guid_to",   Type.UUID,    "Значение ссылки");
        col ("ord",       Type.INTEGER, "№ п/п");
        
    }
    
}
