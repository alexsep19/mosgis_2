package ru.eludia.products.mosgis.jms.gis.poll;

import static ru.eludia.base.DB.HASH;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ws.rs.core.Response;

import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.incoming.InLegalAct;
import ru.eludia.products.mosgis.db.model.tables.PlannedExamination;
import ru.eludia.products.mosgis.db.model.tables.LegalAct;
import ru.eludia.products.mosgis.db.model.tables.LegalActLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inImportLegalActFilesQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportLegalActFileMDB extends UUIDMDB<LegalAct> {

    @EJB
    protected RestGisFilesClient restGisFilesClient;

	@Override
	protected Get get(UUID uuid) {
	    return (Get) ModelHolder.getModel().get(getTable(), uuid, "AS root", "*")
		.toMaybeOne(InLegalAct.class, "AS in_la").on()
		.toMaybeOne(VocOrganization.class, "AS org", "orgppaguid").on("org.uuid = in_la.uuid_org")
	    ;
	}

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException, Exception {

	if (r.get("org.orgppaguid") == null) {
	    throw new Exception("Can't find orgppaguid");
	}

        Response rp = restGisFilesClient.get ((UUID) r.get ("org.orgppaguid"), RestGisFilesClient.Context.CONTENTMANAGEMENT, (UUID) r.get ("attachmentguid"));

        db.update (getTable (), HASH (
            "uuid", uuid,
            "mime", rp.getHeaderString ("Content-Type"),
            "len",  rp.getHeaderString ("Content-Length")
        ));

        InputStream in = rp.readEntity (InputStream.class);

        try (PreparedStatement st = db.getConnection ().prepareStatement ("SELECT body FROM " + getTable ().getName () + " WHERE uuid = ? FOR UPDATE")) {

            st.setString (1, uuid.toString ().replace ("-", "").toUpperCase ());

            try (ResultSet rs = st.executeQuery ()) {

                if (rs.next ()) {

                    Blob blob = rs.getBlob (1);

                    long sum = 0L;

                    try (OutputStream out = blob.setBinaryStream (0L)) {

                        byte [] buffer = new byte [8 * 1024];
                        int len;
                        while ((len = in.read (buffer)) > 0) {
                            out.write (buffer, 0, len);
                            sum += len;
                        }

                    }
                    catch (IOException ex) {
                        logger.log (Level.SEVERE, "Can't set BLOB stream", ex);
                    }

                    logger.info (sum + " bytes wrote");

                }

            }

            db.update (getTable (), HASH (
                "uuid",      uuid,
                "id_status", 0
            ));

        }

    }

}