package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.BaseDecisionMSP;
import ru.eludia.products.mosgis.db.model.tables.BaseDecisionMSPLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.InBaseDecisionMSP;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi301;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;
import ru.eludia.products.mosgis.rest.api.BaseDecisionMSPLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BaseDecisionMSPImpl extends BaseCRUD<BaseDecisionMSP> implements BaseDecisionMSPLocal {

    @Resource (mappedName = "mosgis.inNsiBaseDecisionMSPsQueue")
    Queue queue;

    @Resource(mappedName = "mosgis.inImportBaseDecisionMSPsQueue")
    Queue inImportBaseDecisionMSPsQueue;

    @Override
    public Queue getQueue (VocAction.i action) {

        switch (action) {
            case CREATE:
            case UPDATE:
            case DELETE:
                return queue;
            default:
                return null;
        }

    }

    private void filterOffDeleted (Select select) {
        select.and ("is_deleted", 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        filterOffDeleted (select);

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and (BaseDecisionMSP.c.LABEL_UC.lc () + " LIKE ?%", searchString.toUpperCase ());
        
    }
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            filterOffDeleted (select);
        }

    }
    
    private void checkFilter (JsonObject data, BaseDecisionMSP.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toMaybeOne (BaseDecisionMSPLog.class).on ()
            .toMaybeOne (OutSoap.class,       "err_text").on ()
            .toMaybeOne (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
            .orderBy ("root." + BaseDecisionMSP.c.LABEL_UC.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObject data = p.getJsonObject ("data");
        
        checkFilter (data, BaseDecisionMSP.c.UUID_ORG, select);

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
            .toMaybeOne (BaseDecisionMSPLog.class, "AS log").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("log.uuid_out_soap=out_soap.uuid")
            .toMaybeOne (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
        ));
        
        VocGisStatus.addTo (job);
        VocAction.addTo (job);
        Nsi301.i.addTo (job);
    });}

    @Override
    public JsonObject getVocs () {return fetchData ((db, job) -> {
        
        final MosGisModel m = ModelHolder.getModel ();

        VocGisStatus.addTo (job);
        VocAction.addTo (job);
        Nsi301.i.addTo(job);
    });}

    @Override
    public JsonObject doImport(User user) {

	try (DB db = ModelHolder.getModel().getDb()) {

	    String uuidOrg = user.getUuidOrg();

	    if (uuidOrg == null) {
		logger.warning("User has no org set, access prohibited");
		throw new ValidationException("foo", "Отсутствует организация пользователя, доступ запрещен");
	    }

	    Map<String, Object> data = new HashMap ();

	    data.put(InBaseDecisionMSP.c.UUID_ORG.lc(), uuidOrg);

	    UUID uuid = (UUID) db.insertId(InBaseDecisionMSP.class, data);

	    UUIDPublisher.publish(inImportBaseDecisionMSPsQueue, uuid);

	} catch (Exception ex) {
	    Logger.getLogger(BaseDecisionMSP.class.getName()).log(Level.SEVERE, null, ex);
	}

	return EMPTY_JSON_OBJECT;
    }
}