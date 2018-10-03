package ru.eludia.products.mosgis.jms;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import ru.eludia.products.mosgis.jmx.NsiLocal;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jms.base.TextMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.nsiTopic")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
})
public class NsiMDB extends TextMDB {
    
    private static Logger logger = Logger.getLogger (NsiMDB.class.getName ());
    
    @EJB
    NsiLocal nsi;
    
    @EJB
    ModelHolder modelHolder;
    
    @Override
    protected void onTextMessage (TextMessage message) throws SQLException, JMSException {
        logger.log (Level.INFO, "Caught " + message.getJMSMessageID () + "...");               
        modelHolder.updateModel();
        logger.log (Level.INFO, "Done handling " + message.getJMSMessageID ());
    }    
}