package gateways.messaging;

import messaging.QueueNames;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MessageConnectionGateway {
    private Connection connection;
    private Session session;
    private Context jndiContext;
    private Destination destination;

    public MessageConnectionGateway(String channel) {
        openJMSConnection(channel);
    }

    /**
     * This method initiates the JMS connection for the Queue's using channels.
     * It establishes an ActiveMQ connection and the corresponding Queue's.
     * @param channel
     */
    private void openJMSConnection(String channel) {
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
            this.destination = (Destination) jndiContext.lookup(channel);
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
    }

    public Context getJndiContext() {
        return jndiContext;
    }

    public Destination getDestination() {
        return destination;
    }
}
