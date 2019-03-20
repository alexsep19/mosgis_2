package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class VocNsi79Ref80 extends Table {
        
    public VocNsi79Ref80 () {
        
        super ("vc_nsi_79_826b2e3255", "Вид документа программы: Вид программы");
        
        pk  ("guid_from", Type.UUID,    "Глобально-уникальный идентификатор элемента справочника");
        pk  ("guid_to",   Type.UUID,    "Значение ссылки");
        col ("ord",       Type.INTEGER, "№ п/п");
        
    }
    
}
