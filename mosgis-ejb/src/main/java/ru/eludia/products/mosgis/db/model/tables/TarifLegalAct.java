package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class TarifLegalAct extends EnTable  {

    public static final String TABLE_NAME = "tb_tf_legal_acts";

    public enum c implements EnColEnum {

	UUID_TF                (Type.UUID, "Тариф"),

	UUID_LEGAL_ACT         (LegalAct.class, "Утверждающий нормативно-правовой акт")
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        @Override
        public boolean isLoggable () {
            return false;
        }

    }

    public TarifLegalAct () {

	super  (TABLE_NAME, "Тарифы: утверждающие нормативно-правовые акты");

	cols   (c.class);

	key    (c.UUID_TF);
    }
}