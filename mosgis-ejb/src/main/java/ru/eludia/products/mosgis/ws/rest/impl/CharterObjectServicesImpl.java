package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.CharterFile;
import ru.eludia.products.mosgis.rest.api.CharterObjectServicesLocal;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectService;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CharterObjectServicesImpl extends BaseCRUD<CharterObjectService> implements CharterObjectServicesLocal  {

    private static final Logger logger = Logger.getLogger (CharterObjectServicesImpl.class.getName ());    

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        final Model m = db.getModel ();

        job.add ("item", db.getJsonObject (m
            .get   (getTable (), id,          "AS root", "*"    )
        ));
        
    });}

    @Override
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {
        
        ComplexSearch s = new ComplexSearch (p.getJsonArray ("search"));
        
        if (!s.getFilters ().containsKey ("uuid_charter_object")) throw new IllegalStateException ("uuid_charter_object filter is not set");
                        
        final NsiTable nsi2 = NsiTable.getNsiTable (2);
        final NsiTable nsi3 = NsiTable.getNsiTable (3);

        Select select = db.getModel ()
            .select     (getTable (),              "AS root", "*", "uuid AS id")
            .toMaybeOne (CharterFile.class,        "AS proto", "label").on ()
            .toMaybeOne (AdditionalService.class,  "AS adds", "label").on ()
            .toMaybeOne (nsi3, nsi3.getLabelField ().getfName () + " AS nsi_label").on ("(root.code_vc_nsi_3=vc_nsi_3.code AND vc_nsi_3.isactual=1)")
            .toMaybeOne (nsi2, "code AS code_vc_nsi_2").on ("(vc_nsi_2.guid=vc_nsi_3.f_1587117ecc)")
            .where      ("is_deleted", 0)
            .orderBy    ("\"nsi_label\"")
            .orderBy    ("adds.label")
            ;
        
        db.addJsonArrays (job, s.filter (select, ""));

    });}

}