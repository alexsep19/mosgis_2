package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractType;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import static ru.eludia.products.mosgis.db.model.voc.VocGisStatus.i.ANNUL;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.house_management.ContractType;
import ru.gosuslugi.dom.schema.integration.house_management.DateDetailsType;
import ru.gosuslugi.dom.schema.integration.house_management.DeviceMeteringsDaySelectionType;    

public class Charter extends Table {

    public Charter () {

        super ("tb_charters", "Уставы");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        col   ("is_deleted",                Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");
        fk    ("uuid_org",                  VocOrganization.class,                      "Организация");
        
        fk    ("id_ctr_status",             VocGisStatus.class,          new Num (VocGisStatus.i.PROJECT.getId ()), "Статус устава с точки зрения mosgis");
        fk    ("id_ctr_status_gis",         VocGisStatus.class,          new Num (VocGisStatus.i.PROJECT.getId ()), "Статус устава с точки зрения ГИС ЖКХ");
        fk    ("id_ctr_state_gis",          VocGisStatus.class,          new Num (VocGisStatus.i.NOT_RUNNING.getId ()), "Состояние устава с точки зрения ГИС ЖКХ");
        
        col   ("date_",                     Type.DATE,             null,        "Дата регистрации TCН/ТСЖ/кооператива (Организации Поставщика данных)");
        col   ("automaticrolloveroneyear",  Type.BOOLEAN,          Bool.FALSE,  "Автоматически продлить срок оказания услуг на один год");
        
    //  DateDetailsType:

        //    PeriodMetering
        col   ("ddt_m_start",               Type.NUMERIC,          2,   null,  "Начало периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)");
        col   ("ddt_m_start_nxt",           Type.BOOLEAN,          Bool.FALSE, "1, если начало периода ввода показаний ПУ в следующем месяце; иначе 0");
        col   ("ddt_m_end",                 Type.NUMERIC,          2,   null,  "Окончание периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)");
        col   ("ddt_m_end_nxt",             Type.BOOLEAN,          Bool.FALSE, "1, если окончание периода ввода показаний ПУ в следующем месяце; иначе 0");

        //    PaymentDocumentInterval
        col   ("ddt_d_start",               Type.NUMERIC,          2,   null,  "Срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)");
        col   ("ddt_d_start_nxt",           Type.BOOLEAN,          Bool.FALSE, "1, если срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0");

        //    PaymentInterval
        col   ("ddt_i_start",               Type.NUMERIC,          2,   null,  "Срок внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)");
        col   ("ddt_i_start_nxt",           Type.BOOLEAN,          Bool.FALSE, "1, если срок внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0");
        
        
        fk    ("uuid_out_soap",             OutSoap.class,             null,    "Последний запрос на импорт в ГИС ЖКХ");
        col   ("charterguid",               Type.UUID,                 null,    "UUID устава в ГИС ЖКХ");
        col   ("charterversionguid",        Type.UUID,                 null,    "Идентификатор последней известной версии устава");

        fk    ("id_log",                    CharterLog.class,          null,    "Последнее событие редактирования");
        
        col   ("versionnumber",             Type.INTEGER,          10, null,    "Номер версии (по состоянию в ГИС ЖКХ)");

        col   ("reasonofannulment",         Type.STRING,         1000, null,    "Причина аннулирования");
        col   ("is_annuled",                Type.BOOLEAN,          new Virt ("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");

        key   ("charterguid", "charterguid");
        
    }

}