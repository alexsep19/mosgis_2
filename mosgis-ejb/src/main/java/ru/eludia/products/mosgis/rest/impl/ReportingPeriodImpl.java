package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.ReportingPeriod;
import ru.eludia.products.mosgis.db.model.tables.WorkingList;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlan;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.api.ReportingPeriodLocal;
import ru.eludia.products.mosgis.rest.impl.base.Base;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ReportingPeriodImpl extends Base<ReportingPeriod> implements ReportingPeriodLocal {

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final String fhg = WorkingList.c.FIASHOUSEGUID.lc ();

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (ReportingPeriod.class, id, "*")
            .toOne (WorkingPlan.class, "AS plan", "*").on ()
            .toOne (WorkingList.class
                , fhg + " AS " + fhg
                , WorkingList.c.DT_FROM.lc ()
                , WorkingList.c.DT_TO.lc ()
                , WorkingList.c.ID_CTR_STATUS.lc ()
            ).on ()
            .toMaybeOne (VocBuilding.class, "AS fias", "label").on ()
            .toMaybeOne (ContractObject.class, "AS cao", "uuid", "startdate", "enddate").on ()
            .toMaybeOne (Contract.class, "AS ca", "*").on ()
            .toMaybeOne (CharterObject.class, "AS cho", "uuid", "startdate", "enddate").on ()
            .toMaybeOne (Charter.class, "AS ch", "*").on ()
            .toMaybeOne (VocOrganization.class, "AS chorg", "label").on ("ch.uuid_org=chorg.uuid")                
                
        ));

    });}
    
}