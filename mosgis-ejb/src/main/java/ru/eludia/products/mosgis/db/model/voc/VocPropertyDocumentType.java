package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocPropertyDocumentType extends Table {

    public VocPropertyDocumentType () {

        super ("vc_prop_doc_types", "Виды свидетельств прав собственности");

        pk    ("id",           Type.INTEGER, "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");

        data  (i.class);

    }

    public enum i {
        
        ACT    (1, "Акт"),
        EGRP   (2, "Выписка из ЕГРП"),
        CERT   (3, "Свидетельство о государственной регистрации права");
                
        int id;
        String label;

        public int getId () {
            return id;
        }        

        public String getLabel () {
            return label;
        }

        private i (int id, String label) {
            this.id = id;
            this.label = label;
        }
        
    }
        
}