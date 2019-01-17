package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Infrastructure;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.InfrastructuresLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InfrastructuresImpl extends BaseCRUD<Infrastructure> implements InfrastructuresLocal {

    private final String LABEL_FIELD_NAME_NSI_33 = "f_c8e745bc63";
    
    @Override
    public JsonObject select(JsonObject p, User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JsonObject getItem(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAction.addTo (jb);
        
        final MosGisModel model = ModelHolder.getModel ();

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb,

                NsiTable.getNsiTable (3).getVocSelect (),
                NsiTable.getNsiTable (34).getVocSelect (),
                NsiTable.getNsiTable (35).getVocSelect (),
                NsiTable.getNsiTable (37).getVocSelect (),
                NsiTable.getNsiTable (38).getVocSelect (),
                NsiTable.getNsiTable (39).getVocSelect (),
                NsiTable.getNsiTable (40).getVocSelect (),
                
                model
                    .select (NsiTable.getNsiTable (33), "code AS id", LABEL_FIELD_NAME_NSI_33 + " AS label")
                    .where (LABEL_FIELD_NAME_NSI_33 + " IS NOT NULL")
                    .orderBy ("code"),

                model
                    .select (VocGisStatus.class, "id", "label")
                    .orderBy ("id")

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
    
    
}
