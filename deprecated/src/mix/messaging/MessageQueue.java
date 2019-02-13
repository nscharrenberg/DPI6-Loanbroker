package mix.messaging;

import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.Properties;

public class MessageQueue {
    private static MessageQueue instance = new MessageQueue();

    private Connection connection;
    private Session session;

    private MessageProducer producer;
    private MessageConsumer consumer;

    // Destinations
    private Destination loanRequestDestination;
    private Destination loanReplyDestination;
    private Destination bankInterestRequestDestination;
    private Destination bankInterestReplyDestination;

    public static String loanRequest =  "loanRequestQueue";
    public static String loanReply = "loanReplyQueue";
    public static String bankInterestRequest = "loanBankInterestRequestQueue";
    public static String bankInterestReply = "bankInterestReplyQueue";

    public MessageQueue() {
        this.connection = null;
        this.session = null;
        this.producer = null;
        this.consumer = null;
        this.loanRequestDestination = null;
        this.loanReplyDestination = null;
        this.bankInterestRequestDestination = null;
        this.bankInterestReplyDestination = null;
    }

    public static MessageQueue getInstance() {
        return instance;
    }

    /**
     * This method produces a JMS message and sends it to the Queue.
     * @param obj
     * @param messageID
     */
    public String produce(Serializable obj, Destination destination, String messageID) {
        try {
            if(connection == null) {
                openJMSConnection();
            }

            producer = session.createProducer(destination);

            ObjectMessage msg = session.createObjectMessage(obj);

            if(messageID != null) {
                msg.setJMSCorrelationID(messageID);
            }

            producer.send(msg);
            return msg.getJMSMessageID();
        } catch (JMSException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method consumes a JMS message and handles it's request.
     *
     * @param queue
     * @param listener
     */
    public void consume(String queue, MessageListener listener) {
        try {
            if(connection == null) {
                openJMSConnection();
            }

            if(queue.equals(loanRequest)) {
                consumer = session.createConsumer(loanRequestDestination);
            } else if(queue.equals(loanReply)) {
                consumer = session.createConsumer(loanReplyDestination);
            } else if (queue.equals(bankInterestRequest)) {
                consumer = session.createConsumer(bankInterestRequestDestination);
            } else if (queue.equals(bankInterestReply)) {
                System.out.println("Consumer here!:");
                consumer = session.createConsumer(bankInterestReplyDestination);
            }

            consumer.setMessageListener(listener);
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method initiates the JMS connection for the Queue's.
     * It establishes an ActiveMQ connection and the corresponding Queue's.
     */
    private void openJMSConnection() {
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            props.put(("queue." + loanRequest), loanRequest);
            props.put(("queue." + loanReply), loanReply);
            props.put(("queue." + bankInterestRequest), bankInterestRequest);
            props.put(("queue." + bankInterestReply), bankInterestReply);

            Context jndiContext = new InitialContext(props);

            ActiveMQConnectionFactory factory = (ActiveMQConnectionFactory) jndiContext.lookup("ConnectionFactory");
            factory.setTrustAllPackages(true);
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            loanRequestDestination = (Destination) jndiContext.lookup(loanRequest);
            loanReplyDestination = (Destination) jndiContext.lookup(loanReply);
            bankInterestRequestDestination = (Destination) jndiContext.lookup(bankInterestRequest);
            bankInterestReplyDestination = (Destination) jndiContext.lookup(bankInterestReply);
        } catch (JMSException | NamingException e) {
            e.printStackTrace();
        }
    }

    public Destination getLoanRequestDestination() {
        return loanRequestDestination;
    }

    public Destination getLoanReplyDestination() {
        return loanReplyDestination;
    }

    public Destination getBankInterestRequestDestination() {
        return bankInterestRequestDestination;
    }

    public Destination getBankInterestReplyDestination() {
        return bankInterestReplyDestination;
    }

    public static String getLoanRequest() {
        return loanRequest;
    }

    public static String getLoanReply() {
        return loanReply;
    }

    public static String getBankInterestRequest() {
        return bankInterestRequest;
    }

    public static String getBankInterestReply() {
        return bankInterestReply;
    }

    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
    }

    public MessageProducer getProducer() {
        return producer;
    }

    public MessageConsumer getConsumer() {
        return consumer;
    }
}
