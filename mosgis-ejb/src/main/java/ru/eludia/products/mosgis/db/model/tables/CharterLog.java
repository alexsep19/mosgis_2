package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
public class CharterLog extends Table {

    public CharterLog () {
        
        super ("tb_charters__log", "История изменения уставов");
        
        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        col   ("action",                    Type.STRING,                                "Действие");
        fk    ("uuid_object",               Charter.class,                              "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
        col   ("charterversionguid",        Type.UUID,                          null,   "Идентификатор версии договора");
        
        col   ("is_deleted",                Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");
        fk    ("id_ctr_status",             VocGisStatus.class,                 null,   "Статус договора с точки зрения mosgis");
        fk    ("id_ctr_status_gis",         VocGisStatus.class,                 null,   "Статус договора с точки зрения ГИС ЖКХ");        
        col   ("date_",                     Type.DATE,                          null,   "Дата регистрации TCН/ТСЖ/кооператива (Организации Поставщика данных)");
        col   ("automaticrolloveroneyear",  Type.BOOLEAN,                       null,   "Автоматически продлить срок оказания услуг на один год");
        col   ("nocharterapproveprotocol",  Type.BOOLEAN,                       null,   "Протокол, содержащий решение об утверждении устава, отсутствует");
        col   ("ddt_m_start",               Type.NUMERIC,          2,           null,   "Начало периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)");
        col   ("ddt_m_start_nxt",           Type.BOOLEAN,                       null,   "1, если начало периода ввода показаний ПУ в следующем месяце; иначе 0");
        col   ("ddt_m_end",                 Type.NUMERIC,          2,           null,   "Окончание периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)");
        col   ("ddt_m_end_nxt",             Type.BOOLEAN,                       null,   "1, если окончание периода ввода показаний ПУ в следующем месяце; иначе 0");
        col   ("ddt_d_start",               Type.NUMERIC,          2,           null,   "Срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)");
        col   ("ddt_d_start_nxt",           Type.BOOLEAN,                       null,   "1, если срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0");
        col   ("ddt_i_start",               Type.NUMERIC,          2,           null,   "Срок внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)");
        col   ("ddt_i_start_nxt",           Type.BOOLEAN,                       null,   "1, если срок внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0");
        col   ("reasonofannulment",         Type.STRING,         1000,          null,    "Причина аннулирования");

        col   ("versionnumber",             Type.INTEGER,          10, null,    "Номер версии (по состоянию в ГИС ЖКХ)");
        col   ("is_annuled",                Type.BOOLEAN,          new Virt ("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");

       trigger ("BEFORE INSERT", "BEGIN "

           + "SELECT"
           + "   is_deleted"
           + "  ,id_ctr_status"
           + "  ,id_ctr_status_gis"
           + "  ,date_"
           + "  ,automaticrolloveroneyear"
           + "  ,nocharterapproveprotocol"                              
           + "  ,ddt_m_start"
           + "  ,ddt_m_start_nxt"
           + "  ,ddt_m_end"
           + "  ,ddt_m_end_nxt"
           + "  ,ddt_d_start"
           + "  ,ddt_d_start_nxt"
           + "  ,ddt_i_start"
           + "  ,ddt_i_start_nxt"
           + "  ,reasonofannulment"
           + " INTO "
           + "   :NEW.is_deleted"
           + "  ,:NEW.id_ctr_status"
           + "  ,:NEW.id_ctr_status_gis"
           + "  ,:NEW.date_"
           + "  ,:NEW.automaticrolloveroneyear"
           + "  ,:NEW.nocharterapproveprotocol"                              
           + "  ,:NEW.ddt_m_start"
           + "  ,:NEW.ddt_m_start_nxt"
           + "  ,:NEW.ddt_m_end"
           + "  ,:NEW.ddt_m_end_nxt"
           + "  ,:NEW.ddt_d_start"
           + "  ,:NEW.ddt_d_start_nxt"
           + "  ,:NEW.ddt_i_start"
           + "  ,:NEW.ddt_i_start_nxt"
           + "  ,:NEW.reasonofannulment"
           + " FROM tb_charters WHERE uuid=:NEW.uuid_object; "

       + "END;");
       
    }
    
}