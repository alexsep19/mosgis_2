package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;

public class PremiseUsageTarifOktmo extends Table {

    public PremiseUsageTarifOktmo () {

        super ("tb_pu_usage_tf_oktmo", "Территория действия тарифа Размер платы за пользование жилым помещением");

        pkref ("uuid",  PremiseUsageTarif.class, "Тариф Размер платы за пользование жилым помещением");
        pkref ("oktmo", VocOktmo.class, "ОКТМО");
    }
}