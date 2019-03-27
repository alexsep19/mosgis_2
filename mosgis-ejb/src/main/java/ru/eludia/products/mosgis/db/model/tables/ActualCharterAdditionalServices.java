package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;

public class ActualCharterAdditionalServices extends View {
    
    public static final String TABLE_NAME = "vw_ch_add_svc";    

    public ActualCharterAdditionalServices () {        
        
        super  (TABLE_NAME, "Дополнительные услуги, оказываемые по адресам в рамках действующих уставов");

        cols   (ActualBuildingAdditionalServices.c.class);
        pk     (ActualBuildingAdditionalServices.c.ID);        

    }

    @Override
    public final String getSQL () {

        return "SELECT" +
            "  cs.uuid id" +
            "  , co.fiashouseguid" +
            "  , cs.uuid_add_service" +
            "  , cs.startdate" +
            "  , cs.enddate " +
            "  , co.uuid_org " +
            "FROM" +
            "  vw_charter_objects co " +
            "  INNER JOIN tb_charter_services cs ON (cs.uuid_charter_object=co.uuid AND cs.is_deleted=0 AND cs.is_additional=1) " +
            "WHERE" +
            "  co.is_own=1";

    }

}