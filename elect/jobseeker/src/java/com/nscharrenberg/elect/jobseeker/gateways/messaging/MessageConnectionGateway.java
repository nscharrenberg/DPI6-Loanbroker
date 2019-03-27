package com.nscharrenberg.elect.jobseeker.gateways.messaging;

import com.nscharrenberg.elect.jobseeker.gateways.application.QueueName;

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

    public MessageConnectionGateway() {
        initConnection();
    }

    /**
     * Initialize the connection with the jobseeker.
     */
    private void initConnection() {
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
        props.put(String.format("queue.%s", QueueName.SEEK_JOB_REQUEST), QueueName.SEEK_JOB_REQUEST);
        props.put(String.format("queue.%s", QueueName.SEEK_JOB_REPLY), QueueName.SEEK_JOB_REPLY);

        try {
            this.jndiContext = new InitialContext(props);
            ConnectionFactory factory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
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
