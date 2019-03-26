package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;

public class ActualCharterMainMunicipalServices extends View {
    
    public static final String TABLE_NAME = "vw_ch_municipal_svc";    

    public ActualCharterMainMunicipalServices () {

        super  (TABLE_NAME, "Коммунальные услуги, оказываемые по адресам в рамках действующих уставов");

        cols   (ActualBuildingMainMunicipalServices.c.class);
        pk     (ActualBuildingMainMunicipalServices.c.ID);        

    }

    @Override
    public final String getSQL () {

        return "SELECT" +
            "  cs.uuid id" +
            "  , co.fiashouseguid" +
            "  , ms.uuid uuid_m_m_service" +
            "  , cs.startdate" +
            "  , cs.enddate " +
            "  , co.uuid_org " +
            "FROM" +
            "  vw_charter_objects co " +
            "  INNER JOIN tb_charter_services cs ON (cs.uuid_charter_object=co.uuid AND cs.is_deleted=0 AND cs.is_additional=0) " +
            "  INNER JOIN tb_municipal_svc    ms ON (ms.is_deleted=0 AND ms.uuid_org = co.uuid_org AND ms.code_vc_nsi_3 = cs.code_vc_nsi_3) " +
            "WHERE" +
            "  co.is_own=1";

    }

}