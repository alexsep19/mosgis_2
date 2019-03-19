package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class VocPlannedExaminationStatus extends Table {

	private static JsonArray jsonArray;

	private static final String TABLE_NAME = "vc_pln_exm_status";

	public static final void addTo(JsonObjectBuilder job) {
		job.add(TABLE_NAME, jsonArray);
	}

	static {
		JsonArrayBuilder builder = Json.createArrayBuilder();

		for (VocPlannedExaminationStatus.i value : VocPlannedExaminationStatus.i.values())
			builder.add(Json.createObjectBuilder().add("id", value.id).add("label", value.label));

		jsonArray = builder.build();
	}

	public VocPlannedExaminationStatus() {
		super(TABLE_NAME, "Статусы плановых проверок");
		cols(c.class);
		pk(c.ID);
		data(i.class);
	}

	public enum c implements ColEnum {
		ID    (Type.NUMERIC, 2, "Идентификатор"), 
		LABEL (Type.STRING,     "Наименование");
		
		@Override
		public Col getCol() {
			return col;
		}

		private Col col;

		private c(Type type, Object... p) {
			col = new Col(this, type, p);
		}
	}

	public enum i {

		PLANNED(10, "Запланировна"), 
		CANCELLED(20, "Отменена"), 
		FINISHED(30, "Завершена"), 
		ANNULLED(40, "Аннулирована");

		private int id;
		private String label;

		public int getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		private i(int id, String label) {
			this.id = id;
			this.label = label;
		}

		public static VocPlannedExaminationStatus.i forId(int id) {
			for (VocPlannedExaminationStatus.i i : values())
				if (i.id == id)
					return i;
			return null;
		}

		public static VocPlannedExaminationStatus.i forId(Object id) {
			return forId(Integer.parseInt(id.toString()));
		}

	}

}
