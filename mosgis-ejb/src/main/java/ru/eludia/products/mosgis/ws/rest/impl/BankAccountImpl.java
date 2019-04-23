package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.products.mosgis.db.model.tables.ActualBankAccount;
import ru.eludia.products.mosgis.db.model.tables.BankAccount;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.BankAccountLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.BankAccountLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BankAccountImpl extends BaseCRUD<BankAccount> implements BankAccountLocal {

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        JsonObject data = p.getJsonObject ("data");
        if (data == null) throw new InternalServerErrorException ("JSON data not set");
                        
        final String kUuidOrg = BankAccount.c.UUID_ORG.lc ();                
        
        String uuidOrg = data.getString (kUuidOrg, null);
        if (data == null) throw new InternalServerErrorException ("uuid_org data not set");

        db.addJsonArrayCnt (job,
            ActualBankAccount.select (uuidOrg)
            .limit (p.getInt ("offset"), p.getInt ("limit"))
        );

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (BankAccount.class, id, "AS root", "*")
	    .toMaybeOne(VocOrganization.class, "AS org", "uuid", "label").on("root.uuid_org = org.uuid")
	    .toMaybeOne(VocOrganization.class, "AS cred_org", "uuid", "label").on("root.uuid_cred_org = cred_org.uuid")
	    .toMaybeOne(VocBic.class, "AS bank", "*").on()
//	    .toOne(BankAccountLog.class, "AS log").on()
//	    .toMaybeOne(OutSoap.class, "AS soap", "*").on("log.uuid_out_soap=soap.uuid")
        ));
        
        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}

}