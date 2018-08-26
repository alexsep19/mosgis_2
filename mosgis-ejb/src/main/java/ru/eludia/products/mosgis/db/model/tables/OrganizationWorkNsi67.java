package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class OrganizationWorkNsi67 extends Table {

    public OrganizationWorkNsi67 () {
        
        super ("tb_org_works_nsi_67", "Работы и услуги организации — Обязательные работы, обеспечивающие надлежащее содержание МКД");
        
        pkref ("uuid",       OrganizationWork.class,  "Работа / услуга");
        pk    ("code",       Type.STRING,  20,        "Ссылка на НСИ \"Обязательные работы, обеспечивающие надлежащее содержание МКД\" (реестровый номер 67)");

    }

}