package ru.eludia.products.mosgis.db.model.tables.dyn;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class MultipleRefTable extends Table {

    String targetField;    

    public final String getTargetField () {
        return targetField;
    }
    
    public final String getFieldName () {
        return "f_" + targetField;
    }
    
    public final String getParentRefName () {
        return getPk ().get (0).getName ();
    }

    public MultipleRefTable (Table parent, String targetField, String remark) {

        super (parent.getName () + "_f_" + targetField, remark);

        this.targetField = targetField;

        pkref  ("uuid", parent.getClass (),  "Ссылка на " + parent.getRemark ());
        pk     ("code", Type.STRING, 20, "Код элемента справочника, уникальный в пределах справочника");
        
    }

    @Override
    public String toString () {
        return "[" + name + ":" + getPk ().get (0) + " - " + getPk ().get (1) + "]";
    }

}