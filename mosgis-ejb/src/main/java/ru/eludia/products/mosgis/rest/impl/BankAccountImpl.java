package ru.eludia.products.mosgis.rest.impl;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.BankAccount;
import ru.eludia.products.mosgis.db.model.tables.RcContract;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocRcContractServiceTypes;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.BankAccountLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BankAccountImpl extends BaseCRUD<BankAccount> implements BankAccountLocal {

    private void filterOffDeleted (Select select) {
        select.and ("is_deleted", 0);
    }
/*
    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        filterOffDeleted (select);

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and ("accountnumber LIKE ?%", searchString.toUpperCase ());
        
    }
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            filterOffDeleted (select);
        }

    }
*/    
/*    
    private void checkFilter (JsonObject data, BankAccount.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }
*/
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                
        
        final MosGisModel m = ModelHolder.getModel ();

        Select select = m.select (getTable (), "AS root", "*", "uuid AS id")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.LABEL.lc ()).on ()
            .toMaybeOne (VocBic.class, "AS bank", "*").on ()
            .orderBy ("root.accountnumber")
            .and (EnTable.c.IS_DELETED, 0)
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObject data = p.getJsonObject ("data");
        if (data == null) throw new InternalServerErrorException ("JSON data not set");
                        
        final String kUuidOrg = BankAccount.c.UUID_ORG.lc ();                
        
        String uuidOrg = data.getString (kUuidOrg, null);
        if (data == null) throw new InternalServerErrorException ("uuidOrg data not set");
        
        select
            .andEither (kUuidOrg, uuidOrg)
            .or (kUuidOrg, m
                .select (RcContract.class, RcContract.c.UUID_ORG.lc ())
                .where (EnTable.c.IS_DELETED, 0)
                .and (RcContract.c.UUID_ORG_CUSTOMER, uuidOrg)
                .and (RcContract.c.ID_CTR_STATUS,     VocGisStatus.i.APPROVED.getId ())
                .and (RcContract.c.ID_SERVICE_TYPE,   VocRcContractServiceTypes.i.BILLING.getId ())
            );

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
        ));
        
        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}
/*
    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocBankAccountType.addTo (jb);
        VocAction.addTo (jb);
        
        return jb.build ();
        
    }
*/        
}