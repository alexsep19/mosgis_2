package ru.eludia.products.mosgis.rest.api;

import java.sql.SQLException;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

public interface VotingProtocolsLocal extends CRUDBackend {
    
    public JsonObject getVocs ();
    public JsonObject getCachAndOktmo (String fiashouseguid) throws SQLException;
    
}
