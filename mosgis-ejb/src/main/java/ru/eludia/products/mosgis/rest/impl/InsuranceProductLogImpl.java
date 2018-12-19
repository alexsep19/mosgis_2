package ru.eludia.products.mosgis.rest.impl;

import java.io.IOException;
import java.io.OutputStream;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.InsuranceProduct;
import ru.eludia.products.mosgis.db.model.tables.InsuranceProductLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.InsuranceProductLogLocal;
import ru.eludia.products.mosgis.rest.impl.base.Base;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InsuranceProductLogImpl extends Base<InsuranceProductLog> implements InsuranceProductLogLocal {

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {

        Select select = ModelHolder.getModel ()
            .select     (getTable (), "AS root", "*", "uuid AS id")
            .toOne      (VocOrganization.class, "AS org_ins", "label").on ("root.insuranceorg=org_ins.uuid")
            .toMaybeOne (VocUser.class, "label").on ()
            .toMaybeOne (OutSoap.class, "AS soap", "id_status", "is_failed", "ts", "ts_rp", "err_text").on ()
            .where      ("uuid_object", p.getJsonObject ("data").getString ("uuid_object"))
            .orderBy    ("root.ts DESC")
            .limit      (p.getInt ("offset"), p.getInt ("limit"));

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "*")
            .toOne (InsuranceProduct.class, "AS p", "uuid_org").on ()
        ));

    });}
    
    @Override
    public void download (String id, OutputStream out) throws IOException, WebApplicationException {fetchData ((db, job) -> {        
        db.getStream (getTable (), id, "body", out);        
    });}

}