package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Num;
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
        fk    ("uuid_org_customer",         VocOrganization.class,                      "Контрагент");
        
        fk    ("id_customer_type",          VocGisCustomerType.class,                 "Тип контрагента");
        
        fk    ("id_ctr_status",             VocGisStatus.class,          "Статус договора с точки зрения mosgis");
        fk    ("id_ctr_status_gis",         VocGisStatus.class,          "Статус договора с точки зрения ГИС ЖКХ");
        fk    ("id_ctr_state",              VocGisStatus.class,          "Состояние договора с точки зрения mosgis");
        fk    ("id_ctr_state_gis",          VocGisStatus.class,          "Состояние договора с точки зрения ГИС ЖКХ");
        
        col   ("docnum",                    Type.STRING,                    "Номер договора");
        col   ("signingdate",               Type.DATE,                          "Дата заключения");
        col   ("effectivedate",             Type.DATE,                          "Дата вступления в силу");
        col   ("plandatecomptetion",        Type.DATE,                          "Планируемая дата окончания");

        col   ("automaticrolloveroneyear",  Type.BOOLEAN,            "1, если запись удалена; иначе 0");
 
        col   ("contractbase",              Type.STRING,  "Основание заключения договора");
        col   ("code_vc_nsi_58",            Type.STRING,           20,   null,  "Ссылка на НСИ \"Основание заключения договора\" (реестровый номер 58)");

        col   ("ddt_m_start",               Type.NUMERIC,          2,   null,  "Начало периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)");
        col   ("ddt_m_start_nxt",           Type.BOOLEAN,          null, "1, если начало периода ввода показаний ПУ в следующем месяце; иначе 0");
        col   ("ddt_m_end",                 Type.NUMERIC,          2,   null,  "Окончание периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)");
        col   ("ddt_m_end_nxt",             Type.BOOLEAN,          null, "1, если окончание периода ввода показаний ПУ в следующем месяце; иначе 0");
        col   ("ddt_d_start",               Type.NUMERIC,          2,   null,  "Срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)");
        col   ("ddt_d_start_nxt",           Type.BOOLEAN,          null, "1, если срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0");
        col   ("ddt_i_start",               Type.NUMERIC,          2,   null,  "Срок внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)");
        col   ("ddt_i_start_nxt",           Type.BOOLEAN,          null, "1, если срок внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0");

        fk    ("uuid_out_soap",             OutSoap.class,             null,    "Последний запрос на импорт в ГИС ЖКХ");
        col   ("contractguid",              Type.UUID,                 null,    "UUID договора в ГИС ЖКХ");

        fk    ("id_log",                    ContractLog.class,         null, "Последнее событие редактирования");
        
    }

    @Override
    public final String getSQL () {

        return "SELECT " +
            "t.uuid" +
            ", t.is_deleted" +
            ", t.uuid_org" +
            ", t.uuid_org_customer" +
            ", t.id_customer_type" +
            ", t.id_ctr_status" +
            ", t.id_ctr_status_gis" +
            ", t.docnum" +
            ", t.signingdate" +
            ", t.effectivedate" +
            ", t.plandatecomptetion" +
            ", t.automaticrolloveroneyear" +
            ", t.code_vc_nsi_58 contractbase" +
            ", t.code_vc_nsi_58" +
            ", t.contractguid"+
            ", t.ddt_d_start"+
            ", t.ddt_d_start_nxt"+
            ", t.ddt_i_start"+
            ", t.ddt_i_start_nxt"+
            ", t.ddt_m_end"+
            ", t.ddt_m_end_nxt"+
            ", t.ddt_m_start"+
            ", t.ddt_m_start_nxt"+
            ", t.id_contract_type"+
            ", t.id_ctr_state_gis"+
            ", t.id_log"+
            ", t.uuid_out_soap" +
            ", CASE" +
            "    WHEN ID_CTR_STATUS IN (100, 110) THEN 80 " +
            "    WHEN UUID_OUT_SOAP IS NULL THEN 80 " +
            "    WHEN PLANDATECOMPTETION < TRUNC (SYSDATE) THEN 60" +
            "    ELSE 50" +
            "  END id_ctr_state"+
            " FROM " + getName (Contract.class) + " t " + 
            " WHERE id_contract_type = " + VocGisContractType.i.MGMT.getId ();
        
    }

}