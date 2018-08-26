package ru.eludia.products.mosgis.jms.base;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public abstract class TextMDB implements MessageListener {
    
    protected Logger logger = java.util.logging.Logger.getLogger (this.getClass ().getName ());
        
    protected abstract void onTextMessage (TextMessage message) throws SQLException, JMSException;

    @Override
    public void onMessage (Message message) {
        
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
