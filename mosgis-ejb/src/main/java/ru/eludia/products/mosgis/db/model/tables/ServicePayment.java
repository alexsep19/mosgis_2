package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.NUMERIC;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.gosuslugi.dom.schema.integration.house_management.ContractPaymentsInfoType;

public class ServicePayment extends EnTable {

    public enum c implements EnColEnum {

        UUID_CONTRACT_PAYMENT (ContractPayment.class,   "Ссылка на [сведения о размере платы за] услуги управления"),
        UUID_ORG_WORK         (OrganizationWork.class,  "Ссылка на работу/услугу организации"),
        ID_LOG                (ServicePaymentLog.class,  "Последнее событие редактирования"),
        SERVICEPAYMENTSIZE    (NUMERIC, 14, 4, null,  "Размер платы (цены, тарифа) за работы (услуги)/Размер платы за работы (услуги), в целой части указываются рубли, в вещественной - до сотых долей копеек.")
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
                case UUID_CONTRACT_PAYMENT:
                case UUID_ORG_WORK:
                    return false;
                default: 
                    return true;
            }
        }

    }
    
    private static final Logger logger = Logger.getLogger (ServicePayment.class.getName ());

    public ServicePayment () {
        
        super ("tb_svc_payments", "Информация о размере платы (цене, тарифе) за содержание и текущий ремонт общего имущества в многоквартирном доме/Информация о размере платы за содержание жилого помещения, установленном по результатам открытого конкурса по отбору управляющей организации для управления многоквартирным домом");

        cols   (c.class);
        
        key    ("uuid_contract", c.UUID_CONTRACT_PAYMENT, c.UUID_ORG_WORK);

    }
    
    public static final ContractPaymentsInfoType.ServicePayment toServicePayment (Map <String, Object> r) {
        final ContractPaymentsInfoType.ServicePayment sp = new ContractPaymentsInfoType.ServicePayment ();
        sp.setServicePaymentSize ((BigDecimal) r.get (c.SERVICEPAYMENTSIZE.lc ()));
        sp.setService (NsiTable.toDom (r.get ("w.uniquenumber").toString (), (UUID) r.get ("w.elementguid")));
        return sp;
    }

}