package ru.eludia.products.mosgis.db.model.incoming;

import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class InInspectionPlans extends Table {

	enum c implements ColEnum {
		UUID      (Type.UUID,      NEW_UUID, "Ключ"), 
		TS        (Type.TIMESTAMP, NOW,      "Дата/время записи в БД"),
		UUID_USER (VocUser.class,            "Оператор"), 
		YEAR_FROM (Type.NUMERIC,   4, null,  "Год с"),
		YEAR_TO   (Type.NUMERIC,   4, null,  "Год по");

		@Override
		public Col getCol() {
			return col;
		}

		private Col col;

		private c(Type type, Object... p) {
			col = new Col(this, type, p);
		}

		private c(Class<?> c, Object... p) {
			col = new Ref(this, c, p);
		}
	}

	public InInspectionPlans() {

		super("in_inspection_plans", "Запросы на прием планов провреок из ГИС ЖКХ");

		cols(InInspectionPlans.c.class);

		pk(c.UUID);

	}

}