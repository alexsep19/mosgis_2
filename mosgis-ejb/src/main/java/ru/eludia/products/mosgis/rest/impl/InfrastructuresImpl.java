package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.db.model.tables.Infrastructure;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.InfrastructuresLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InfrastructuresImpl extends BaseCRUD<Infrastructure> implements InfrastructuresLocal {

    @Override
    public JsonObject select(JsonObject p, User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JsonObject getItem(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JsonObject getVocs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
