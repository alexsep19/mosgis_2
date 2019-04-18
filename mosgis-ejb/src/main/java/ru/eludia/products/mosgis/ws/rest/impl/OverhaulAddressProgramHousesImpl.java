package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouseLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.Sender;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.rest.api.OverhaulAddressProgramHousesLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OverhaulAddressProgramHousesImpl extends BaseCRUD <OverhaulAddressProgramHouse> implements OverhaulAddressProgramHousesLocal {
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OverhaulAddressProgramHouse.class, "AS root", "*")
                .where      ("program_uuid", p.getString ("program_uuid"))
                .toOne      (House.class, "AS house", "address AS address").on ()
                .toOne      (VocBuilding.class, "AS build", "oktmo AS oktmo").on ("build.houseguid = house.fiashouseguid")
                .where      ("is_deleted", 0)
                .limit      (p.getInt ("offset"), p.getInt ("limit"));
        
        List <Map <String, Object>> houses = db.getList (select);
        
        for (Map <String, Object> house: houses) {
            
            int genCnt = db.getCnt (ModelHolder.getModel ()
                    .select (OverhaulAddressProgramHouseWork.class, "AS works", "uuid")
                    .where  (OverhaulAddressProgramHouseWork.c.HOUSE_UUID.lc (), house.get ("uuid"))
                    .and    ("is_deleted", 0)
            );
            
            int approvedCnt = db.getCnt (ModelHolder.getModel ()
                    .select (OverhaulAddressProgramHouseWork.class, "AS works", "uuid")
                    .where  (OverhaulAddressProgramHouseWork.c.HOUSE_UUID.lc (), house.get ("uuid"))
                    .and    (OverhaulAddressProgramHouseWork.c.ID_OAPHW_STATUS.lc (), VocGisStatus.i.APPROVED.getId ())
            );
            
            house.put ("works_general_cnt", genCnt);
            house.put ("works_approved_cnt", approvedCnt);
            
        }

        job.add (select.getTableAlias (), DB.to.JsonArrayBuilder (houses).build ());
        job.add ("cnt", db.getCnt (select));
            
    });}
    
    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get        (getTable (), id, "*")
            .toOne      (OverhaulAddressProgram.class, "AS program", "startyear AS startyear", 
                                                                   "endyear AS endyear", 
                                                                   "startmonth AS startmonth", 
                                                                   "endmonth AS endmonth", 
                                                                   "is_deleted", 
                                                                   "org_uuid", 
                                                                   "last_succesfull_status").on ()
            .toOne      (House.class, "AS house", "address AS address", "fiashouseguid AS fiashouseguid").on ()
            .toOne      (VocBuilding.class, "AS build", "oktmo AS oktmo").on ("build.houseguid = house.fiashouseguid")
        ));
        
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        
        db.addJsonArrays (job,
            NsiTable.getNsiTable (218).getVocSelect (),
            
            db.getModel ()
                .select (VocOverhaulWorkType.class, "code AS id", "servicename AS label")
                .where  ("id_owt_status", 40)
                .and    ("isactual", 1)
                .and    ("is_deleted", 0)
        );
            
        
    });}
    
    @Override
    public JsonObject getLog (String id, JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OverhaulAddressProgramHouseLog.class, "AS log", "*", "uuid AS id")
            .and ("uuid_object", id)
            .toOne      (OverhaulAddressProgramHouse.class, "AS object").on ()
            .toOne      (House.class, "AS house", "address AS address").on ("house.uuid = object.house")
            .toOne      (VocBuilding.class, "AS build", "oktmo AS oktmo").on ("build.houseguid = house.fiashouseguid")
            .toMaybeOne (OutSoap.class, "AS soap", "id_status", "is_failed", "ts", "ts_rp", "err_text", "uuid_ack").on ()
            .toMaybeOne (WsMessages.class).on ()
            .toMaybeOne (Sender.class, Sender.c.LABEL.lc ()).on ()
            .toMaybeOne (VocUser.class, "label").on ()            
            .orderBy ("log.ts DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        db.addJsonArrayCnt (job, select);
        
    });}
    
}
