package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.gosuslugi.dom.schema.integration.house_management.ImportPublicPropertyContractRequest;


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
        
        CONTRACTNUMBER       (Type.STRING, 255,    null,       "Номер договора"),
        DATE_                (Type.DATE,                       "Дата договора"),
        STARTDATE            (Type.DATE,                       "Дата начала действия договора"),
        ENDDATE              (Type.DATE,                       "Планируемая дата окончания действия договора"),
        CONTRACTOBJECT       (Type.STRING, 255,    null,       "Предмет договора"),
        COMMENTS             (Type.STRING, 255,    null,       "Комментарий"),
        PAYMENT              (Type.NUMERIC, 10, 2, null,       "Размер платы за предоставление в пользование части общего имущества собственников помещений в МКД в месяц"),
        MONEYSPENTDIRECTION  (Type.STRING, 255,    null,       "Направление расходования средств, внесенных за пользование частью общего имущества"),
        
        DDT_START            (Type.NUMERIC, 2,     null,       "Начало периода внесения платы по договору (1..31 — конкретное число; 99 — последнее число)"),
        DDT_START_NXT        (Type.BOOLEAN,        Bool.FALSE, "1, если начало периода внесения платы по договору в следующем месяце; иначе 0"),
        DDT_END              (Type.NUMERIC, 2,     null,       "Окончание периода внесения платы по договору (1..31 — конкретное число; 99 — последнее число)"),
        DDT_END_NXT          (Type.BOOLEAN,        Bool.FALSE, "1, если окончание периода внесения платы по договору в следующем месяце; иначе 0"),
        IS_OTHER             (Type.BOOLEAN,        Bool.FALSE, "1, если период внесеняи платы — \"иной\"; иначе 0"),
        OTHER                (Type.STRING,         null,       "Иное (период внесения платы)"),
        
        ISGRATUITOUSBASIS    (Type.BOOLEAN,        Bool.TRUE, "1, если договор заключен на безвозмездной основе; иначе 0")

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
    
    public static ImportPublicPropertyContractRequest toImportPublicPropertyContractRequest (Map<String, Object> r) {
        final ImportPublicPropertyContractRequest createImportPublicPropertyContractRequest = new ImportPublicPropertyContractRequest ();
        final ImportPublicPropertyContractRequest.Contract contract = new ImportPublicPropertyContractRequest.Contract ();
        final ImportPublicPropertyContractRequest.Contract.PublicPropertyContract publicPropertyContract = toContractPublicPropertyContract (r);
        contract.setPublicPropertyContract (publicPropertyContract);
        contract.setTransportGUID (UUID.randomUUID ().toString ());
        createImportPublicPropertyContractRequest.getContract ().add (contract);
        return createImportPublicPropertyContractRequest;
    }
    
    private static ImportPublicPropertyContractRequest.Contract.PublicPropertyContract toContractPublicPropertyContract (Map<String, Object> r) {
        ImportPublicPropertyContractRequest.Contract.PublicPropertyContract result = DB.to.javaBean (ImportPublicPropertyContractRequest.Contract.PublicPropertyContract.class, r);
        return result;
    }
    
}