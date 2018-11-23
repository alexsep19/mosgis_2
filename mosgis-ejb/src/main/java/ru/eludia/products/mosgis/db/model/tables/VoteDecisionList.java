package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class VoteDecisionList extends EnTable {
    
    public enum c implements EnColEnum {
        
        PROTOCOL_UUID  (VotingProtocol.class, "Протокол"),
        
        ID_LOG (VoteDecisionListLog.class, "Последнее событие редактирования"),
        
        QUESTIONNUMBER (NUMERIC, 4, null, "Номер вопроса"),
        QUESTIONNAME   (STRING, 3000, "Вопрос"),
        
        DECISIONTYPE_VC_NSI_63 (STRING, "Тип вопроса (НСИ 63)"),
        
        AGREE   (NUMERIC, 3, null, "Результат голосование \"За\""),
        AGAINST (NUMERIC, 3, null, "Результат голосования \"Против\""),
        ABSTENT (NUMERIC, 3, null, "Результат голосования \"Воздержался\""),
        TOTAL   (STRING, new Virt ("DECODE(\"AGREE\", NULL, 0, \"AGREE\")+DECODE(\"AGAINST\", NULL, 0, \"AGAINST\")+DECODE(\"ABSTENT\", NULL, 0, \"ABSTENT\")"), "Всего голосующих"),
        
        FORMINGFUND_VC_NSI_241 (STRING, null, "Выбранный способ формирования фонда (НСИ 241)"),
        MANAGEMENTTYPE_VC_NSI_25 (STRING, null, "Выбранный способ управления МКД (НСИ 25)"),
        
        VOTINGRESUME (STRING, 1, "Итоги голосования: DECISION_IS_(M)ADE - решение принято DECISION_IS_(N)OT_MADE - решение не принято")
        
        ;
        
        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
    }
    
    public VoteDecisionList () {
        
        super ("tb_vote_decision_lists", "Таблица повесток ОСС");
        
        cols (c.class);
        key  ("questionname", "questionname");
        
    }   
}
