package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTerritory;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.VocOrganizationTerritoriesLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocOrganizationTerritoriesImpl extends BaseCRUD<VocOrganizationTerritory> implements VocOrganizationTerritoriesLocal {
    
    @Override
    public JsonObject select(JsonObject p, User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JsonObject getItem(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }    
    
}
