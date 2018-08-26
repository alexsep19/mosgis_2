package ru.eludia.products.mosgis.db.model.nsi;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiNsiRefField;

public class NsiMultipleRefTable extends Table {

    NsiTable srcTable;
    NsiNsiRefField targetField;

    public NsiNsiRefField getTargetField () {
        return targetField;
    }

    public NsiMultipleRefTable (NsiTable srcTable, NsiNsiRefField targetField) {

        super (srcTable.getName () + '_' + targetField.getName (), srcTable.getRemark () + ": " + targetField.getRemark ());

        this.srcTable = srcTable;
        this.targetField = targetField;

        pk  ("guid_from", Type.UUID,    "Глобально-уникальный идентификатор элемента справочника");
        pk  ("guid_to",   Type.UUID,    "Значение ссылки");
        col ("ord",       Type.INTEGER, "№ п/п");

    }

}