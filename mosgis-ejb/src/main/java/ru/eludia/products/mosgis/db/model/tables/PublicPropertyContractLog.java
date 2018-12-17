package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.gosuslugi.dom.schema.integration.house_management.ImportPublicPropertyContractRequest;

public class PublicPropertyContractLog extends GisWsLogTable {

    public PublicPropertyContractLog () {

        super ("tb_pp_ctr__log", "История редактирования [сведений о размере платы за] услуги управления", PublicPropertyContract.class
            , EnTable.c.class
            , PublicPropertyContract.c.class
        );
        
    }
    
    public static ImportPublicPropertyContractRequest toImportPublicPropertyContractRequest (Map<String, Object> r) {
        final ImportPublicPropertyContractRequest createImportPublicPropertyContractRequest = new ImportPublicPropertyContractRequest ();
        final ImportPublicPropertyContractRequest.Contract contract = new ImportPublicPropertyContractRequest.Contract ();
        final ImportPublicPropertyContractRequest.Contract.PublicPropertyContract publicPropertyContract = toContractPublicPropertyContract (r);
        contract.setPublicPropertyContract (publicPropertyContract);
        contract.setTransportGUID (UUID.randomUUID ().toString ());
        createImportPublicPropertyContractRequest.getContract ().add (contract);
        return createImportPublicPropertyContractRequest;
    }
    
    private static ImportPublicPropertyContractRequest.Contract.PublicPropertyContract toContractPublicPropertyContract (Map<String, Object> r) {
        ImportPublicPropertyContractRequest.Contract.PublicPropertyContract result = DB.to.javaBean (ImportPublicPropertyContractRequest.Contract.PublicPropertyContract.class, r);
        return result;
    }

    public Get get (Object id) {
        return getModel ().get (this, id, "*");
    }
                
}