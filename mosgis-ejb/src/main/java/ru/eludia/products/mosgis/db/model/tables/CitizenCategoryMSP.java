package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBudgetLevel;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class CitizenCategoryMSP extends EnTable {

    public static final String TABLE_NAME = "tb_cit_cat_msp";

    public enum c implements EnColEnum {

	UUID_ORG                     (VocOrganization.class, null, "Организация, которая создала данную запись"),

	CATEGORYNAME                 (Type.STRING,  500,    null, "Наименование отдельной категории граждан"),
	LABEL_UC                     (Type.STRING,          new Virt  ("UPPER(\"CATEGORYNAME\")"), "НАИМЕНОВАНИЕ"),

	CODE_VC_NSI_154              (Type.STRING,  20,     null, "Отдельные категории граждан, имеющие право на получение компенсаций расходов (НСИ 154)"),

	FROMDATE                     (Type.DATE, "Дата начала предоставления компенсаций расходов"),
	TODATE                       (Type.DATE, null, "Дата окончания предоставления компенсаций расходов"),
	BUDGETLEVEL                  (VocBudgetLevel.class, "Бюджет, за счет которого осуществляются выплаты. Возможные значения:"),

	// Territory - Территория предоставления компенсации расходов:

	CATEGORYGUID                 (Type.UUID,             null,  "Глобально-уникальный идентификатор элемента справочника"),
	ID_LOG                       (CitizenCategoryMSPLog.class, "Последнее событие редактирования"),

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

    public CitizenCategoryMSP () {

        super (TABLE_NAME, "Перечень отдельных категорий граждан, имеющих право на получение компенсации расходов");

        cols (c.class);

        key (c.UUID_ORG);
    }
}