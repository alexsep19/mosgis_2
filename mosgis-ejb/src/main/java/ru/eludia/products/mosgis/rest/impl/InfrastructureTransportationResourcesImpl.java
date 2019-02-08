package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.InfrastructureTransportationResource;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.InfrastructureTransportationResourcesLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InfrastructureTransportationResourcesImpl extends BaseCRUD<InfrastructureTransportationResource> implements InfrastructureTransportationResourcesLocal {
    
    @Override
    public JsonObject select(JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select(getTable (), "AS root", "*", "uuid AS id")
                .orderBy ("root.code_vc_nsi_2")
                .where   ("uuid_oki", p.getJsonObject ("data").getString("uuid_oki"))
                .and     ("is_deleted", 0)
                .limit   (p.getInt ("offset"), p.getInt ("limit"));

        db.addJsonArrayCnt (job, select);
        
    });}

    @Override
    public JsonObject getItem(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
