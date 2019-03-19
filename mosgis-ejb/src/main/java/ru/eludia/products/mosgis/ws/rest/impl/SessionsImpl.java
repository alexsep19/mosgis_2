package ru.eludia.products.mosgis.ws.rest.impl;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.rest.api.SessionsLocal;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTerritory;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jmx.Conf;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SessionsImpl implements SessionsLocal {

    private static final Logger logger = Logger.getLogger (SessionsImpl.class.getName ());
    
    private static final JsonObject ADMIN_ROLE = Json.createObjectBuilder ().add ("admin", 1).build ();
    
    private JsonObject getRoles (final Object uuid_org, JsonObjectBuilder jb, final DB db) throws SQLException {
        
        if (uuid_org == null) return ADMIN_ROLE;
        
        JsonObjectBuilder roles = Json.createObjectBuilder ();
                    
        jb.add ("uuid_org", uuid_org.toString ());            
            
        db.forEach (ModelHolder.getModel ().select (VocOrganizationNsi20.class, "code").where ("uuid", uuid_org), (rs) -> {
            final String role = "nsi_20_" + rs.getString (1);
            logger.info ("adding role: " + role);
            roles.add (role, 1);
        });
        
        db.forEach (ModelHolder.getModel ()
                .select (VocOrganizationTerritory.class)
                .toOne  (VocOktmo.class, "code").on ()
                .where  ("uuid_org", uuid_org),
                (rs) -> {
                    final String oktmo = "oktmo_" + rs.getString(1);
                    logger.info ("adding oktmo: " + oktmo);
                    roles.add (oktmo, 1);
        });
            
        return roles.build ();
        
    }
            
    @Override
    public JsonObject create (String login, String password) {
        
        if (login == null || password == null) return null;
                
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            Map<String, Object> user = db.getMap (ModelHolder.getModel ()
                .select     (VocUser.class, "salt", "sha1", "uuid", "label", "uuid_org", "is_locked")
                .toMaybeOne (VocOrganization.class, "label AS label_org").on ()
                .where ("login", login)
            );

            if (user != null) {
                
                if ("1".equals(user.get ("is_locked").toString())) {
                    logger.warning ("Login attempt of blocked user:  " + login);
                    return null;
                }
                
                String asIs = user.get ("sha1").toString ();
                String toBe = VocUser.encrypt ((UUID) user.get ("salt"), password);

                if (asIs.equals (toBe)) {

                    jb.add ("id", user.get ("uuid").toString ());
                    jb.add ("label", user.get ("label").toString ());
                    jb.add ("label_org", DB.to.String (user.get ("label_org")));
                    jb.add ("role", getRoles (user.get ("uuid_org"), jb, db));

                    return jb.build ();

                }
                else {
                    logger.warning ("Wrong password entered for " + login + ": " + asIs + " != " + toBe);
                    return null;
                }

            }

        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }

        String adminLogin = Conf.get (VocSetting.i.USER_ADMIN_LOGIN);
        String adminPassword = Conf.get (VocSetting.i.USER_ADMIN_PASSWORD);

        if (!adminLogin.isEmpty () && !adminPassword.isEmpty () && adminLogin.equals (login)) {
            
            if (!adminPassword.equals (password)) return null;
            
            jb.add ("id",  "-1");
            jb.add ("label", "Условный администратор");
            jb.add ("role", ADMIN_ROLE);
            
            return jb.build ();
            
        }

        logger.warning ("Unknown login: " + login);
        return null;

    }

}