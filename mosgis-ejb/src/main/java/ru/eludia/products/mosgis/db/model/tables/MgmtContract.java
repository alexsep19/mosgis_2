package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class MgmtContract extends View {

    public MgmtContract () {
        
        super ("tb_mgmt_contracts", "Договоры управления");        
        cols  (EnTable.c.class);
        cols  (Contract.c.class);
        pk    (EnTable.c.UUID);
        fk    ("id_ctr_state", VocGisStatus.class, "Состояние договора с точки зрения mosgis");
        
    }

    @Override
    public final String getSQL () {
        
        StringBuilder sb = new StringBuilder ("SELECT ");
        
        for (EnTable.c i: EnTable.c.values ()) {
            sb.append ("t.");
            sb.append (i.lc ());
            sb.append (',');
        }
        
        for (Contract.c i: Contract.c.values ()) {
            sb.append ("t.");
            sb.append (i.lc ());
            sb.append (',');
        }

        return sb +
            "CASE" +
            "    WHEN ID_CTR_STATUS IN (100, 110) THEN 80 " +
            "    WHEN UUID_OUT_SOAP IS NULL THEN 80 " +
            "    WHEN PLANDATECOMPTETION < TRUNC (SYSDATE) THEN 60" +
            "    ELSE 50" +
            "  END id_ctr_state"+
            " FROM " + getName (Contract.class) + " t " + 
            " WHERE id_contract_type = " + VocGisContractType.i.MGMT.getId ();
        
    }

}