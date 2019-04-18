package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class CitizenCompensationToCategory extends EnTable {

    public static final String TABLE_NAME = "tb_cit_comp_to_cats";

    public enum c implements EnColEnum {

	UUID_CIT_COMP                (CitizenCompensation.class, "Гражданин, получающий компенсацию расходов"),
	UUID_CIT_COMP_CAT            (CitizenCompensationCategory.class, "Категория"),

	PERIODFROM                   (Type.DATE, "Дата начала предоставления компенсаций расходов"),
	PERIODTO                     (Type.DATE, null, "Дата окончания предоставления компенсаций расходов")
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {

            switch (this) {
                case UUID_CIT_COMP:
                    return false;
                default:
                    return true;
            }

        }

    }

    public CitizenCompensationToCategory () {

        super (TABLE_NAME, "Перечень отдельных категорий граждан, имеющих право на получение компенсации расходов");

        cols (c.class);

        key (c.UUID_CIT_COMP);
    }
}