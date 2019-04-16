package ru.eludia.products.mosgis.db.model.voc;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Num;

public class VocGisStatus extends Table {
    
    private static JsonArray jsonArray;
    private static JsonArray jsonArrayLite;
    
    private static final String TABLE_NAME = "vc_gis_status";
    
    public static final Num DEFAULT = new Num (VocGisStatus.i.PROJECT.getId ());
    
    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }       
    
    public static final void addLiteTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArrayLite);
    }
    
    static {
        
        JsonArrayBuilder builder = Json.createArrayBuilder ();
        JsonArrayBuilder builderLite = Json.createArrayBuilder ();
        
        for (i value: i.values ()) {
            
            builder.add (Json.createObjectBuilder ()
                .add ("id",    value.id)
                .add ("label", value.label)
            );
            
            builderLite.add (Json.createObjectBuilder ()
                .add ("id",    value.id)
                .add ("label", value == i.APPROVED ? "размещён" : value.label)
            );
            
        }
                    
        jsonArray = builder.build ();
        jsonArrayLite = builderLite.build ();
        
    }    

    public VocGisStatus () {
        
        super (TABLE_NAME, "Статусы процессов утверждения в ГИС ЖКХ (из hcs-house-management-types.xsd + собственные)");
        
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
        SIGNED               (41,  "Signed",          "подписан"),
        
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
        
        ANNUL                (110, "Annul",           "аннулирован"),
                
        PENDING_RQ_CANCEL    (112,  "_pending_rq_cancel", "ожидание отмены"),
        PENDING_RP_CANCEL    (113,  "_pending_rp_cancel", "ожидание подтверждения отмены"),
        FAILED_CANCEL        (114,  "_failed_cancel", "ошибка отмены"),
        CANCELLED            (120,  "C", "отменён"),
        
        PENDING_RQ_PUBLISHANDPROJECT (122, "_pending_rq_publishandproject", "ожидание размещения проекта"),
        PENDING_RP_PUBLISHANDPROJECT (123, "_pending_rp_publishandproject", "ожидание подтверждения размещения проекта"),
        FAILED_PUBLISHANDPROJECT     (124, "_failed_publishandproject", "ошибка размещения проекта"),

        PUBLISHEDANDPROJECT  (-20, "PublishedAndProject", "размещен проект"),
        
        PENDING_RQ_PLACE_PROGRAM_WORKS  (-22, "_pending_rq_place_program_works", "ожидание размещения видов работ программы"),
        PENDING_RP_PLACE_PROGRAM_WORKS  (-23, "_pending_rp_place_program_works", "ожидание подтверждения размещения видов работ программы"),
        
        PROGRAM_WORKS_PLACE_FINISHED    (-31, "_program_works_place_finished", "завершена отправка видов работ программы"),
        
        PENDING_RQ_DELETEPROJECT    (-32, "_pending_rq_deleteproject", "ожидание удаления проекта"),
        PENDING_RP_DELETEPROJECT    (-33, "_pending_rp_deleteproject", "ожидание подтверждения удаления проекта"),
        FAILED_DELETEPROJECT        (-34, "_failed_deleteproject", "ошибка удаления проекта"),
        
        PROJECT_DELETED             (-41, "_project_deleted", "удален проект"),

	PENDING_RQ_DELETE (-52, "_pending_rq_delete", "ожидание удаления"),
	PENDING_RP_DELETE (-53, "_pending_rp_delete", "ожидание подтверждения удаления"),
	FAILED_DELETE     (-54, "_failed_delete", "ошибка удаления"),

	DELETED           (-60, "Deleted", "удален"),

	PENDING_RQ_UNDELETE (-62, "_pending_rq_undelete", "ожидание восстановления"),
	PENDING_RP_UNDELETE (-63, "_pending_rp_undelete", "ожидание подтверждения восстановления"),
	FAILED_UNDELETE     (-64, "_failed_undelete", "ошибка восстановления"),
        ;

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
        
        public boolean isInProgress () {
            
            switch (Math.abs (id) % 10) {
                case 2:
                case 3:
                    return true;
                default:
                    return false;
            }
            
        }

        @Override
        public String toString () {
            return Byte.toString (id);
        }
        
        public static final Object [] progressStatus = Arrays.asList (VocGisStatus.i.values ()).stream ()
            .filter ((t) -> t.isInProgress ())
            .map ((t) -> t.getId ())
            .toArray ();        
        
        public static final String progressStatusString = Arrays
            .asList (progressStatus)
            .stream ()
            .map ((t) -> t.toString ())
            .collect (Collectors.joining (","));

    }

}