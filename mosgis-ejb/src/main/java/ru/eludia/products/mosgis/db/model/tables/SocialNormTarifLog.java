package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiation;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationOperator;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationValueKindType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.tariff.ImportResidentialPremisesUsageRequest;
import ru.gosuslugi.dom.schema.integration.tariff.ResidentialPremisesUsageType;
import ru.gosuslugi.dom.schema.integration.tariff.CoefficientType;
import ru.gosuslugi.dom.schema.integration.tariff.DifferentiationType;
import ru.gosuslugi.dom.schema.integration.tariff.ImportResidentialPremisesUsageRequest.ImportResidentialPremisesUsage.CancelResidentialPremisesUsage;
import ru.gosuslugi.dom.schema.integration.tariff.ValueEnumerationType;
import ru.gosuslugi.dom.schema.integration.tariff.ValueMultilineType;
import ru.gosuslugi.dom.schema.integration.tariff.ValueOKTMOType;

public class SocialNormTarifLog extends GisWsLogTable {

    public SocialNormTarifLog () {

        super ("tb_sn_tfs__log", "История редактирования тарифов Социальная норма потребления электрической энергии", SocialNormTarif.class
            , EnTable.c.class
            , SocialNormTarif.c.class
        );
    }
}