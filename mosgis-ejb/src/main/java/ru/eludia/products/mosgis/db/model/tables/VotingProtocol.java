package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;

public class VotingProtocol extends Table {
    
    public VotingProtocol () {
        
        super ("tb_voting_protocols", "Протоколы ОСС");
        
        pk  ("uuid", Type.UUID, "Ключ");
        
        fk  ("fiashouseguid",           VocBuilding.class,                   "Глобальный уникальный идентификатор дома по ФИАС");
        
        col ("protocolnum", Type.STRING, 30, null, "Номер протокола");
        col ("protocoldate", Type.DATE, "Дата составления протокола");
        
        col ("avoitingdate", Type.DATE, null, "Дата окончания приема решений (заочное голосование опросным путем)");
        col ("resolutionplace", Type.STRING, 3000, null, "Место принятия решений (заочное голосование опросным путем)");
        //attachments?
        
        col ("meetingdate", Type.DATETIME, null, "Дата и время проведения собрания (очное голосование)");
        col ("votingplace", Type.STRING, 3000, null, "Место проведения собрания (очное голосование)");
        //attachments?
        
        col ("evotingdatebegin", Type.DATETIME, null, "Дата и время начала проведения голосования (заочное голосование с использованием системы)");
        col ("evotingdateend", Type.DATETIME, null, "Дата и время окончания проведения голосования (заочное голосование с использованием системы)");
        col ("discipline", Type.STRING, null, "Порядок приема оформленных в письменной форме решений собственников (заочное голосование с использованием системы)");
        col ("inforeview", Type.STRING, null, "Порядок ознакомления с информацией и (или) материалами, которые будут представлены на данном собрании (заочное голосование с использованием системы)");
        //attachments?
        
        col ("meeting_av_date", Type.DATETIME, null, "Дата и время проведения собрания (очно-заочное голосование)");
        col ("meeting_av_place", Type.STRING, 3000, null, "Место проведения собрания (очно-заочное голосование)");
        col ("meeting_av_date_end", Type.DATE, null, "Дата окончания приема решений (очно-заочное голосование)");
        col ("meeting_av_res_place", Type.STRING, 3000, null, "Место приема решения (очно-заочное голосование)");
        //attachments?
        
        //voteinitiators?
        col ("extravoting", Type.BOOLEAN, "Внеочередное собрание");
        col ("annualvoting", Type.BOOLEAN, new Virt("DECODE(\"EXTRAVOTING\",1,0,1)"), "Ежегодное собрание");
        col ("meetingeligibility", Type.STRING, 1, "Правомочность собрания. (C)OMPETENT - правомочно, (N)OT_COMPETENT - не правомочно");
        //decisionlist?
        
        col ("modification", Type.STRING, null, "Основание изменения (для протоколов в статусе \"Размещен\")");
    }
    
}