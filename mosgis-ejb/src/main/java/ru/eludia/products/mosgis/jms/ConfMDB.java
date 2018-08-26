package ru.eludia.products.mosgis.jms;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.eludia.products.mosgis.jms.base.TextMDB;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import ru.eludia.products.mosgis.jmx.ConfLocal;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.confTopic")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
})
public class ConfMDB extends TextMDB {
    
    private static Logger logger = Logger.getLogger (ConfMDB.class.getName ());

    @EJB
    ConfLocal conf;

    @Override
    protected void onTextMessage (TextMessage message) throws SQLException, JMSException {
        logger.log (Level.INFO, "Caught " + message.getJMSMessageID () + "...");                
        conf.reload ();        
        logger.log (Level.INFO, "Done handling " + message.getJMSMessageID ());
    }    

}