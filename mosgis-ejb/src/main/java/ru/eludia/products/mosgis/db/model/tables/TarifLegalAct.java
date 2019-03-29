package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class TarifLegalAct extends Table {

    public TarifLegalAct () {

        super ("tb_tf_legal_acts", "Тарифы: утверждающие нормативно-правовые акты");

        pk    ("uuid", Type.UUID, "Тариф");
        pkref ("uuid_legal_act",  LegalAct.class, "Утверждающий нормативно-правовой акт");
    }
}