package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.products.mosgis.db.model.tables.License;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.LicenseLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.LicenseAccompanyingDocument;
import ru.eludia.products.mosgis.db.model.tables.LicenseHouse;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocLicenseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;
import ru.eludia.products.mosgis.db.model.tables.LicenseLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocDocumentStatus;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class LicenseImpl extends BaseCRUD<License> implements LicenseLocal {

    @Override
    public JsonObject getItem(String id, User user) {
        return fetchData((db, job) -> {

            Model m = ModelHolder.getModel();

            JsonObject item = db.getJsonObject(m
                    .get(License.class, id, "AS root", "*")
                    .toOne (VocOrganization.class, "AS org", VocOrganization.c.LABEL.lc(), VocOrganization.c.UUID.lc(), "id_type").on (License.c.UUID_ORG.lc())
                    .toOne (VocOrganization.class, "AS org_authority", VocOrganization.c.LABEL.lc()).on (License.c.UUID_ORG_AUTHORITY.lc())
                    .toMaybeOne(VocBuildingAddress.class, "AS fias",  "label").on ("root.region_fias_guid=fias.houseguid")
                    .toMaybeOne(LicenseLog.class, "AS log").on()
                    .toMaybeOne(OutSoap.class, "err_text").on("log.uuid_out_soap=out_soap.uuid")
            );

            job.add("item", item);
            
            VocLicenseStatus.addTo(job);
            VocDocumentStatus.addTo(job);
            VocAction.addTo(job);
            
            db.addJsonArrays (job, 
                
                NsiTable.getNsiTable(75).getVocSelect () // Тип документа

            );

        });
    } 

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");

    }    
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }

    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and ("label_uc LIKE %?%", searchString.toUpperCase ());
        
    }    
    
    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocLicenseStatus.addTo (jb);

        return jb.build ();
        
    }

    @Override
    public JsonObject select(JsonObject p) {
        final Model m = ModelHolder.getModel ();
        
        Select select = m.select (License.class,"AS root", "*", "uuid AS id")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.LABEL.lc()).on (License.c.UUID_ORG.lc())
            .toOne (VocOrganization.class, "AS org_authority", VocOrganization.c.LABEL.lc()).on (License.c.UUID_ORG_AUTHORITY.lc())
            .toMaybeOne(VocBuildingAddress.class, "AS fias",  "label").on ("root.region_fias_guid=fias.houseguid")
            .orderBy (License.c.LICENSE_REG_DATE.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        applySearch (Search.from (p), select);
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
    }

    @Override
    public JsonObject getDocuments(String id, JsonObject p) {
        
        final Model m = ModelHolder.getModel ();
        
        Select select = m.select (LicenseAccompanyingDocument.class,"AS root", "*")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.LABEL.lc()).on (LicenseAccompanyingDocument.c.UUID_ORG_DECISION.lc())
            .where(LicenseAccompanyingDocument.c.UUID_LICENSE.lc(), id)
            .orderBy (LicenseAccompanyingDocument.c.NAME.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
    }

    @Override
    public JsonObject getHouses(String id, JsonObject p) {
        
        final Model m = ModelHolder.getModel ();
        
        Select select = m.select (LicenseHouse.class,"AS root", "*")
            .toOne(VocBuildingAddress.class, "AS fias",  "label").on ("root.fiashouseguid=fias.houseguid")
            .where(LicenseAccompanyingDocument.c.UUID_LICENSE.lc(), id)
            .orderBy (LicenseHouse.c.HOUSEADDRESS.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }
    
    @Override
    public JsonObject select(JsonObject p, User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
