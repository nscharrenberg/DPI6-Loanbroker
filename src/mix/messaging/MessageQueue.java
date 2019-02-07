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

    /**
     * This method produces a JMS message and sends it to the Queue.
     * @param obj
     */
    public void produce(Serializable obj) {
        try {
            if(connection == null) {
                openJMSConnection();
            }

            if(obj instanceof LoanRequest) {
                producer = session.createProducer(loanRequestDestination);
            } else if (obj instanceof LoanReply) {
                producer = session.createProducer(loanReplyDestination);
            }else if (obj instanceof BankInterestRequest) {
                producer = session.createProducer(bankInterestRequestDestination);
            } else if (obj instanceof BankInterestReply) {
                producer = session.createProducer(bankInterestReplyDestination);
            }

            ObjectMessage msg = session.createObjectMessage(obj);
            producer.send(msg);
        } catch (JMSException e) {
            e.printStackTrace();
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
}
