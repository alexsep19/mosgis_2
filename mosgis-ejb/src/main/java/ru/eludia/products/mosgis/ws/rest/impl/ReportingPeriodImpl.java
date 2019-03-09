package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.phys.PhysicalCol;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.ReportingPeriod;
import ru.eludia.products.mosgis.db.model.tables.ReportingPeriodLog;
import ru.eludia.products.mosgis.db.model.tables.WorkingList;
import ru.eludia.products.mosgis.db.model.tables.WorkingListItem;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlan;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanItem;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.ReportingPeriodLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ReportingPeriodImpl extends BaseCRUD<ReportingPeriod> implements ReportingPeriodLocal {

    @Resource (mappedName = "mosgis.inReportingPeriodsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }

    @Override
    protected void publishMessage (VocAction.i action, String id_log) {
        
        switch (action) {
            case APPROVE:
                super.publishMessage (action, id_log);
            default:
                return;
        }
        
    }

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        final String fhg = WorkingList.c.FIASHOUSEGUID.lc ();
        
        final JsonObject item = db.getJsonObject (ModelHolder.getModel ()
                .get (ReportingPeriod.class, id, "*")                
                .toOne (WorkingPlan.class, "AS plan", "*").on ()
                .toOne (WorkingList.class
                        , fhg + " AS " + fhg
                        , WorkingList.c.DT_FROM.lc ()
                        , WorkingList.c.DT_TO.lc ()
                        , WorkingList.c.ID_CTR_STATUS.lc ()
                ).on ()
                .toMaybeOne (VocBuilding.class, "AS fias", "label").on ()
                .toMaybeOne (ContractObject.class, "AS cao", "uuid", "startdate", "enddate").on ()
                .toMaybeOne (Contract.class, "AS ca", "*").on ()
                .toMaybeOne (CharterObject.class, "AS cho", "uuid", "startdate", "enddate").on ()
                .toMaybeOne (Charter.class, "AS ch", "*").on ()
                .toMaybeOne (VocOrganization.class, "AS chorg", "label").on ("ch.uuid_org=chorg.uuid")                                
                
                .toMaybeOne (ReportingPeriodLog.class, "AS rpl").on ()
                .toMaybeOne (OutSoap.class, "err_text").on ("rpl.uuid_out_soap=out_soap.uuid")
                
        );

        job.add ("item", item);
        
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        
        VocBuilding.addCaCh (db, job, item.getString (fhg));
        
        db.addJsonArrays (job, 

            NsiTable.getNsiTable (3).getVocSelect (),            
            NsiTable.getNsiTable (57).getVocSelect (),
            NsiTable.getNsiTable (56).getVocSelect (),
            
            db.getModel ()
                .select (OrganizationWork.class, "AS org_works", "uuid AS id", "label", "code_vc_nsi_56")
                .toOne (VocOkei.class, "national AS unit").on ()
                .where  ("uuid_org", item.getString ("ca.uuid_org", item.getString ("ch.uuid_org", "00")))
                .and    ("is_deleted", 0)
                .and    ("elementguid IS NOT NULL")
                .orderBy ("org_works.label")

        );

    });}

    @Override
    public JsonObject doFill (String id, User user) {return doAction ((db) -> {
        
        Model m = db.getModel ();
        
        final Table planTable = m.get (WorkingPlanItem.class);
        final String listTable = m.get (WorkingListItem.class).getName ();
        
        PhysicalCol key = planTable.getColumn (WorkingPlanItem.c.UUID_REPORTING_PERIOD).toPhysical ();
        
        QP qp = new QP ("MERGE INTO ");        
        qp.append (planTable.getName ());
        qp.append (" n USING (SELECT pi.uuid");
        
        qp.append (", pi.");
        qp.append (WorkingPlanItem.c.WORKCOUNT.lc ());
        qp.append (" ");
        qp.append (WorkingPlanItem.c.COUNT.lc ());
        
        qp.append (", li.");
        qp.append (WorkingListItem.c.AMOUNT.lc ());
        qp.append (" ");
        qp.append (WorkingPlanItem.c.AMOUNT.lc ());

        qp.append (", li.");
        qp.append (WorkingListItem.c.PRICE.lc ());
        qp.append (" ");
        qp.append (WorkingPlanItem.c.PRICE.lc ());
        
        qp.append (" FROM ");
        qp.append (planTable.getName ());
        qp.append (" pi INNER JOIN  ");
        qp.append (listTable);
        qp.append (" li ON pi.");
        qp.append (WorkingPlanItem.c.UUID_WORKING_LIST_ITEM.lc ());
        qp.append ("=li.uuid WHERE ");
        qp.add (key, id);
        qp.append ("=?) o ON (n.uuid=o.uuid) WHEN MATCHED THEN UPDATE SET ");
        
        qp.append ("n.");
        qp.append (WorkingPlanItem.c.COUNT.lc ());
        qp.append ("=o.");
        qp.append (WorkingPlanItem.c.COUNT.lc ());
        
        qp.append (", n.");
        qp.append (WorkingPlanItem.c.AMOUNT.lc ());
        qp.append ("=o.");
        qp.append (WorkingPlanItem.c.AMOUNT.lc ());

        qp.append (", n.");
        qp.append (WorkingPlanItem.c.PRICE.lc ());
        qp.append ("=o.");
        qp.append (WorkingPlanItem.c.PRICE.lc ());
        
        db.d0 (qp);
        
    });}
    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (EnTable.c.UUID,               id,
            ReportingPeriod.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}
    
    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {
                
        final Map<String, Object> r = HASH (
            EnTable.c.UUID,               id,
            ReportingPeriod.c.ID_CTR_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );
                
        db.update (getTable (), r);
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}    

    @Override
    public JsonObject select (JsonObject p, User user) {return EMPTY_JSON_OBJECT;}
    
}