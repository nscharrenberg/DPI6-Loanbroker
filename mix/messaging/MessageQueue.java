package messaging;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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

    Map<String, Destination> destinations = new HashMap<>();

    public MessageQueue() {
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");
        openJMSConnection();
    }

    public Destination getLoanRequestDestination() {
        return loanRequestDestination;
    }

    public void setLoanRequestDestination(Destination loanRequestDestination) {
        this.loanRequestDestination = loanRequestDestination;
    }

    public Destination getLoanReplyDestination() {
        return loanReplyDestination;
    }

    public void setLoanReplyDestination(Destination loanReplyDestination) {
        this.loanReplyDestination = loanReplyDestination;
    }

    public Destination getBankInterestRequestDestination() {
        return bankInterestRequestDestination;
    }

    public void setBankInterestRequestDestination(Destination bankInterestRequestDestination) {
        this.bankInterestRequestDestination = bankInterestRequestDestination;
    }

    public Destination getBankInterestReplyDestination() {
        return bankInterestReplyDestination;
    }

    public void setBankInterestReplyDestination(Destination bankInterestReplyDestination) {
        this.bankInterestReplyDestination = bankInterestReplyDestination;
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

    public Destination createDestination(String destinationId) {
        try {
            return (Destination) jndiContext.lookup(destinationId);
        } catch (NamingException e) {
            e.printStackTrace();
            return null;
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
    public String produce(Serializable obj, String destination, String messageId) {
        MessageProducer producer;

        try {
            Destination sendDestination = createDestination(destination);
            this.destinations.put(destination, sendDestination);
            producer = session.createProducer(sendDestination);
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
    public void consume(String destination, MessageListener listener) {
        MessageConsumer consumer;

        try {
            Destination receiveDestination = createDestination(destination);
            this.destinations.put(destination, receiveDestination);
            consumer = session.createConsumer(receiveDestination);
            connection.start();
            consumer.setMessageListener(listener);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
