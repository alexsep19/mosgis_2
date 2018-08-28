package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractType;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class MgmtContract extends View {

    public MgmtContract () {
        
        super  ("tb_mgmt_contracts", "Страховые компании");

        pk    ("uuid",                      Type.UUID,              "Ключ");
        col   ("is_deleted",                Type.BOOLEAN,                    "1, если запись удалена; иначе 0");

        fk    ("uuid_org",                  VocOrganization.class,                      "Договородержатель");
        fk    ("uuid_org_customer",       VocOrganization.class,                      "Контрагент");
        
        fk    ("id_customer_type",        VocGisCustomerType.class,                 "Тип контрагента");
        
        fk    ("id_status",                 VocGisStatus.class,           "Статус договора с точки зрения mosgis");
        fk    ("id_status_gis",             VocGisStatus.class,          "Статус договора с точки зрения ГИС ЖКХ");
        
        col   ("docnum",                    Type.STRING,                    "Номер договора");
        col   ("signingdate",               Type.DATE,                          "Дата заключения");
        col   ("effectivedate",             Type.DATE,                          "Дата вступления в силу");
        col   ("plandatecomptetion",        Type.DATE,                          "Планируемая дата окончания");

        col   ("automaticrolloveroneyear",  Type.BOOLEAN,            "1, если запись удалена; иначе 0");
 
        col   ("contractbase",              Type.STRING,  "Основание заключения договора");
        
    }

    @Override
    public final String getSQL () {

        return "SELECT " +
            "t.uuid" +
            ", t.is_deleted" +
            ", t.uuid_org" +
            ", t.uuid_org_customer" +
            ", t.id_customer_type" +
            ", t.id_status" +
            ", t.id_status_gis" +
            ", t.docnum" +
            ", t.signingdate" +
            ", t.effectivedate" +
            ", t.plandatecomptetion" +
            ", t.automaticrolloveroneyear" +
            ", t.code_vc_nsi_58 contractbase" +
            " FROM " + getName (Contract.class) + " t " + 
            " WHERE id_contract_type = " + VocGisContractType.i.MGMT.getId ();
        
    }

}