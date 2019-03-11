package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class VocDifferentiationNsi268 extends Table {

    public VocDifferentiationNsi268 () {

        super  ("vc_diff_nsi_268", "Сведения о виде тарифа критерия дифференциации");

        pkref  (VocDifferentiation.c.DIFFERENTIATIONCODE.lc (), VocDifferentiation.class, "Критерий дифференциации");
        pk     ("code", Type.STRING, 20, "Вид тарифа (НСИ 268)");

    }

}