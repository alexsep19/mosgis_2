package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocGisStatus extends Table {

    public VocGisStatus () {
        
        super ("vc_gis_status", "Статусы процессов утверждения в ГИС ЖКХ (из hcs-house-management-types.xsd + собственные)");
        
        pk    ("id",           Type.INTEGER, "Ключ");        
        col   ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {

        PROJECT              (10,  "Project",         "проект"),
        
        MUTATING             (11,  "_mutating",       "изменение"),

        PENDING_RQ_PLACING   (12,  "_pending_rq_placing", "ожидание размещения"),
        PENDING_RP_PLACING   (13,  "_pending_rp_placing", "ожидание подтверждения размещения"),
        FAILED_PLACING       (14,  "_failed_placing", "ошибка размещения"),
        
        APPROVAL_PROCESS     (20,  "ApprovalProcess", "на утверждении"),

        PENDING_RQ_REFRESH   (22,  "_pending_rq_refresh", "ожидание обновления статуса"),
        PENDING_RP_REFRESH   (23,  "_pending_rp_refresh", "ожидание результата обновления статуса"),
        FAILED_REFRESH       (24,  "_failed_refresh", "ошибка обновления статуса"),

        REVIEWED             (30,  "Reviewed",        "рассмотрен"),
        
        PENDING_RQ_APPROVAL  (32,  "_pending_rq_approval", "ожидание утверждения"),
        PENDING_RP_APPROVAL  (33,  "_pending_rp_approval", "ожидание подтверждения утверждения"),

        FAILED_STATE         (34,  "_failed_state", "ошибка обмена"),
        
        APPROVED             (40,  "Approved",        "утвержден"),
        
        PENDING_RQ_EDIT      (42,  "_pending_rq_edit", "ожидание переразмещения"),
        PENDING_RP_EDIT      (43,  "_pending_rp_edit", "ожидание подтверждения переразмещения"),
        
        RUNNING              (50,  "Running",         "действующий"),
        
        PENDING_RQ_ROLLOVER  (52,  "_pending_rq_rollover", "ожидание пролонгации"),
        PENDING_RP_ROLLOVER  (53,  "_pending_rp_rollover", "ожидание подтверждения пролонгации"),
        
        EXPIRED              (60,  "Expired",         "истек срок действия"),
        
        PENDING_RQ_RELOAD    (62,  "_pending_rq_reload", "ожидание обновления данных"),
        PENDING_RP_RELOAD    (63,  "_pending_rp_reload", "ожидание результата обновления данных"),
        FAILED_RELOAD        (64,  "_failed_reload", "ошибка обновления данных"),
        
        LOCKED               (70,  "Locked",          "заблокирован"),
        NOT_RUNNING          (80,  "NotRunning",      "не действующий"),
        REJECTED             (90,  "Rejected",        "отклонен"),
        
        PENDING_RQ_TERMINATE (92,  "_pending_rq_terminate", "ожидание расторжения"),
        PENDING_RP_TERMINATE (93,  "_pending_rp_terminate", "ожидание подтверждения расторжения"),
        FAILED_TERMINATE     (94,  "_failed_terminate", "ошибка расторжения"),
        
        TERMINATED           (100, "Terminated",      "расторгнут/закрыт"),
        
        PENDING_RQ_ANNULMENT (102,  "_pending_rq_annulment", "ожидание аннулирования"),
        PENDING_RP_ANNULMENT (103,  "_pending_rp_annulment", "ожидание подтверждения аннулирования"),
        FAILED_ANNULMENT     (104,  "_failed_annulment", "ошибка аннулирования"),
        
        ANNUL                (110, "Annul",           "аннулирован");

        byte id;
        String name;
        String label;
        
        public ru.eludia.base.model.def.Num asDef () {
            return new ru.eludia.base.model.def.Num (id);
        }

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

        public static i forId (int id) {
            for (i i: values ()) if (i.id == id) return i;
            return null;
        }

        public static i forId (Object id) {
            return forId (Integer.parseInt (id.toString ()));
        }

    }

}