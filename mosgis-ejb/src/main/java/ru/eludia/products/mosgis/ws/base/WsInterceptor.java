/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.eludia.products.mosgis.ws.base;

import java.sql.SQLException;
import java.util.Map;
import javax.annotation.Resource;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.web.base.Errors;
import ru.eludia.products.mosgis.web.base.Fault;
import ru.mos.gkh.gis.schema.integration.base.BaseAsyncResponseType;
import ru.mos.gkh.gis.schema.integration.base.GetStateRequest;

/**
 *
 * @author Aleksei
 */
public class WsInterceptor {

    @Resource
    private WebServiceContext wsContext;

    @AroundInvoke
    public <T extends BaseAsyncResponseType> Object checkMessageStatus(InvocationContext context) throws Exception {

        Class<?> returnType = context.getMethod().getReturnType();
        if (!BaseAsyncResponseType.class.isAssignableFrom(returnType)) {
            return context.proceed();
        }
        T result = (T) returnType.newInstance();

        try (DB db = ModelHolder.getModel().getDb()) {
            MessageContext msgContext = wsContext.getMessageContext();
            
            Map sender = (Map)msgContext.get("sender");
            
            String requestMsgGuid = ((GetStateRequest) context.getParameters()[0]).getMessageGUID();
            Map<String, Object> wsMsg = db.getMap(db.getModel().select(WsMessages.class, "*")
                    .where(WsMessages.c.UUID,requestMsgGuid)
                    .and(WsMessages.c.UUID_SENDER, sender.get("uuid")));

            if (wsMsg == null || wsMsg.isEmpty())
                throw new Fault(Errors.INT002013);

            msgContext.put("msgId", wsMsg.get(WsMessages.c.UUID_MESSAGE.lc()));

            VocAsyncRequestState.i status = VocAsyncRequestState.i.forId(wsMsg.get(WsMessages.c.ID_STATUS.lc()));

            if (VocAsyncRequestState.i.DONE.equals(status)) {
                msgContext.put("response", wsMsg.get(WsMessages.c.RESPONSE.lc()));
                return context.proceed();
            } else {
                result.setMessageGUID(wsMsg.get(WsMessages.c.UUID.lc()).toString());
                result.setRequestState(status.getId());
                return result;
            }
        } catch (SQLException ex) {
            throw new Fault(ex.getMessage(), ex);
        }
    }
}
