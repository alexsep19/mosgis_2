package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class OrganizationMemberDocumentLog extends LogTable {

    public OrganizationMemberDocumentLog () {

        super ("tb_org_member_docs__log", "История редактирования членов товарищества, кооператива", OrganizationMemberDocument.class
            , EnTable.c.class
            , OrganizationMemberDocument.c.class
        );
    }
}