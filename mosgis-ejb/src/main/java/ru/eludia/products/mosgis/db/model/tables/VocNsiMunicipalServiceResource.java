package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;

public class VocNsiMunicipalServiceResource extends View {

    public enum c implements ColEnum {
        CODE_VC_NSI_3        (Type.STRING, "Коммунальная услуга"),
        CODE_VC_NSI_239      (Type.STRING, "Тарифицируемый ресурс"),
        CODE_VC_OKEI         (VocOkei.class, "Единица измерения"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public VocNsiMunicipalServiceResource () {
        super  ("vw_ms_r", "Связь вида коммунальной услуги, тарифицируемого ресурса и единиц измерения");
        cols   (c.class);
    }

    @Override
    public final String getSQL () {

        StringBuilder sb = new StringBuilder ("SELECT ");

        return sb.toString ()
            + " vc_nsi_3.code     code_vc_nsi_3 "
            + " , vc_nsi_239.code code_vc_nsi_239 "
            + " , vc_okei.code    code_vc_okei "
            + " FROM vc_nsi_236 o "
            + " LEFT  JOIN vc_okei             ON o.unit        = vc_okei.code "
            + " INNER JOIN vc_nsi_236 parent_1 ON parent_1.guid = o.parent "
            + " INNER JOIN vc_nsi_236 parent_2 ON parent_2.guid = parent_1.parent "
            + " INNER JOIN vc_nsi_239          ON parent_1." + VocNsi236.c.GUID_VC_NSI_239.lc() + " = vc_nsi_239.guid "
            + " INNER JOIN vc_nsi_3            ON parent_2." + VocNsi236.c.GUID_VC_NSI_3.lc() + " = vc_nsi_3.guid "
        ;

    }

}