package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPaymentBaseType;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi237;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi324;
import ru.gosuslugi.dom.schema.integration.payment.ImportSupplierNotificationsOfOrderExecutionRequest;
import ru.gosuslugi.dom.schema.integration.payment.ImportSupplierNotificationsOfOrderExecutionRequest.SupplierNotificationOfOrderExecution;

public class PaymentLog extends GisWsLogTable {

public PaymentLog () {

        super (Payment.TABLE_NAME + "__log", "История редактирования платежей", Payment.class
            , EnTable.c.class
            , Payment.c.class
        );

    }

    public static Map<String, Object> getForExport(DB db, String id) throws SQLException {

	return db.getMap(db.getModel()
	    .get(PaymentLog.class, id, "*")
	    .toOne(Payment.class, "AS r",
		 EnTable.c.UUID.lc(),
		 Payment.c.ID_CTR_STATUS.lc()
	    ).on()
	    .toOne(VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on("r.uuid_org=org.uuid")
            .toMaybeOne (Account.class, "AS acct"
                , Account.c.ACCOUNTNUMBER.lc ()
                , Account.c.ID_CTR_STATUS.lc ()
                , Account.c.SERVICEID.lc ()
            ).on ()
            .toMaybeOne (PaymentDocument.class, "AS pd"
                , PaymentDocument.c.PAYMENTDOCUMENTID.lc ()
                , PaymentDocument.c.ID_CTR_STATUS.lc ()
            ).on ()
	);
    }

    public static ImportSupplierNotificationsOfOrderExecutionRequest toImportSupplierNotificationsOfOrderExecutionRequest(Map<String, Object> r) {
	final ImportSupplierNotificationsOfOrderExecutionRequest result = new ImportSupplierNotificationsOfOrderExecutionRequest();

	SupplierNotificationOfOrderExecution s = DB.to.javaBean(SupplierNotificationOfOrderExecution.class, r);

	s.setTransportGUID(UUID.randomUUID().toString());

	s.setOrderPeriod(DB.to.javaBean(SupplierNotificationOfOrderExecution.OrderPeriod.class, r));

	switch (VocPaymentBaseType.i.forId(r.get(Payment.c.ID_TYPE.lc()))) {
	    case ACCOUNT:
		s.setServiceID(DB.to.String(r.get("acct.serviceid")));
		break;
	    case PAYMENT_DOCUMENT:
		s.setPaymentDocumentID(DB.to.String(r.get("pd.paymentdocumentid")));
		break;
	}
	result.getSupplierNotificationOfOrderExecution().add(s);

	return result;
    }
}