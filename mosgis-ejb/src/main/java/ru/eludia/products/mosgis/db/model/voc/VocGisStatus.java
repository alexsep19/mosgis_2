package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocGisStatus extends Table {

    public VocGisStatus () {
        
        super ("vc_gis_status", "Статусы процессов утверждения в ГИС ЖКХ (из hcs-house-management-types.xsd)");
        
        pk    ("id",           Type.INTEGER, "Ключ");        
        col   ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {

        PROJECT          (10,  "Project",         "проект"),
        MUTATING         (11,  "_mutating",       "изменение"),
        
        REVIEWED         (20,  "Reviewed",        "рассмотрен"),
        APPROVAL_PROCESS (30,  "ApprovalProcess", "на утверждении"),
        APPROVED         (40,  "Approved",        "утвержден"),
        RUNNING          (50,  "Running",         "действующий"),
        EXPIRED          (60,  "Expired",         "истек срок действия"),
        LOCKED           (70,  "Locked",          "заблокирован"),
        NOT_RUNNING      (80,  "NotRunning",      "не действующий"),
        REJECTED         (90,  "Rejected",        "отклонен"),
        TERMINATED       (100, "Terminated",      "расторгнут/закрыт"),
        ANNUL            (110, "Annul",           "аннулирован");

        byte id;
        String name;
        String label;

        public byte getId () {
            return id;
        }

        public String getName () {
            return name;
        }

        public String getLabel () {
            return label;
        }

        private i (int id, String name, String label) {
            this.id = (byte) id;
            this.name = name;
            this.label = label;            
        }
        
        public static i forName (String name) {
            for (i i: values ()) if (i.name.equals (name)) return i;
            return null;
        }
        
    }
    
}