package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.InfrastructureResource;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.InfrastructureResourcesLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InfrastructureResourcesImpl extends BaseCRUD<InfrastructureResource> implements InfrastructureResourcesLocal {
    
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
    public JsonObject getItem(String id, User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
