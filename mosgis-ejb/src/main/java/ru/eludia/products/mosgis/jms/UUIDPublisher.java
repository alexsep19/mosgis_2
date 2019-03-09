package ru.eludia.products.mosgis.jms;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.Session;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UUIDPublisher {
    
    @Resource (mappedName = "mosgis.ConnectionFactory")
    private ConnectionFactory connectionFactory;
    private Connection connection;
    
    private static Logger logger = Logger.getLogger (UUIDPublisher.class.getName ());
    
    private String getName (Destination destination) throws JMSException {
        if (destination instanceof Queue) return ((Queue) destination).getQueueName ();
        if (destination instanceof Topic) return ((Topic) destination).getTopicName ();
        throw new IllegalArgumentException ("Unknown destination type " + destination.getClass ().getName ());
    }
    
    @PostConstruct
    public void init () {
        try {
            connection = connectionFactory.createConnection ();
        }
        catch (JMSException ex) {
            throw new IllegalStateException (ex);
        }
    }
    
    @PreDestroy 
    public void done () {
        try {
            connection.close ();
        }
        catch (JMSException ex) {
            throw new IllegalStateException (ex);
        }
    }
    
    public void publish (Destination destination, UUID uuid) {
        publish (destination, uuid.toString ());
    }
    
    public void publish (Destination destination, String s) {
        
        try {

            Session session = connection.createSession (false, Session.AUTO_ACKNOWLEDGE);

            try {
                MessageProducer messageProducer = session.createProducer (destination);
                Message message = session.createTextMessage (s);                                                              
                messageProducer.send (message);
                logger.log (Level.INFO, s + " -} " + getName (destination));
            }
            finally {
                try {
                    session.close();
                }
                catch (Exception e) {
                    logger.log (Level.SEVERE, "Can't close JMS session", e);
                }
            }

        }
        catch (JMSException ex) {
            throw new IllegalStateException (ex);
        }
                    
    }    

}