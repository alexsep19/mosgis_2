package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocLegalActLevel;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi237;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi324;

public class LegalAct extends AttachTable  {

    public static final String TABLE_NAME = "tb_legal_acts";

    public enum c implements EnColEnum {
	UUID_ORG               (VocOrganization.class, "Организация, которая завела данный НПА в БД"),

	CODE_VC_NSI_324        (Nsi324.class, "Вид НПА (НСИ 324)"),
	NAME                   (Type.STRING, 1000, null, "Наименование документа"),
	DOCNUMBER              (Type.STRING, 1000, null, "Номер документа"),

	LEVEL_                 (VocLegalActLevel.class, null, "Уровень (сфера действия)"),
	CODE_VC_NSI_237        (Nsi237.class, Nsi237.CODE_MOSCOW, "Сфера действия НПА региональный уровень (НСИ 237)"),
	SCOPE                  (Type.BOOLEAN, Bool.FALSE, "1, если действует в заданных ОКТМО, 0, если действует по всей Москве"),

	APPROVEDATE            (Type.DATE, null, "Дата принятия документа органом власти"),
	ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),

	ID_LOG                 (LegalActLog.class, "Последнее событие редактирования")
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

    public LegalAct () {
        super  (TABLE_NAME, "Нормативно-правовые акты (НПА)");
        cols   (c.class);
        key    (c.UUID_ORG);

	key    ("attachmentguid", AttachTable.c.ATTACHMENTGUID);

        trigger ("BEFORE UPDATE", "BEGIN "

            + CHECK_LEN

            + "IF :NEW.ID_CTR_STATUS = " + VocGisStatus.i.ANNUL + " THEN " + " :NEW.ID_STATUS := " + VocFileStatus.i.DELETED + "; END IF; "

        + "END;");
    }
}