package mix.messaging;

import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.Serializable;

public class MessageQueue {
    private Connection connection;
    private Session session;

    private MessageProducer producer;
    private MessageConsumer consumer;

    // Queues
    private Queue loanRequestQueue;
    private Queue loanReplyQueue;
    private Queue bankInterestRequestQueue;
    private Queue bankInterestReplyQueue;

    public static String loanRequest =  "loanRequestQueue";
    public static String loanReply = "loanReplyQueue";
    public static String bankInterestRequest = "loanBankInterestRequestQueue";
    public static String bankInterestReply = "bankInterestReplyQueue";

    public MessageQueue() {
        this.connection = null;
        this.session = null;
        this.producer = null;
        this.consumer = null;
        this.loanRequestQueue = null;
        this.loanReplyQueue = null;
        this.bankInterestRequestQueue = null;
        this.bankInterestReplyQueue = null;
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
                producer = session.createProducer(loanRequestQueue);
            } else if (obj instanceof LoanReply) {
                producer = session.createProducer(loanReplyQueue);
            }else if (obj instanceof BankInterestRequest) {
                producer = session.createProducer(bankInterestRequestQueue);
            } else if (obj instanceof BankInterestReply) {
                producer = session.createProducer(bankInterestReplyQueue);
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
                consumer = session.createConsumer(loanRequestQueue);
            } else if(queue.equals(loanReply)) {
                consumer = session.createConsumer(loanReplyQueue);
            } else if (queue.equals(bankInterestRequest)) {
                consumer = session.createConsumer(bankInterestRequestQueue);
            } else if (queue.equals(bankInterestReply)) {
                consumer = session.createConsumer(bankInterestReplyQueue);
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
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
            factory.setTrustAllPackages(true);
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            loanRequestQueue = session.createQueue(loanRequest);
            loanReplyQueue = session.createQueue(loanReply);
            bankInterestRequestQueue = session.createQueue(bankInterestRequest);
            bankInterestReplyQueue = session.createQueue(bankInterestReply);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
