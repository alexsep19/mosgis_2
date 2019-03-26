package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;

public class ActualContractMainMunicipalServices extends View {
    
    public static final String TABLE_NAME = "vw_ca_municipal_svc";    

    public ActualContractMainMunicipalServices () {        
        
        super  (TABLE_NAME, "Коммунальные услуги, оказываемые по адресам в рамках действующих договоров управления");

        cols   (ActualBuildingMainMunicipalServices.c.class);
        pk     (ActualBuildingMainMunicipalServices.c.ID);        

    }

    @Override
    public final String getSQL () {

        return "SELECT" +
            "  ms.uuid id" +
            "  , co.fiashouseguid" +
            "  , ms.uuid uuid_m_m_service" +
            "  , cs.startdate" +
            "  , cs.enddate " +
            "  , co.uuid_org " +
            "FROM" +
            "  vw_contract_objects co " +
            "  INNER JOIN tb_contract_services cs ON (cs.uuid_contract_object=co.uuid AND cs.is_deleted=0 AND cs.is_additional=0) " +
            "  INNER JOIN tb_municipal_svc     ms ON (ms.is_deleted=0 AND ms.uuid_org = co.uuid_org AND ms.code_vc_nsi_3 = cs.code_vc_nsi_3) " +
            "WHERE" +
            "  co.is_own=1";

    }

}