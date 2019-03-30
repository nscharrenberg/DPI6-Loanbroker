package com.nscharrenberg.elect.broker.gateways.messaging;

import com.nscharrenberg.elect.broker.data.CompanyList;
import com.nscharrenberg.elect.broker.gateways.application.QueueName;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.jms.Connection;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MessageConnectionGateway {
    protected Connection connection;
    protected Session session;
    protected Context jndiContext;
    protected Destination destination;
    protected Properties props;

    public MessageConnectionGateway() {
        initConnection();
    }

    /**
     * Initialize the connection with the broker.
     */
    private void initConnection() {
        props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
        props.put(String.format("queue.%s", QueueName.SEEK_JOB_REQUEST), QueueName.SEEK_JOB_REQUEST);
        props.put(String.format("queue.%s", QueueName.SEEK_JOB_REPLY), QueueName.SEEK_JOB_REPLY);
        props.put(String.format("queue.%s", QueueName.OFFER_JOB_REPLY), QueueName.OFFER_JOB_REPLY);

        /**
         * Queues for companies
         */
        CompanyList.stream().forEach(c -> {
            props.put(String.format("queue.%s_%s", QueueName.OFFER_JOB_REQUEST, c.getName()), String.format("%s_%s", QueueName.OFFER_JOB_REQUEST, c.getName()));
        });

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

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Context getJndiContext() {
        return jndiContext;
    }

    public void setJndiContext(Context jndiContext) {
        this.jndiContext = jndiContext;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
}
