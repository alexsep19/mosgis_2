package ru.eludia.products.mosgis.db.model.nsi;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiScalarField;

public class NsiMultipleScalarTable extends Table {

    NsiTable srcTable;
    NsiScalarField valueField;

    public NsiMultipleScalarTable () {
        super (null);
    }

    public NsiScalarField getValueField () {
        return valueField;
    }

    public NsiMultipleScalarTable (NsiTable srcTable, NsiScalarField valueField) {

        super (srcTable.getName () + '_' + valueField.getName (), srcTable.getRemark () + ": " + valueField.getRemark ());

        this.srcTable = srcTable;
        this.valueField = valueField;

        pk ("guid", Type.UUID,    "Глобально-уникальный идентификатор элемента справочника");
        pk ("ord",  Type.INTEGER, "№ п/п");
        
        add (valueField.getCols ().get (0));

    }

}