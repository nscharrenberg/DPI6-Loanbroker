package messaging;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.Properties;

public class MessageQueue {
    private Connection connection;
    private Session session;
    private Context jndiContext;

    private MessageProducer producer;
    private MessageConsumer consumer;

    // Destinations
    private Destination loanRequestDestination;
    private Destination loanReplyDestination;
    private Destination bankInterestRequestDestination;
    private Destination bankInterestReplyDestination;

    public MessageQueue() {
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");


    }


    /**
     * This method initiates the JMS connection for the Queue's.
     * It establishes an ActiveMQ connection and the corresponding Queue's.
     */
    private void openJMSConnection() {
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
        props.put(("queue." + QueueNames.loanRequest), QueueNames.loanRequest);
        props.put(("queue." + QueueNames.loanReply), QueueNames.loanReply);
        props.put(("queue." + QueueNames.bankInterestRequest), QueueNames.bankInterestRequest);
        props.put(("queue." + QueueNames.bankInterestReply), QueueNames.bankInterestReply);

        try {
            this.jndiContext = new InitialContext(props);
            ConnectionFactory factory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method produces a JMS message and sends it to the Queue.
     *
     * @param obj
     * @param destination
     * @param messageId
     * @return
     */
    public String produce(Serializable obj, Destination destination, String messageId) {
        MessageProducer producer;

        try {
            producer = session.createProducer(destination);
            Message msg = session.createObjectMessage(obj);

            if(messageId != null) {
                msg.setJMSCorrelationID(messageId);
            }

            producer.send(msg);
            return msg.getJMSMessageID();
        } catch (JMSException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method consumes a JMS message and handles it's request.
     *
     * @param destination
     * @param listener
     */
    public void consume(Destination destination, MessageListener listener) {
        MessageConsumer consumer;

        try {
            consumer = session.createConsumer(destination);
            connection.start();
            consumer.setMessageListener(listener);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
