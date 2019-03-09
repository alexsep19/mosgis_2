package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi3;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi33;
import ru.eludia.products.mosgis.db.ModelHolder;

public class VocNsi33Ref3 extends Table {

    public VocNsi33Ref3 () {
        
        super ("vc_nsi_33_d966dd6cbc", "Вид объекта коммунальной инфраструктуры: Вид коммунальной услуги");
        
        pk  ("guid_from", Type.UUID,    "Глобально-уникальный идентификатор элемента справочника");
        pk  ("guid_to",   Type.UUID,    "Значение ссылки");
        col ("ord",       Type.INTEGER, "№ п/п");
        
    }
    
    public static Select getRefs () {
        
        return ModelHolder.getModel ()
                .select     (VocNsi33.class, "AS ref_33_to_3", "code AS code_33")
                .toMaybeOne (VocNsi33Ref3.class).on ("ref_33_to_3.guid = vc_nsi_33_d966dd6cbc.guid_from")
                .toOne      (VocNsi3.class, "code AS code_3").on ("vc_nsi_3.guid = vc_nsi_33_d966dd6cbc.guid_to");
        
    }
    
}
