package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.Charter;
//import ru.eludia.products.mosgis.db.model.tables.CharterFile;
import ru.eludia.products.mosgis.rest.api.CharterObjectsLocal;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectLog;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocCharterObjectReason;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CharterObjectsImpl extends BaseCRUD<CharterObject> implements CharterObjectsLocal  {

    private static final Logger logger = Logger.getLogger (CharterObjectsImpl.class.getName ());    

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        final Model m = db.getModel ();
        
        final JsonObject item = db.getJsonObject (m
            .get   (getTable (), id,          "AS root", "*"    )
            .toOne (Charter.class,            "AS ctr",  "*"    ).on ()
            .toOne (VocOrganization.class,    "AS org",  "label").on ("ctr.uuid_org")
            .toOne (VocBuildingAddress.class, "AS fias", "label").on ("root.fiashouseguid=fias.houseguid")
            .toMaybeOne (House.class,         "AS house", "uuid" ).on ("root.fiashouseguid=house.fiashouseguid")
        );

        job.add ("item", item);
                
        VocAction.addTo (job);
        
        db.addJsonArrays (job,                
                
            NsiTable.getNsiTable (3).getVocSelect (),                
            
            m.select (VocContractDocType.class, "id", "label").orderBy ("label"),
            
            m.select (VocGisStatus.class,       "id", "label").orderBy ("id"),
            
            m.select (VocCharterObjectReason.class, "id", "label").orderBy ("label"),
            
            m.select (AdditionalService.class, "uuid AS id", "label").orderBy ("label")
                .and ("is_deleted", 0)
                .and ("uuid_org", item.getString ("ctr.uuid_org"))

        );

    });}

    @Override
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {
        
        ComplexSearch s = new ComplexSearch (p.getJsonArray ("search"));
        
        if (!s.getFilters ().containsKey ("uuid_charter")) throw new IllegalStateException ("uuid_charter filter is not set");
                
        Select select = db.getModel ()
            .select     (getTable (),              "AS root", "*", "uuid AS id")
            .toOne      (VocBuildingAddress.class, "AS fias",      "label"                                       ).on ("root.fiashouseguid=fias.houseguid")
            .toMaybeOne (House.class,              "AS house",     "uuid"                                        ).on ("root.fiashouseguid=house.fiashouseguid")
//            .toMaybeOne (CharterFile.class,       "AS agreement", "agreementnumber AS no", "agreementdate AS dt").on ()
            .toMaybeOne (CharterObjectLog.class,  "AS log",       "ts"                                          ).on ()
            .where      ("is_deleted", 0)
            .orderBy    ("fias.label")
            .limit      (p.getInt ("offset"), p.getInt ("limit"));
        
        db.addJsonArrays (job, s.filter (select, ""));

    });}
    
    @Override
    public JsonObject doAnnul (String id, JsonObject p, User user) {return doAction ((db) -> {
                
        db.update (getTable (), HASH (
            "uuid",           id,
            "annulmentinfo",  p.getJsonObject ("data").getString ("annulmentinfo")
        ));
        
        logAction (db, user, id, VocAction.i.ANNUL);
                        
    });}

}
