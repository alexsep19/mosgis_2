package ru.eludia.products.mosgis.db.model.tables;

import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Num.ZERO;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jms.Queue;

import ru.eludia.base.DB;
import ru.eludia.base.db.util.SyncMap;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocPlannedExaminationFileType;
import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;

public class PlannedExaminationFile extends EnTable {

	public enum c implements EnColEnum {

		LABEL          (Type.STRING, 1024,                   "Имя файла"),
        MIME           (Type.STRING,                  null,  "Тип содержимого"),
        LEN            (Type.INTEGER,                 null,  "Размер"),
        BODY           (Type.BLOB,              EMPTY_BLOB,  "Содержимое"),
        DESCRIPTION    (Type.STRING, 500,             null,  "Имя файла"),
        ATTACHMENTGUID (Type.UUID,                    null,  "Идентификатор сохраненного вложения"),
        ATTACHMENTHASH (Type.BINARY, 32,              null,  "ГОСТ Р 34.11-94"),
        ID_STATUS      (Type.INTEGER,                 ZERO,  "Статус"),
		
        ID_TYPE        (VocPlannedExaminationFileType.class, "Тип документа"),
		ID_LOG         (PlannedExaminationFileLog.class,     "Последнее событие редактирования"),
		UUID_PLANNED_EXAMINATION(PlannedExamination.class,   "Ссылка на плановую проверку"),;

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

		@Override
		public boolean isLoggable() {
			return false;
		}

	}

	public PlannedExaminationFile() {

		super("tb_pln_exm_files", "Файлы плановых проверок");

		cols(c.class);

		key("parent", c.UUID_PLANNED_EXAMINATION);
		key("attachmentguid", AttachTable.c.ATTACHMENTGUID);

		trigger("BEFORE UPDATE", "BEGIN "
				+ " IF :NEW.body IS NULL THEN :NEW.body := EMPTY_BLOB(); END IF; "
				+ " IF :NEW.len IS NULL THEN :NEW.len := -1; END IF; "
				+ " IF :NEW.id_status = 0 AND DBMS_LOB.GETLENGTH (:NEW.body) = :NEW.len THEN "
				+ "   :NEW.id_status := 1; "
				+ " END IF;"
		+ "END;");

	}

	public class Sync extends SyncMap<AttachmentType> {

		private static final long serialVersionUID = -3677460996283702620L;
		
		private RestGisFilesClient filesClient;
		
		private Queue queue;

		public Sync(DB db, UUID uuid_planned_examination, RestGisFilesClient mDB, Queue queue) {
			super(db);
			this.filesClient = mDB;
			this.queue = queue;
			commonPart.put(PlannedExaminationFile.c.UUID_PLANNED_EXAMINATION.lc(), uuid_planned_examination);
			commonPart.put(AttachTable.c.ID_STATUS.lc(), 1);
		}

		@Override
		public String[] getKeyFields() {
			return new String[] { AttachTable.c.ATTACHMENTGUID.lc() };
		}

		@Override
		public void setFields(Map<String, Object> h, AttachmentType a) {
			h.putAll(DB.HASH(
					AttachTable.c.LABEL, a.getName(),
					AttachTable.c.DESCRIPTION, a.getDescription(),
					AttachTable.c.ATTACHMENTGUID, a.getAttachment().getAttachmentGUID(),
					AttachTable.c.ATTACHMENTHASH, a.getAttachmentHASH().toUpperCase()
			));
		}

		@Override
		public Table getTable() {
			return PlannedExaminationFile.this;
		}

		@Override
		public void processCreated(List<Map<String, Object>> created) throws SQLException {

			created.forEach((h) -> {

				final UUID uuid = UUID.fromString(h.get("uuid").toString());

				logger.info("Scheduling download for " + uuid);

				filesClient.download(uuid, queue);

			});

		}

		@SuppressWarnings("rawtypes")
		@Override
		public void processUpdated(List<Map<String, Object>> updated) throws SQLException {

			updated.forEach((h) -> {

				Object hashAsIs = ((Map) h.get(ACTUAL)).get("attachmenthash");
				Object hashToBe = ((Map) h.get(WANTED)).get("attachmenthash");

				if (DB.eq(hashAsIs, hashToBe) && Long.parseLong(((Map) h.get(ACTUAL)).get("len").toString()) > 0)
					return;

				final UUID uuid = UUID.fromString(h.get("uuid").toString());

				logger.info("Scheduling download for " + uuid + ": " + hashToBe + " <> " + hashAsIs);

				filesClient.download(uuid, queue);

			});

		}

		@Override
		public void processDeleted(List<Map<String, Object>> deleted) throws SQLException {

			deleted.forEach((h) -> {
				Object u = h.get("uuid");
				h.clear();
				h.put("uuid", u);
				h.put(AttachTable.c.ID_STATUS.lc(), 2);
			});

			db.update(getTable(), deleted);

		}

		public void addAll(List<AttachmentType> src, VocPlannedExaminationFileType.i type) {
			for (AttachmentType a : src) {
				Map<String, Object> h = new HashMap<>();
				setFields(h, a);
				h.put(PlannedExaminationFile.c.ID_TYPE.lc(), type.getId());
				addRecord(h);
			}

		}

	}

}