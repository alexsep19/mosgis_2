package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class StuckCharters extends View {

    @Override
    public String getSQL () {
        
        return
                
            "SELECT " +
            "  c.uuid " +
            "  , c.id_ctr_status" +
            "  , s.uuid uuid_out_soap" +
            " FROM " +
            "  tb_charters c" +
            "  INNER JOIN tb_charters__log l ON c.id_log = l.uuid" +
            "  LEFT JOIN out_soap s ON l.uuid_out_soap = s.uuid" +
            " WHERE" +
            "  l.ts < SYSDATE - (SELECT value / 30 / 24 FROM tb_settings WHERE id='ttl.charters')" +
            "  AND MOD (c.id_ctr_status, 10) IN (2, 3)";
        
    }

    public enum c implements ColEnum {

        UUID               (Type.UUID, "uuid договора"),
        ID_CTR_STATUS      (VocGisStatus.class, "Статус договора"),
        UUID_OUT_SOAP      (OutSoap.class, "uuid SOAP-запроса")        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;
        private String sql = null;
        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
    }

    public StuckCharters () {

        super ("vw_stuck_charters", "Зависшие договоры");
        
        cols   (c.class);
        pk     (c.UUID);
        
    }
                
}