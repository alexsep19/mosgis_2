package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWork;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
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
            .toOne      (OverhaulRegionalProgram.class, "startyear AS startyear", "endyear AS endyear").on ()
            .toOne      (House.class, "AS house", "address AS address", "fiashouseguid AS fiashouseguid").on ()
            .toOne      (VocBuilding.class, "AS build", "oktmo AS oktmo").on ("build.houseguid = house.fiashouseguid")
        ));
        
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        
        db.addJsonArrays (job,
            NsiTable.getNsiTable (218).getVocSelect (),
            
            db.getModel ()
                .select (OverhaulRegionalProgramHouseWork.class, "AS works", "startyearmonth", "endyearmonth")
                .toOne  (VocOverhaulWorkType.class, "AS type", "code AS code_nsi_219", "code_vc_nsi_218 AS code_nsi_218", "servicename AS servicename").on ("type.uuid = works.work")
            
        );
        
    });}

}
