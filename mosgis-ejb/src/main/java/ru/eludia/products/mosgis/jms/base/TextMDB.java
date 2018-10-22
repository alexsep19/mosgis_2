package ru.eludia.products.mosgis.jms.base;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public abstract class TextMDB implements MessageListener {
    
    protected Logger logger = java.util.logging.Logger.getLogger (this.getClass ().getName ());
        
    protected abstract void onTextMessage (TextMessage message) throws SQLException, JMSException;
    
    protected Destination ownDestination = null;

    protected Destination getOwnDestination () {
        return ownDestination;
    }
    
    protected Queue getOwnQueue () {
        return (Queue) ownDestination;
    }    

    @Override
    public void onMessage (Message message) {
        
        try {
            ownDestination = message.getJMSDestination ();
            if (ownDestination == null) throw new IllegalArgumentException ("getJMSDestination () returned null for " + message);
        }
        catch (JMSException ex) {
            logger.log (Level.SEVERE, "Can't get own destination", ex);
            return;
        }
        
        if (message instanceof TextMessage)
            try {
                onTextMessage ((TextMessage) message);
            }
            catch (SQLException | JMSException ex) {
                logger.log (Level.SEVERE, null, ex);
            }
        else
            logger.log (Level.SEVERE, "Wrong message class: " + message.getClass ().getName ());
        
    }
    
}
