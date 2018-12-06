package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class VocOrganizationProposalLog extends LogTable {

    public VocOrganizationProposalLog () {

        super ("vc_org_proposals__log", "История редактирования: создание обособленных подразделений и ФПИЮЛ"
            , VocOrganizationProposal.class
            , EnTable.c.class
            , VocOrganizationProposal.c.class
        );

    }

}