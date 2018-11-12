package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;

public class VoteInitiator extends EnTable {
    
    public enum c implements ColEnum {
        
        UUID (Type.UUID,              NEW_UUID, "Ключ"           ),
        ORG  (PropertyDocument.class, null,     "Организация"    ),
        IND  (VocPerson.class,        null,     "Физическое лицо"),
        
        IND_SURNAME        (STRING, 256, null, "Фамилия"),
        IND_FIRSTNAME      (STRING, 256, null, "Имя"),
        IND_PATRONYMIC     (STRING, 256, null, "Отчество"),
        IND_ISFEMALE       (BOOLEAN,     null, "Пол (1 - женский, 0 - мужской)"),
        IND_CODE_VC_NSI_95 (STRING, 20, null, "Код документа, ужостоверяющего личность (НСИ 95)"),
        
        INIT_TYPE (  );
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
    }
    
    public VoteInitiator () {
        
        super ("tb_vote_initiators", "Инициаторы собрания собственников");
        
        cols (Charter.c.class);
        
    }
    
}
