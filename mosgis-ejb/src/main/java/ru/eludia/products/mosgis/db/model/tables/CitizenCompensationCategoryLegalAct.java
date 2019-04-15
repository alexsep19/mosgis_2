package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class CitizenCompensationCategoryLegalAct extends Table {

    public CitizenCompensationCategoryLegalAct () {

        super ("tb_cit_comp_cat_legal_acts", "Перечень отдельных категорий граждан, имеющих право на получение компенсации расходов: нормативно-правовые акты");

        pkref ("uuid", CitizenCompensationCategory.class, "Категория граждан");
        pkref ("uuid_legal_act",  LegalAct.class, "Утверждающий нормативно-правовой акт");
    }
}