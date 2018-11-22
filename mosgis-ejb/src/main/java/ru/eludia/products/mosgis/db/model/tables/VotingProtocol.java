package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.SyncMap;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocVotingForm;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class VotingProtocol extends Table {
    
    public VotingProtocol () {
        
        super ("tb_voting_protocols", "Протоколы ОСС");
        
        pk  ("uuid", Type.UUID, NEW_UUID, "Ключ");
        
        fk  ("uuid_org",                  VocOrganization.class,                      "Организация");
        
        fk  ("fiashouseguid",           VocBuilding.class,                   "Глобальный уникальный идентификатор дома по ФИАС");
        col ("is_deleted",              Type.BOOLEAN, Bool.FALSE,            "1, если запись удалена; иначе 0");
        
        fk  ("id_prtcl_status",           VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (),   "Статус протокола с точки зрения mosgis");
        ref ("id_prtcl_status_gis",       VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (),   "Статус протокола с точки зрения ГИС ЖКХ");
        
        fk  ("form_", VocVotingForm.class, null, "Форма проведения");
        //col ("label_form", Type.STRING, new Virt("DECODE(\"FORM_\", 0, 'Заочное голосование (опросным путем)', 1, 'Очное голосование', 2, 'Заочное голосование с использованием системы', 3, 'Очно-заочное голосование', 'Неизвестная форма проведения')"), "Форма проведения");
        //col ("label_form_uc", Type.STRING, new Virt("UPPER(\"LABEL_FORM\")"), "ФОРМА ПРОВЕДЕНИЯ");
        
        col ("protocolnum", Type.STRING, 30, null, "Номер протокола");
        col ("protocoldate", Type.DATE, "Дата составления протокола");
        
        col ("label", Type.STRING, new Virt("'№' || protocolnum || ' от ' || TO_CHAR (protocoldate, 'DD.MM.YYYY')"), "№/датаы");
        
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
        
        fk  ("id_log",                VotingProtocolLog.class,    null,         "Последнее событие редактирования");
    }
    
    private final static String [] keyFields = {""};

    public class Sync extends SyncMap<NsiRef> {
        
        UUID uuid_org;

        public Sync (DB db, UUID uuid_org) {
            super (db);
            this.uuid_org = uuid_org;
            commonPart.put ("uuid_org", uuid_org);
            commonPart.put ("is_deleted", 0);
        }                

        @Override
        public String[] getKeyFields () {
            return keyFields;
        }

        @Override
        public void setFields (Map<String, Object> h, NsiRef o) {
            //h.put ("uniquenumber", o.getCode ());
        }

        @Override
        public Table getTable () {
            return VotingProtocol.this;
        }
        
    }
    
}