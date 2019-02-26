package abnamro.bank.gateways.messaging;

import messaging.QueueNames;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MessageConnectionGateway {
    protected Connection connection;
    protected Session session;
    protected Context jndiContext;
    protected Destination destination;

    public MessageConnectionGateway() {
        openJMSConnection();
    }

    /**
     * This method initiates the JMS connection for the Queue's using channels.
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
            ActiveMQConnectionFactory factory = (ActiveMQConnectionFactory) jndiContext.lookup("ConnectionFactory");
            factory.setTrustAllPackages(true);
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
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

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setJndiContext(Context jndiContext) {
        this.jndiContext = jndiContext;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
}
