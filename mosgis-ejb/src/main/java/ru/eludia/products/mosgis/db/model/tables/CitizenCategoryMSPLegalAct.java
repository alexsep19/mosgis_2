package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class CitizenCategoryMSPLegalAct extends Table {

    public CitizenCategoryMSPLegalAct () {

        super ("tb_cit_cat_msp_legal_acts", "Перечень отдельных категорий граждан, имеющих право на получение компенсации расходов: нормативно-правовые акты");

        pkref ("uuid", CitizenCategoryMSP.class, "Категория граждан");
        pkref ("uuid_legal_act",  LegalAct.class, "Утверждающий нормативно-правовой акт");
    }
}