package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTerritory;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.VocOrganizationTerritoriesLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocOrganizationTerritoriesImpl extends BaseCRUD<VocOrganizationTerritory> implements VocOrganizationTerritoriesLocal {

    @Override
    public JsonObject getItem(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }    
    
    @Override
    public JsonObject select(JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Map<String, Object> data = getData (p);
        
        Select select = ModelHolder.getModel ().select(VocOrganizationTerritory.class, "uuid AS id")
                .toOne (VocOktmo.class, "id AS oktmo_id", "code AS code", "site_name AS label").on ()
                .where (VocOrganizationTerritory.c.UUID_ORG.lc (), data.get("uuid_org").toString ())
                .and   ("is_deleted", "0")
                .orderBy ("code");
        
        db.addJsonArrayCnt(job, select);
        
    });}
}
