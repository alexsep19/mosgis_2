package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPaymentBaseType;
import ru.gosuslugi.dom.schema.integration.payment.ImportSupplierNotificationsOfOrderExecutionRequest;
import ru.gosuslugi.dom.schema.integration.payment.ImportSupplierNotificationsOfOrderExecutionRequest.SupplierNotificationOfOrderExecution;
import ru.gosuslugi.dom.schema.integration.payment.ImportNotificationsOfOrderExecutionCancellationRequest;
import ru.gosuslugi.dom.schema.integration.payments_base.NotificationOfOrderExecutionCancellationType;

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

	result.getSupplierNotificationOfOrderExecution().add(toSupplierNotificationOfOrderExecution (r));

	return result;
    }

    public static ImportNotificationsOfOrderExecutionCancellationRequest toNotificationsOfOrderExecutionCancellationRequest(Map<String, Object> r) {
	final ImportNotificationsOfOrderExecutionCancellationRequest result = new ImportNotificationsOfOrderExecutionCancellationRequest();

	result.getNotificationOfOrderExecutionCancellation().add(toNotificationOfOrderExecutionCancellation(r));

	return result;
    }

    private static NotificationOfOrderExecutionCancellationType toNotificationOfOrderExecutionCancellation(Map<String, Object> r) {

	r.put("comment", r.get(Payment.c.CANCELLATIONCOMMENT.lc()));

	final NotificationOfOrderExecutionCancellationType result = DB.to.javaBean(NotificationOfOrderExecutionCancellationType.class, r);

	result.setTransportGUID(UUID.randomUUID().toString());

	return result;
    }

    public static SupplierNotificationOfOrderExecution toSupplierNotificationOfOrderExecution (Map<String, Object> r) {

	SupplierNotificationOfOrderExecution result = DB.to.javaBean(SupplierNotificationOfOrderExecution.class, r);

	result.setTransportGUID(UUID.randomUUID().toString());

	result.setOrderPeriod(DB.to.javaBean(SupplierNotificationOfOrderExecution.OrderPeriod.class, r));

	switch (VocPaymentBaseType.i.forId(r.get(Payment.c.ID_TYPE.lc()))) {
	case ACCOUNT:
	    result.setServiceID(DB.to.String(r.get("acct.serviceid")));
	    break;
	case PAYMENT_DOCUMENT:
	    result.setPaymentDocumentID(DB.to.String(r.get("pd.paymentdocumentid")));
	    break;
	}

	return result;
    }
}