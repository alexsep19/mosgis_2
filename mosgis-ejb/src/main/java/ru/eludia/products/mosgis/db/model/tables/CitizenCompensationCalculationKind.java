package ru.eludia.products.mosgis.db.model.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocServiceType;
import ru.gosuslugi.dom.schema.integration.msp.ExportCategoryType;
import ru.gosuslugi.dom.schema.integration.msp.ExportCategoryType.Actual;


public class CitizenCompensationCalculationKind extends EnTable {

    public static final String TABLE_NAME = "tb_cit_comp_calc_kinds";

    public enum c implements EnColEnum {

	UUID_CIT_COMP_CAT            (CitizenCompensationCategory.class, "Категория граждан"),
	CATEGORYGUID                 (Type.UUID, null, "Категория граждан"),

	SERVICE                      (VocServiceType.class,  null, "На оплату какого значения предоставляется компенсация"),

	CODE_VC_NSI_275              (Type.STRING,  20, null, "Норматив для начисления компенсации (НСИ 275)"),
	APPLIESTOALLFAMILYMEMBERS    (Type.BOOLEAN, null, "Распространяется на всех членов семьи"),
	DISCOUNTSIZE                 (Type.NUMERIC, 5, 2, null, "Размер, предоставляемой компенсации"),
	VALIDFROM                    (Type.DATE, null, "Дата начала предоставления компенсации"),
	VALIDTO                      (Type.DATE, null, "Дата окончания предоставления компенсации"),

	COMMENT_                     (Type.STRING, null, "Примечание"),

	ID_CTR_STATUS                (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS            (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
	    return false;
        }

    }

    public CitizenCompensationCalculationKind () {

        super (TABLE_NAME, "Перечень отдельных категорий граждан, имеющих право на получение компенсации расходов: порядок расчета компенсации расходов от фактических расходов на оплату жилищно-коммунальных услуг");

        cols (c.class);

        key (c.UUID_CIT_COMP_CAT);

	trigger ("BEFORE INSERT OR UPDATE", ""
	+ "BEGIN "
            + "IF :NEW.UUID_CIT_COMP_CAT IS NULL AND :NEW.CATEGORYGUID IS NOT NULL THEN "
	    + "  SELECT UUID INTO :NEW.UUID_CIT_COMP_CAT FROM " + CitizenCompensationCategory.TABLE_NAME + " WHERE CATEGORYGUID = :NEW.CATEGORYGUID; "
	    + "END IF; "
        + "END;");
    }
    
    public static Map<String, Object> toHash(ExportCategoryType.Actual t) {

	Map<String, Object> result = DB.HASH(
	    c.SERVICE.lc(), VocServiceType.i.forId(t.getService()),
	    c.CODE_VC_NSI_275.lc(), t.getNsiDiscountAmountLimitationCode().getCode(),
	    c.APPLIESTOALLFAMILYMEMBERS.lc(), DB.ok(t.isAppliesToAllFamilyMembers())? 1 : 0,
	    c.DISCOUNTSIZE.lc(), t.getDiscountSize(),
	    c.VALIDFROM.lc(), t.getValidFrom(),
	    c.VALIDTO.lc(), t.getValidTo(),
	    c.COMMENT_.lc(), t.getComment()
	);

	return result;
    }

}