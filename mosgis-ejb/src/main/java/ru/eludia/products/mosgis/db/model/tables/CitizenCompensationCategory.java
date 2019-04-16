package ru.eludia.products.mosgis.db.model.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBudgetLevel;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi237;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocServiceType;
import ru.gosuslugi.dom.schema.integration.msp.ExportCategoryType;

public class CitizenCompensationCategory extends EnTable {

    public static final String TABLE_NAME = "tb_cit_comp_cats";

    public enum c implements EnColEnum {

	UUID_ORG                     (VocOrganization.class, null, "Организация, которая создала данную запись"),

	CATEGORYNAME                 (Type.STRING,  500,    null, "Наименование отдельной категории граждан"),
	LABEL_UC                     (Type.STRING,          new Virt  ("UPPER(\"CATEGORYNAME\")"), "НАИМЕНОВАНИЕ"),

	CODE_VC_NSI_154              (Type.STRING,  20,     null, "Отдельные категории граждан, имеющие право на получение компенсаций расходов (НСИ 154)"),

	FROMDATE                     (Type.DATE, "Дата начала предоставления компенсаций расходов"),
	TODATE                       (Type.DATE, null, "Дата окончания предоставления компенсаций расходов"),
	BUDGETLEVEL                  (VocBudgetLevel.class, "Бюджет, за счет которого осуществляются выплаты. Возможные значения:"),

	SCOPE                        (Type.BOOLEAN, Bool.FALSE, "1, если действует в заданном ОКТМО, 0, если действует по всей Москве"),
	CODE_VC_NSI_237              (Nsi237.class, 20, new ru.eludia.base.model.def.String("77"), "Сфера действия НПА региональный уровень (НСИ 237)"),
	OKTMO_CODE                   (Type.STRING,  11,   null, "Код ОКТМО"),
	OKTMO                        (VocOktmo.class, null, "ОКТМО"),

	PAYOUTTERM                  (Type.STRING, null, "Срок перечисления компенсации"),
	PROVISIONDOCUMENTS          (Type.STRING, null, "Перечень документов, необходимых для получения компенсации"),
	DENIALREASONS               (Type.STRING, null, "Основания для отказа в предоставлении компенсации"),
	SUSPENSIONREASONS           (Type.STRING, null, "Основания для приостановления предоставления компенсации"),
	TERMINATIONREASONS          (Type.STRING, null, "Основания для прекращения предоставления компенсации"),
	RESUMPTIONREASONS           (Type.STRING, null, "Основания для возобновления предоставления компенсации"),
	RECALCULATIONREASONS        (Type.STRING, null, "Основания для перерасчета компенсации"),
	REFUNDREASONS               (Type.STRING, null, "Основания возврата излишне полученной суммы компенсации"),


	IS_FIXED                     (Type.BOOLEAN, new Virt  ("CASE WHEN FIXEDCOMPENSATIONSUM IS NOT NULL THEN 1 ELSE 0 END"), "Порядок расчета фиксированный"),
	FIXEDCOMPENSATIONSUM         (Type.NUMERIC, 20, 2, null, "Фиксированный размер денежной выплаты, руб."),
	FIXEDSUMESTABLISHMENTDATE    (Type.DATE, null, "Дата установления фиксированного размера"),
	COMMENT_                     (Type.STRING, null, "Примечание"),
	DESCRIPTION                  (Type.STRING, null, "Описание порядка расчета"),

	CATEGORYGUID                 (Type.UUID,             null,  "Глобально-уникальный идентификатор элемента справочника"),
	ID_LOG                       (CitizenCompensationCategoryLog.class, "Последнее событие редактирования"),

	ID_CTR_STATUS                (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS            (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {

            switch (this) {
                case UUID_ORG:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }

        }

    }

    public CitizenCompensationCategory () {

        super (TABLE_NAME, "Перечень отдельных категорий граждан, имеющих право на получение компенсации расходов");

        cols (c.class);

        key (c.UUID_ORG);

	trigger ("BEFORE INSERT OR UPDATE", ""
	+ "BEGIN "
            + "IF :NEW.OKTMO IS NULL AND :NEW.OKTMO_CODE IS NOT NULL THEN "
	    + "  SELECT ID INTO :NEW.OKTMO FROM " + VocOktmo.TABLE_NAME + " WHERE CODE = :NEW.OKTMO_CODE AND SECTION_CODE = '1'; "
	    + "END IF; "
        + "END;");
    }

    public static Map<String, Object> toHASH(ExportCategoryType cat) {

	final Map<String, Object> result = DB.HASH(
	    c.CATEGORYGUID.lc(), cat.getCategoryGuid(),
	    c.FROMDATE.lc(), cat.getFromDate(),
	    c.TODATE.lc(), cat.getToDate(),
	    c.CATEGORYNAME.lc(), cat.getCategoryName(),

	    c.PAYOUTTERM.lc(), cat.getPayoutTerm(),
	    c.PROVISIONDOCUMENTS.lc(), cat.getProvisionDocuments(),
	    c.DENIALREASONS.lc(), cat.getDenialReasons(),
	    c.SUSPENSIONREASONS.lc(), cat.getSuspensionReasons(),
	    c.TERMINATIONREASONS.lc(), cat.getTerminationReasons(),
	    c.RESUMPTIONREASONS.lc(), cat.getResumptionReasons(),
	    c.RECALCULATIONREASONS.lc(), cat.getRecalculationReasons(),
	    c.REFUNDREASONS.lc(), cat.getRefundReasons(),
	    c.DESCRIPTION.lc(), cat.getDescription(),

	    c.FIXEDCOMPENSATIONSUM.lc(), cat.getFixedCompensationSum(),
	    c.FIXEDSUMESTABLISHMENTDATE.lc(), cat.getFixedSumEstablishmentDate(),
	    c.COMMENT_.lc(), cat.getComment()
	);

	result.put(c.BUDGETLEVEL.lc(), VocBudgetLevel.i.forId(cat.getBudgetLevel()));

	ExportCategoryType.Territory terr = cat.getTerritory();

	if (DB.ok(terr.getMunicipality())) {
	    result.put(c.OKTMO_CODE.lc(), terr.getMunicipality().getCode());
	    result.put(c.SCOPE.lc(), 1);
	}

	if (DB.ok(terr.getRegion())) {
	    if (DB.eq(terr.getRegion().getCode(), "77")) {
		result.put(c.OKTMO_CODE.lc(), VocOktmo.CODE_MOSCOW);
	    } else {
		result.put(c.CODE_VC_NSI_237.lc(), terr.getRegion().getCode());
	    }
	    result.put(c.SCOPE.lc(), 0);
	}

	if (DB.ok(cat.getNsiCategory())) {
	    result.put(c.CODE_VC_NSI_154.lc(), cat.getNsiCategory().getCode());
	}



	List<ExportCategoryType.Actual> actual  = cat.getActual();

	List<Map<String, Object>> discounts = new ArrayList<>();

	if (DB.ok(actual)) {
	    for (ExportCategoryType.Actual t : actual) {
		Map<String, Object> d = CitizenCompensationCalculationKind.toHash(t);
		d.put(CitizenCompensationCalculationKind.c.CATEGORYGUID.lc(), cat.getCategoryGuid());
		discounts.add(d);
	    }
	}

	result.put("discounts", discounts);

	return result;
    }
}