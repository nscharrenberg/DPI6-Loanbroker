package com.nscharrenberg.elect.microsoft.gateways.messaging;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;

public class MessageReceiverGateway extends MessageConnectionGateway {
    private MessageConsumer consumer;

    public MessageReceiverGateway() {
    }

    public MessageConsumer consume(String queue) {
        try {
            setDestination((Destination) getJndiContext().lookup(queue));
            this.consumer = this.getSession().createConsumer(getDestination());
            this.getConnection().start();

            return consumer;
        } catch (JMSException | NamingException e) {
            e.printStackTrace();
            return consumer;
        }
    }
}