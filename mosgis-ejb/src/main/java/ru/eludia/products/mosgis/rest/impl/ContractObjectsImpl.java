package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.rest.api.ContractObjectsLocal;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;

@Stateless
public class ContractObjectsImpl extends BaseCRUD<ContractObject> implements ContractObjectsLocal  {

    private static final Logger logger = Logger.getLogger (ContractObjectsImpl.class.getName ());    

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {
        
        final Model m = db.getModel ();
        
        final JsonObject item = db.getJsonObject (m
            .get   (getTable (), id,          "AS root", "*"    )
            .toOne (Contract.class,           "AS ctr",  "*"    ).on ()
            .toOne (VocOrganization.class,    "AS org",  "label").on ("ctr.uuid_org")
            .toOne (VocBuildingAddress.class, "AS fias", "label").on ("root.fiashouseguid=fias.houseguid")
        );

        job.add ("item", item);
        
        job.add("vc_action", VocAction.getVocJson ());
        
        db.addJsonArrays (job,                
                
            NsiTable.getNsiTable (db, 3).getVocSelect (),                
            
            m.select (VocContractDocType.class, "id", "label").orderBy ("label"),
            
            m.select (VocGisStatus.class,       "id", "label").orderBy ("id"),
            
            m.select (AdditionalService.class, "uuid AS id", "label").orderBy ("label")
                .and ("is_deleted", 0)
                .and ("uuid_org", item.getString ("ctr.uuid_org"))

        );
        
    });}

    @Override
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {
        
        ComplexSearch s = new ComplexSearch (p.getJsonArray ("search"));
        
        if (!s.getFilters ().containsKey ("uuid_contract")) throw new IllegalStateException ("uuid_contract filter is not set");
                
        Select select = db.getModel ()
            .select     (getTable (),              "AS root", "*", "uuid AS id")
            .toOne      (VocBuildingAddress.class, "AS fias",      "label"                                       ).on ("root.fiashouseguid=fias.houseguid")
            .toMaybeOne (ContractFile.class,       "AS agreement", "agreementnumber AS no", "agreementdate AS dt").on ()
            .where      ("is_deleted", 0)
            .orderBy    ("fias.label")
            .limit      (p.getInt ("offset"), p.getInt ("limit"));
        
        db.addJsonArrays (job, s.filter (select, ""));

    });}

}