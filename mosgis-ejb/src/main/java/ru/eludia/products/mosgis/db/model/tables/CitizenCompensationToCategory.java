package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocServiceType;
import ru.gosuslugi.dom.schema.integration.msp.CitizenCompensationCategoryType;
import ru.gosuslugi.dom.schema.integration.msp.ServiceType;

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

    public static List<Map<String, Object>> selectForExport(DB db, Object id) throws SQLException {

	final Model m = db.getModel();

	Map<UUID, Map<String, Object>> id2cat = new HashMap<>();

	db.forEach(m
	    .select(CitizenCompensationToCategory.class, "AS root", "*")
	    .toOne(CitizenCompensationCategory.class, "AS cat", "*").on()
	    .where(CitizenCompensationToCategory.c.UUID_CIT_COMP.lc(), id)
	    .and(EnTable.c.IS_DELETED, 0)
	    , (rs) -> {
		Map<String, Object> i = db.HASH(rs);
		i.put("services", new ArrayList());
		id2cat.put((UUID) i.get("uuid"), i);
	    }
	);

	db.forEach(m.select(CitizenCompensationToCategory.class, "AS root", "*")
	    .toOne(CitizenCompensationToCategoryService.class, "AS root2svc", "uuid").on("root.uuid = root2svc.uuid")
	    .toOne(VocServiceType.class, "AS svc", "*").on()
	    .where(CitizenCompensationToCategory.c.UUID_CIT_COMP.lc(), id)
	    .and(EnTable.c.IS_DELETED, 0)
	    ,
	     (rs) -> {
		Map<String, Object> i = db.HASH(rs);

		final Map<String, Object> cat = id2cat.get(i.get("uuid"));

		if (cat != null) {
		    ((List) cat.get("services")).add(i);
		}
	    }
	);

	return new ArrayList<Map<String, Object>>(id2cat.values());
    }

    public static CitizenCompensationCategoryType toCitizenCompensationCategory(Map<String, Object> r) {

	r.put("categoryguid", r.get("cat.categoryguid"));

	final CitizenCompensationCategoryType result = DB.to.javaBean(CitizenCompensationCategoryType.class, r);

	for (Map<String, Object> i: (List<Map<String, Object>>) r.get ("services")) {
	    result.getService().add(ServiceType.fromValue(DB.to.String(i.get("svc.id"))));
	}

	return result;
    }
}