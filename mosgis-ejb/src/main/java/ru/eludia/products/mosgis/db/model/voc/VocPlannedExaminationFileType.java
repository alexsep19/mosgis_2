package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class VocPlannedExaminationFileType extends Table {

	private static final String TABLE_NAME = "vc_pln_exm_file_types";

	public VocPlannedExaminationFileType() {
		super(TABLE_NAME, "Типы документов по плановым проврекам");
		cols(c.class);
		pk(c.ID);
		data(i.class);
	}

	public enum c implements ColEnum {

		ID   (Type.NUMERIC, 1, "Идентификатор"), 
		LABEL(Type.STRING,     "Наименование");

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

	public enum i {

		CHANGE(1, "Документы по изменению"), 
		CANCEL(2, "Документы по отмене");

		int id;
		String label;

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

		public static i forId(Object id) {
			for (i i : values())
				if (DB.eq(i.id, id))
					return i;
			throw new IllegalArgumentException("Unknown file type id: " + id);
		}

	}
}