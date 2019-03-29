package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramLog;
import ru.eludia.products.mosgis.db.model.tables.Sender;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.rest.api.OverhaulRegionalProgramHousesLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OverhaulRegionalProgramHousesImpl extends BaseCRUD <OverhaulRegionalProgramHouse> implements OverhaulRegionalProgramHousesLocal {

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OverhaulRegionalProgramHouse.class, "AS root", "*")
                .where      ("program_uuid", p.getString ("program_uuid"))
                .toOne      (House.class, "AS house", "address AS address").on ()
                .toOne      (VocBuilding.class, "AS build", "oktmo AS oktmo").on ("build.houseguid = house.fiashouseguid")
                .where      ("is_deleted", 0)
                .limit      (p.getInt ("offset"), p.getInt ("limit"));
        
        db.addJsonArrayCnt (job, select);
            
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get        (getTable (), id, "*")
            .toOne      (OverhaulRegionalProgram.class, "AS program", "startyear AS startyear", "endyear AS endyear", "is_deleted", "org_uuid").on ()
            .toOne      (House.class, "AS house", "address AS address", "fiashouseguid AS fiashouseguid").on ()
            .toOne      (VocBuilding.class, "AS build", "oktmo AS oktmo").on ("build.houseguid = house.fiashouseguid")
        ));
        
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        
        db.addJsonArrays (job,
            NsiTable.getNsiTable (218).getVocSelect (),
            
            db.getModel ()
                .select (OverhaulRegionalProgramHouseWork.class, "AS works", "startyearmonth", "endyearmonth")
                .toOne  (VocOverhaulWorkType.class, "AS type", "code AS code_nsi_219", "code_vc_nsi_218 AS code_nsi_218", "servicename AS servicename").on ("type.uuid = works.work"),
            
            db.getModel ()
                .select (VocOverhaulWorkType.class, "code AS id", "servicename AS label")
                .where  ("id_owt_status", 40)
            
        );
        
    });}
    
    @Override
    public JsonObject getLog (String id, JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OverhaulRegionalProgramHouseLog.class, "AS log", "*", "uuid AS id")
            .and ("uuid_object", id)
            .toOne      (OverhaulRegionalProgramHouse.class, "AS object").on ()
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
