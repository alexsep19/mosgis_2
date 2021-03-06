package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;

public class ActualCharterGeneralNeedsMunicipalResource extends View {
    
    public static final String TABLE_NAME = "vw_ch_gen_need_res";    

    public ActualCharterGeneralNeedsMunicipalResource () {        
        
        super  (TABLE_NAME, "НСИ 337 по адресам в рамках действующих договоров управления");

        cols   (ActualBuildingGeneralNeedsMunicipalResource.c.class);
        pk     (ActualBuildingGeneralNeedsMunicipalResource.c.UUID);        

    }

    @Override
    public final String getSQL () {

        return "SELECT" +
            " g.uuid" +
            " , co.fiashouseguid " +
            " , co.uuid_org " +
            "FROM" +
            "  vw_charter_objects co " +
            "  INNER JOIN " + GeneralNeedsMunicipalResource.TABLE_NAME + " g ON (g.uuid_org=co.uuid_org AND g.is_deleted=0) " +
            "WHERE" +
            "  co.is_own=1";

    }

}