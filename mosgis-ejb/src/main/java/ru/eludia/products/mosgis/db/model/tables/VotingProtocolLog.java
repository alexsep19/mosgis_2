package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class VotingProtocolLog extends Table {
    
    public VotingProtocolLog () {
        
        super ("tb_voting_protocols__log", "История изменения протоколов ОСС");
        
        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        ref   ("action",                    VocAction.class,                            "Действие");
        fk    ("uuid_object",               VotingProtocol.class,                       "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
        
        col   ("is_deleted",                Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");
        fk    ("id_prtcl_status",           VocGisStatus.class,                 null,   "Статус протокола с точки зрения mosgis");
        fk    ("id_prtcl_status_gis",       VocGisStatus.class,                 null,   "Статус протокола с точки зрения ГИС ЖКХ");

        col ("protocolnum", Type.STRING, 30, null, "Номер протокола");
        col ("protocoldate", Type.DATE, "Дата составления протокола");
        
        col ("avotingdate", Type.DATE, null, "Дата окончания приема решений (заочное голосование опросным путем)");
        col ("resolutionplace", Type.STRING, 3000, null, "Место принятия решений (заочное голосование опросным путем)");
        
        col ("meetingdate", Type.DATETIME, null, "Дата и время проведения собрания (очное голосование)");
        col ("votingplace", Type.STRING, 3000, null, "Место проведения собрания (очное голосование)");
        
        col ("evotingdatebegin", Type.DATETIME, null, "Дата и время начала проведения голосования (заочное голосование с использованием системы)");
        col ("evotingdateend", Type.DATETIME, null, "Дата и время окончания проведения голосования (заочное голосование с использованием системы)");
        col ("discipline", Type.STRING, null, "Порядок приема оформленных в письменной форме решений собственников (заочное голосование с использованием системы)");
        col ("inforeview", Type.STRING, null, "Порядок ознакомления с информацией и (или) материалами, которые будут представлены на данном собрании (заочное голосование с использованием системы)");
        
        col ("meeting_av_date", Type.DATETIME, null, "Дата и время проведения собрания (очно-заочное голосование)");
        col ("meeting_av_place", Type.STRING, 3000, null, "Место проведения собрания (очно-заочное голосование)");
        col ("meeting_av_date_end", Type.DATE, null, "Дата окончания приема решений (очно-заочное голосование)");
        col ("meeting_av_res_place", Type.STRING, 3000, null, "Место приема решения (очно-заочное голосование)");
        
        col ("extravoting", Type.BOOLEAN, "Внеочередное собрание");
        col ("annualvoting", Type.BOOLEAN, new Virt("DECODE(\"EXTRAVOTING\",1,0,1)"), "Ежегодное собрание");
        col ("meetingeligibility", Type.STRING, 1, "Правомочность собрания. (C)OMPETENT - правомочно, (N)OT_COMPETENT - не правомочно");
        
        col ("modification", Type.STRING, null, "Основание изменения (для протоколов в статусе \"Размещен\")");
     
        trigger ("BEFORE INSERT", 
                "BEGIN "
                    + "SELECT "
                        + "is_deleted, "
                        + "id_prtcl_status, "
                        + "id_prtcl_status_gis, "
                        + "protocolnum, "
                        + "protocoldate, "
                        + "avotingdate, "
                        + "resolutionplace, "
                        + "meetingdate, "
                        + "votingplace, "
                        + "evotingdatebegin, "
                        + "evotingdateend, "
                        + "discipline, "
                        + "inforeview, "
                        + "meeting_av_date, "
                        + "meeting_av_place, "
                        + "meeting_av_date_end, "
                        + "meeting_av_res_place, "
                        + "extravoting, "
                        + "meetingeligibility, "
                        + "modification "
                    + "INTO "
                        + ":NEW.is_deleted, "
                        + ":NEW.id_prtcl_status, "
                        + ":NEW.id_prtcl_status_gis, "
                        + ":NEW.protocolnum, "
                        + ":NEW.protocoldate, "
                        + ":NEW.avotingdate, "
                        + ":NEW.resolutionplace, "
                        + ":NEW.meetingdate, "
                        + ":NEW.votingplace, "
                        + ":NEW.evotingdatebegin, "
                        + ":NEW.evotingdateend, "
                        + ":NEW.discipline, "
                        + ":NEW.inforeview, "
                        + ":NEW.meeting_av_date, "
                        + ":NEW.meeting_av_place, "
                        + ":NEW.meeting_av_date_end, "
                        + ":NEW.meeting_av_res_place, "
                        + ":NEW.extravoting, "
                        + ":NEW.meetingeligibility, "
                        + ":NEW.modification "
                    + "FROM "
                        + "tb_voting_protocols "
                    + "WHERE "
                        + "uuid=:NEW.uuid_object; "
                + "END;");
        
    }
    
}
