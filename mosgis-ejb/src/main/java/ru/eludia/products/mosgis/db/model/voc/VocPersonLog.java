package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class VocPersonLog extends LogTable {

    public VocPersonLog() {

	super("vc_persons__log", "История редактирования физических лиц", VocPerson.class,
	     EnTable.c.class,
	     VocPerson.c.class
	);

        col   ("label",              Type.STRING,     new Virt("DECODE(\"PATRONYMIC\", NULL, (\"SURNAME\" || ' ' || \"FIRSTNAME\"), (\"SURNAME\" || ' ' || \"FIRSTNAME\" || ' ' || \"PATRONYMIC\"))"), "ФИО");
        col   ("label_uc",           Type.STRING,     new Virt("UPPER(DECODE(\"PATRONYMIC\", NULL, (\"SURNAME\" || ' ' || \"FIRSTNAME\"), (\"SURNAME\" || ' ' || \"FIRSTNAME\" || ' ' || \"PATRONYMIC\")))"), "ФИО (в верхнем регистре)");
    }
}
