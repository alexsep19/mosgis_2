package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocPassportDocFields extends Table {

    public VocPassportDocFields () {
        
        super ("vc_passport_doc_fields", "Описания документов среди полей паспортов МКД/ЖД/...");
        
        pk    ("id",           Type.STRING, "Ключ (см. vc_pass_fields)");
        col   ("id_dt",        Type.STRING, "Ключ поля даты документа (см. vc_pass_fields)");
        col   ("id_no",        Type.STRING, "Ключ поля номера документа (см. vc_pass_fields)");
        col   ("label",        Type.STRING, null, "Краткое наименование (для списка выбора)");
        
        data  (i.class);

    }

    public enum i {

        ENERGY_CLASS        ("uuid_house",       20024, 20817, 20818, "Подтверждение класса энергоэффективности"), // Документы, подтверждающие класс энергетической эффективности
        COMPLIANCE          ("uuid_house",       20168, 20815, 20816, "Соответствие параметров проектной документации"), // Документы, подтверждающие соответствие параметров построенных, реконструированных домов проектной документации
        INVALID_CONDO       ("uuid_house",       20170, 13026, 14526, "Решение о признании дома аварийным"), // Документ, содержащий решение о признании многоквартирного дома аварийным
        INVALID_COTTAGE     ("uuid_house",       21819, 20138, 20139, "Решение о признании непригодным для проживания"), // Документ, содержащий решение о признании жилого дома непригодным для проживания
        INVALID_PREMISE_RES ("uuid_premise_res", 20820, 20128, 20129, "Решение о признании непригодным для проживания"), // Документ, содержащий решение о признании жилого помещения непригодным для проживания
        INVALID_LIVING_ROOM ("uuid_living_room", 20821, 20133, 20134, "Решение о признании комнаты непригодной для проживания"); // Документ, содержащий решение о признании комнаты непригодной для проживания
        
        String refName;
        String id;
        String id_dt;
        String id_no;
        String label;

        public String getRefName () {
            return refName;
        }

        public String getId () {
            return id;
        }

        public String getId_dt () {
            return id_dt;
        }

        public String getId_no () {
            return id_no;
        }

        public String getLabel () {
            return label;
        }

        private i (String refName, int id, int id_dt, int id_no, String label) {
            this.refName = refName;
            this.id = "" + id;
            this.id_dt = "" + id_dt;
            this.id_no = "" + id_no;
            this.label = label;
        }
        
        public static VocPassportDocFields.i forId (String id) {
            for (i i: values ()) if (i.id.equals (id)) return i;
            return null;
        }
                
    }
    
}