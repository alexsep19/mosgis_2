package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.rest.api.OutSoapLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.Base;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OutSoapImpl extends Base implements OutSoapLocal {

    private static final Logger logger = Logger.getLogger (OutSoapImpl.class.getName ());    
   
    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OutSoap.class, "AS root",
                "uuid",        
                "uuid_ack",    
                "svc",         
                "op",          
                "ts",          
                "ts_rp",       
                "is_failed",   
                "err_code",    
                "err_text",    
                "orgppaguid"
            )
            .toMaybeOne (VocOrganization.class, "AS org", VocOrganization.c.LABEL.lc ()).on ("root.orgppaguid=org.orgppaguid")
            .orderBy ("ts DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        db.addJsonArrayCnt (job, select);

    });}

}