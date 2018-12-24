package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.house_management.ImportPublicPropertyContractRequest;

public class AgreementPaymentLog extends GisWsLogTable {
    
    public AgreementPaymentLog () {

        super ("tb_pp_ctr_ap__log", "История редактирования платы по договорам на пользование общим имуществом", AgreementPayment.class
            , EnTable.c.class
            , AgreementPayment.c.class
        );
        
    }
    
    public Get getForExport (Object id) {

        return (Get) getModel ()
            .get (this, id, "*")
            .toOne (AgreementPayment.class, "AS ap"
                , AgreementPayment.c.ID_AP_STATUS.lc ()
            ).on ()
            .toOne (PublicPropertyContract.class, "AS ctr"
                , PublicPropertyContract.c.CONTRACTVERSIONGUID.lc ()
            ).on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctr.uuid_org=org.uuid")
        ;
        
    }    
    
    public static ImportPublicPropertyContractRequest toImportPublicPropertyContractRequest (Map<String, Object> r) {
        final ImportPublicPropertyContractRequest result = new ImportPublicPropertyContractRequest ();
        result.getAddAgreementPayment ().add (toAddAgreementPayment (r));
        return result;
    }

    private static ImportPublicPropertyContractRequest.AddAgreementPayment toAddAgreementPayment (Map<String, Object> r) {
        final ImportPublicPropertyContractRequest.AddAgreementPayment result = DB.to.javaBean (ImportPublicPropertyContractRequest.AddAgreementPayment.class, r);
        result.setDatePeriod (to (r));
        result.setTransportGUID (UUID.randomUUID ().toString ());
        if (result.getAgreementPaymentVersionGUID () == null) result.setContractVersionGUID (DB.to.String (r.get ("ctr.contractversionguid")));
        return result;
    }
    
    private static ImportPublicPropertyContractRequest.AddAgreementPayment.DatePeriod to (Map<String, Object> r) {
        return DB.to.javaBean (ImportPublicPropertyContractRequest.AddAgreementPayment.DatePeriod.class, r);
    }
    
    public static ImportPublicPropertyContractRequest toImportPublicPropertyContractAnnulRequest (Map<String, Object> r) {
        final ImportPublicPropertyContractRequest result = new ImportPublicPropertyContractRequest ();
        result.getAnnulAgreementPayment ().add (toAnnulAgreementPayment (r));
        return result;
    }    
    
    private static ImportPublicPropertyContractRequest.AnnulAgreementPayment toAnnulAgreementPayment (Map<String, Object> r) {
        final ImportPublicPropertyContractRequest.AnnulAgreementPayment result = DB.to.javaBean (ImportPublicPropertyContractRequest.AnnulAgreementPayment.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;        
    }

}