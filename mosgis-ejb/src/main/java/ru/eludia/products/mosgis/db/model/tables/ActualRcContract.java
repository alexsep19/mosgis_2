package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocRcContractServiceTypes;

public class ActualRcContract extends View {

    public ActualRcContract () {
        super  ("vw_rc_ctr_actual", "Список действующих договоров РЦ");
        cols   (EnTable.c.class);
        cols   (RcContract.c.class);
        pk     (EnTable.c.UUID);
    }

    @Override
    public final String getSQL () {

        QP qp = new QP ("SELECT ");

        for (EnTable.c i: EnTable.c.values ()) {
            qp.append (i.lc ());
            qp.append (',');
        }

        for (RcContract.c i: RcContract.c.values ()) {
            qp.append (i.lc ());
            qp.append (',');
        }
        
        qp.setLastChar (' ');
        
        qp.append ("FROM ");
        qp.append (getName (RcContract.class));
        
        qp.append (" WHERE ");

        qp.append (EnTable.c.IS_DELETED.lc ());
        qp.append ("=0");
        
        qp.append (" AND ");

        qp.append (RcContract.c.ID_CTR_STATUS.lc ());
        qp.append ('=');
        qp.append (VocGisStatus.i.APPROVED.getId ());

        qp.append (" AND ");

        qp.append (RcContract.c.ID_SERVICE_TYPE.lc ());
        qp.append ('=');
        qp.append (VocRcContractServiceTypes.i.BILLING.getId ());

        qp.append (" AND ");

        qp.append (RcContract.c.DT_FROM.lc ());
        qp.append ("<= SYSDATE");

        qp.append (" AND (");

        qp.append (RcContract.c.DT_TO.lc ());
        qp.append (" IS NULL OR ");
        qp.append (RcContract.c.DT_TO.lc ());
        qp.append (" >= SYSDATE");
        qp.append (')');
        
        return qp.getSQL ();
        
    }

}