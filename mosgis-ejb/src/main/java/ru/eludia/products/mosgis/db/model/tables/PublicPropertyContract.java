package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.def.Bool.FALSE;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;


public class PublicPropertyContract extends EnTable {

    public enum c implements EnColEnum {

        UUID_ORG             (VocOrganization.class,     "Организация-исполнитель"),
        FIASHOUSEGUID        (VocBuilding.class,         "Дом"),
        UUID_ORG_CUSTOMER    (VocOrganization.class,     "Организация-заказчик"),
        UUID_PERSON_CUSTOMER (VocPerson.class,           "Физлицо-заказчик"),
        ID_CTR_STATUS        (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус устава с точки зрения mosgis"),
        ID_CTR_STATUS_GIS    (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус устава с точки зрения ГИС ЖКХ"),
        ID_CTR_STATE_GIS     (VocGisStatus.class,        VocGisStatus.i.NOT_RUNNING.asDef (), "Состояние устава с точки зрения ГИС ЖКХ"),

        ID_LOG               (PublicPropertyContractLog.class,  "Последнее событие редактирования"),
        
        CONTRACTNUMBER       (STRING, 255,    null,      "Номер договора"),
        DATE_                (DATE,                      "Дата договора"),
        STARTDATE            (DATE,                      "Дата начала действия договора"),
        ENDDATE              (DATE,                      "Планируемая дата окончания действия договора"),
        CONTRACTOBJECT       (STRING, 255,    null,      "Предмет договора"),
        COMMENTS             (STRING, 255,    null,      "Комментарий"),
        PAYMENT              (NUMERIC, 10, 2, null,      "Размер платы за предоставление в пользование части общего имущества собственников помещений в МКД в месяц"),
        MONEYSPENTDIRECTION  (STRING, 255,    null,      "Направление расходования средств, внесенных за пользование частью общего имущества"),
        ISGRATUITOUSBASIS    (BOOLEAN,        FALSE,     "1, если договор заключен на безвозмездной основе; иначе 0")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                case UUID_ORG:
                case UUID_ORG_CUSTOMER:
                case UUID_PERSON_CUSTOMER:
                case FIASHOUSEGUID:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public PublicPropertyContract () {
        
        super ("tb_pp_ctr", "Договор на пользование общим имуществом");

        cols   (c.class);
        
        key    ("uuid_org", c.UUID_ORG);

    }
    
}