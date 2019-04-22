package ru.eludia.products.mosgis.db.model.voc.nsi;

import java.sql.SQLException;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.tables.BaseDecisionMSP;

public class Nsi302 extends View {

    public enum c implements ColEnum {

        ID     (Type.STRING, 20, null, "Код"),
        LABEL  (Type.STRING,     null, "Наименование"),
	GUID   (Type.UUID, null, "Глобально-уникальный идентификатор элемента справочника"),

	CODE_VC_NSI_301              (Type.STRING,  20,     null, "Тип решения о мерах социальной поддержки (НСИ 301)"),

	ISAPPLIEDTOSUBSIDIARIES      (Type.BOOLEAN, null, "1, если применяется для субсидий, иначе 0"),
	ISAPPLIEDTOREFUNDOFCHARGES   (Type.BOOLEAN, null, "1, если применяется для компенсации расходов, иначе 0")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public Nsi302 () {
        super  ("vw_nsi_302", "Основания принятия решений о мерах социальной поддержки");
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " code id "
            + " , label "
	    + " , guid "
	    + " , code_vc_nsi_301 "
	    + " , isappliedtosubsidiaries"
	    + " , isappliedtorefundofcharges "
            + "FROM "
            + BaseDecisionMSP.TABLE_NAME
            + " WHERE"
            + " isactual=1"
        ;

    }

    public static void addTo(DB db, JsonObjectBuilder job) throws SQLException {

        db.addJsonArrays(job,
            db.getModel()
		.select  (Nsi302.class, "AS vc_nsi_302", "*")
                .orderBy ("vc_nsi_302.label")
        );
    }
}