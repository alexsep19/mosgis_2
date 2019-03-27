package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocChargeInfoType;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi50;

public class AnyChargeInfo extends View {

    public enum c implements ColEnum {
        ID                   (Type.UUID,     "id"),
        LABEL_TYPE           (Type.STRING,   "Тип"),
        LABEL                (Type.STRING,   "Услуга"),
        OKEI                 (VocOkei.class, "Единицы измерения (ОКЕИ)"),
        SORTORDER            (Type.STRING,   "Порядок сортировки"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public AnyChargeInfo () {
        super  ("vw_charge_info", "Строки платёжных документов");
        cols   (EnTable.c.class);
        cols   (ChargeInfo.c.class);
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        StringBuilder sb = new StringBuilder ("SELECT ");

        for (EnTable.c i: EnTable.c.values ()) {
            sb.append ("o.");
            sb.append (i.lc ());
            sb.append (',');
        }

        for (ChargeInfo.c i: ChargeInfo.c.values ()) {
            sb.append ("o.");
            sb.append (i.lc ());
            sb.append (',');
        }

        return sb.toString ()
            + " o.uuid id "
            + " , t.label " + c.LABEL_TYPE
            + " , COALESCE (m.label, a.label, n.label, g.generalmunicipalresourcename) " + c.LABEL
            + " , COALESCE (m.okei,  a.okei,  n.okei,  g.okei) " + c.OKEI
            + " , COALESCE (m.label, a.label, n.label, g.sortorder) " + c.SORTORDER
            + " FROM " + ChargeInfo.TABLE_NAME + " o"
            + " INNER JOIN " + VocChargeInfoType.TABLE_NAME + " t ON t.id = o." + ChargeInfo.c.ID_TYPE
            + " LEFT  JOIN " + MainMunicipalService.TABLE_NAME + " m ON m.uuid = " + ChargeInfo.c.UUID_M_M_SERVICE
            + " LEFT  JOIN " + AdditionalService.TABLE_NAME + " a ON a.uuid = " + ChargeInfo.c.UUID_ADD_SERVICE
            + " LEFT  JOIN " + Nsi50.TABLE_NAME + " n ON n.id = " + ChargeInfo.c.CODE_VC_NSI_50
            + " LEFT  JOIN " + GeneralNeedsMunicipalResource.TABLE_NAME + " g ON g.uuid = " + ChargeInfo.c.UUID_GEN_NEED_RES
        ;

    }

}