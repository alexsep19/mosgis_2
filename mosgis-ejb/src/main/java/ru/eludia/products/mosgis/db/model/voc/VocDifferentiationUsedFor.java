package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;

public class VocDifferentiationUsedFor extends Table {

    public VocDifferentiationUsedFor () {

        super  ("vc_diff_tariff",  "Тип тарифа, для которого применяется критерий дифференциации");

        pkref  (VocDifferentiation.c.DIFFERENTIATIONCODE.lc (), VocDifferentiation.class, "Критерий дифференциации");
        pkref  (VocTariffCaseType.c.ID.lc (), VocTariffCaseType.class, "Тип тарифа");

    }

}