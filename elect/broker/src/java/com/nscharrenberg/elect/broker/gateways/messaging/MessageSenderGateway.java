package com.nscharrenberg.elect.broker.gateways.messaging;

import com.google.gson.Gson;

import javax.jms.*;
import javax.naming.NamingException;
import java.io.Serializable;

public class MessageSenderGateway extends MessageConnectionGateway {
    private MessageProducer producer;

    public MessageSenderGateway() {
    }

    public String produce(String queue, Serializable obj, String messageId) {
        try {
            //TODO: General PRoduce logic to send a JMS message.
            setDestination((Destination) getJndiContext().lookup(queue));
            this.producer = this.getSession().createProducer(this.getDestination());
            this.producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            Message msg = this.getSession().createObjectMessage(generateGson(obj));

            //TODO: Decide wether or not to set a correlationId.
            if(messageId != null) {
                msg.setJMSCorrelationID(messageId);
            }

            producer.send(msg);
            return msg.getJMSMessageID();
        } catch (JMSException | NamingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String generateGson(Serializable obj) {
        //TODO: Convert Object to universally readable JSON body.
        Gson gson = new Gson();
        return gson.toJson(obj);
    }
}
