package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocContractDocType extends Table {

    public VocContractDocType () {
        
        super ("vc_contract_doc_types", "Типы документов-приложений к договорам");
        
        pk    ("id",           Type.INTEGER, 2, "Ключ");
        col   ("label",        Type.STRING, null, "Наименование");
        
        data  (i.class);

    }

    public enum i {
        
        AGREEMENT_ATTACHMENT           ( 1, "Дополнительное соглашение"),
        CHARTER                        ( 2, "Устав"),
        COMMISSIONING_PERMIT_AGREEMENT ( 3, "Разрешение на ввод в эксплуатацию"),
        CONTRACT                       ( 4, "Договор"),
        CONTRACT_ATTACHMENT            ( 5, "Приложение к договору"),
        PROTOCOL_BUILDING_OWNER        ( 6, "Решение органа управления застройщика"),
        PROTOCOL_MEETING_BOARD         ( 7, "Протокол заседания правления"),
        PROTOCOL_MEETING_OWNERS        ( 8, "Протокол собрания собственников"),
        PROTOCOL_OK                    ( 9, "Протокол открытого конкурса"),
        SIGNED_OWNERS                  (10, "Реестр собственников"),
        
        OTHER                          (99, "Прочие"); 
                
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