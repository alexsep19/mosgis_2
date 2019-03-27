package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;

public class ActualContractAdditionalServices extends View {
    
    public static final String TABLE_NAME = "vw_ca_add_svc";    

    public ActualContractAdditionalServices () {        
        
        super  (TABLE_NAME, "Дополнительные услуги, оказываемые по адресам в рамках действующих договоров управления");

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
            "  vw_contract_objects co " +
            "  INNER JOIN tb_contract_services cs ON (cs.uuid_contract_object=co.uuid AND cs.is_deleted=0 AND cs.is_additional=1) " +
            "WHERE" +
            "  co.is_own=1";

    }

}